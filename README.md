# Franchise API

Reactive REST API to manage franchises, their branches and the products each branch stocks.
Built with Spring Boot 4 and WebFlux, persisted in MongoDB, provisioned on AWS and MongoDB Atlas with Terraform.

**Deployed URL:** http://franchise-api-680928729.us-east-1.elb.amazonaws.com
(Swagger UI: http://franchise-api-680928729.us-east-1.elb.amazonaws.com/swagger-ui.html)

## Contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Running locally](#running-locally)
- [Configuration](#configuration)
- [Tests](#tests)
- [API](#api)
- [Cloud infrastructure (Terraform)](#cloud-infrastructure-terraform)
- [Deploying](#deploying)
- [Design decisions](#design-decisions)

## Features

- Create a franchise, add branches to it and products to each branch
- Update a product's stock and delete products
- Rename franchises, branches and products
- For a franchise, get the product with the most stock in each branch, including which branch it belongs to
- Validation, uniqueness rules and consistent JSON errors
- Dockerized, with Terraform for MongoDB Atlas and AWS (ECS Fargate)

## Tech stack

| Area | Choice |
|---|---|
| Language / framework | Java 17, Spring Boot 4.1, Spring WebFlux (Project Reactor) |
| Persistence | MongoDB via Spring Data Reactive MongoDB (MongoDB Atlas in the cloud) |
| Build | Maven (wrapper included) |
| Tests | JUnit 5, Mockito, StepVerifier, WebTestClient, Testcontainers, JaCoCo |
| API docs | springdoc-openapi (Swagger UI) |
| Packaging | Docker, Docker Compose |
| Infrastructure | Terraform: MongoDB Atlas M0, AWS VPC, ALB, ECR, ECS Fargate (ARM64), SSM |
| CI | GitHub Actions: build and test, Terraform validation |

## Architecture

Hexagonal (ports and adapters). Dependencies point inward: infrastructure → application → domain.

```
src/main/java/io/github/sergiolopezayala/franchise/
├── domain/                     Plain Java, no framework imports
│   ├── model/                  Franchise, Branch, Product (immutable records), TopStockProduct, Names
│   ├── exception/              NotFoundException, DuplicateNameException, InvalidValueException
│   └── port/                   FranchiseRepositoryPort, IdGenerator
├── application/usecase/        One class per use case (CreateFranchiseUseCase, AddBranchUseCase, ...)
└── infrastructure/
    ├── entrypoint/rest/        Functional routes, handler, request/response DTOs, request validation
    ├── persistence/mongo/      Documents, reactive repository, adapter implementing the port
    └── config/                 Use case wiring, global error handler
infra/                          Terraform (Atlas + AWS)
scripts/deploy.sh               Build, push to ECR and roll out the ECS service
```

A request flows like this:

```
HTTP → FranchiseRouter → FranchiseHandler → UseCase → Franchise (domain rules) → FranchiseRepositoryPort
                                                                                       ↑ implemented by
                                                                             FranchiseMongoAdapter → MongoDB
```

Each franchise is a single MongoDB document with its branches and products embedded:

```json
{ "_id": "...", "name": "Acme", "version": 3,
  "branches": [ { "id": "uuid", "name": "Downtown",
                  "products": [ { "id": "uuid", "name": "Coffee", "stock": 10 } ] } ] }
```

## Running locally

### Prerequisites

- Java 17 (only needed to run without Docker)
- Docker (for Docker Compose and for the Testcontainers tests)

### With Docker Compose (app + MongoDB)

```bash
docker compose up --build
```

The API is available at `http://localhost:8080` and Swagger UI at `http://localhost:8080/swagger-ui.html`.
Stop it with `docker compose down` (add `-v` to also delete the MongoDB data volume).

### Without Docker for the app

Start MongoDB (or point `MONGODB_URI` at any MongoDB, such as Atlas), then run the app with the Maven wrapper:

```bash
docker compose up -d mongo        # or use your own MongoDB
./mvnw spring-boot:run
```

To run against the Atlas cluster created by Terraform:

```bash
MONGODB_URI="$(terraform -chdir=infra output -raw mongodb_uri)" ./mvnw spring-boot:run
```

## Configuration

| Variable | Default | Description |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/franchises` | MongoDB connection string, including the database name |
| `SERVER_PORT` | `8080` | HTTP port |

## Tests

```bash
./mvnw verify
```

Runs every test and writes a coverage report to `target/site/jacoco/index.html`. Docker must be running: the persistence and end-to-end tests start a real MongoDB with Testcontainers.

| Level | What it covers |
|---|---|
| Domain unit tests | Validation, name uniqueness, every change operation, top-stock with multiple branches, empty branches and ties |
| Use case unit tests | Each use case with a mocked repository port, verified with `StepVerifier`, including not-found, duplicate and invalid input paths |
| Router tests | Every endpoint over HTTP with `WebTestClient`, success and failure status codes, error body format |
| MongoDB adapter tests | Against Testcontainers: mapping, versioning, case-insensitive unique index, optimistic locking |
| End-to-end tests | Full stack against Testcontainers: the whole lifecycle, error responses and the OpenAPI document |

## API

Base path: `/api/v1`. All bodies are JSON.

| Method | Path | Description | Success |
|---|---|---|---|
| POST | `/franchises` | Create a franchise | 201 |
| PATCH | `/franchises/{franchiseId}/name` | Rename a franchise | 200 |
| POST | `/franchises/{franchiseId}/branches` | Add a branch | 201 |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/name` | Rename a branch | 200 |
| POST | `/franchises/{franchiseId}/branches/{branchId}/products` | Add a product | 201 |
| DELETE | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}` | Delete a product | 204 |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` | Update stock | 200 |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` | Rename a product | 200 |
| GET | `/franchises/{franchiseId}/products/top-stock` | Product with the most stock in each branch | 200 |

Creates return a `Location` header pointing at the new resource.

### Rules

- Names are required, trimmed and at most 100 characters.
- Stock is an integer greater than or equal to 0.
- Names are unique within their parent, ignoring case: franchise names across the system, branch names within a franchise, product names within a branch.
- Top stock: branches without products are left out. On a tie, the product whose name sorts first alphabetically wins.

### Errors

Every error has the same shape:

```json
{ "timestamp": "2026-10-05T20:35:57.955Z", "status": 404, "error": "Not Found",
  "message": "Branch 5f0c... not found", "path": "/api/v1/franchises/.../branches/5f0c.../name" }
```

| Status | When |
|---|---|
| 400 | Invalid body (blank or too long name, negative or missing stock, malformed JSON) |
| 404 | Franchise, branch or product does not exist, or unknown route |
| 409 | Name already in use, or the franchise was modified by a concurrent request |

### Example session

Set `API=http://localhost:8080/api/v1` (or the deployed URL followed by `/api/v1`), then:

```bash
# Create a franchise
curl -s -X POST $API/franchises -H 'Content-Type: application/json' \
  -d '{"name":"Acme Coffee"}'
# → 201 {"id":"<franchiseId>","name":"Acme Coffee","branches":[]}

# Add a branch
curl -s -X POST $API/franchises/<franchiseId>/branches -H 'Content-Type: application/json' \
  -d '{"name":"Downtown"}'
# → 201 {"id":"<branchId>","name":"Downtown","products":[]}

# Add a product to the branch
curl -s -X POST $API/franchises/<franchiseId>/branches/<branchId>/products \
  -H 'Content-Type: application/json' -d '{"name":"Latte","stock":10}'
# → 201 {"id":"<productId>","name":"Latte","stock":10}

# Update its stock
curl -s -X PATCH $API/franchises/<franchiseId>/branches/<branchId>/products/<productId>/stock \
  -H 'Content-Type: application/json' -d '{"stock":25}'
# → 200 {"id":"<productId>","name":"Latte","stock":25}

# Product with the most stock in each branch
curl -s $API/franchises/<franchiseId>/products/top-stock
# → 200 [{"branchId":"<branchId>","branchName":"Downtown","productId":"<productId>","productName":"Latte","stock":25}]

# Rename the franchise, the branch and the product
curl -s -X PATCH $API/franchises/<franchiseId>/name -H 'Content-Type: application/json' \
  -d '{"name":"Acme Coffee Co"}'
curl -s -X PATCH $API/franchises/<franchiseId>/branches/<branchId>/name \
  -H 'Content-Type: application/json' -d '{"name":"Uptown"}'
curl -s -X PATCH $API/franchises/<franchiseId>/branches/<branchId>/products/<productId>/name \
  -H 'Content-Type: application/json' -d '{"name":"Vanilla Latte"}'

# Delete the product
curl -s -o /dev/null -w '%{http_code}\n' \
  -X DELETE $API/franchises/<franchiseId>/branches/<branchId>/products/<productId>
# → 204
```

## Cloud infrastructure (Terraform)

`infra/` provisions everything the deployed application needs:

- **MongoDB Atlas:** a project, an M0 (free) cluster on AWS in the same region, and a database user restricted to read/write on the `franchises` database
- **AWS:** a VPC with two public subnets, an Application Load Balancer, an ECR repository, an ECS Fargate service (ARM64) with CloudWatch logs, and the connection string stored as an SSM `SecureString` that is injected into the task as a secret

### Prerequisites

- Terraform 1.6 or newer, the AWS CLI, and AWS credentials (`aws configure`)
- A MongoDB Atlas organization and a service account with the *Organization Project Creator* role

### Provisioning

```bash
cp infra/terraform.tfvars.example infra/terraform.tfvars   # fill in the Atlas organization ID and service account
terraform -chdir=infra init
terraform -chdir=infra plan -out=tfplan
terraform -chdir=infra apply tfplan
```

`terraform.tfvars`, plan files and state are git-ignored because they contain secrets. Outputs:

| Output | Description |
|---|---|
| `api_url` | Public URL of the load balancer |
| `ecr_repository_url` | Where `scripts/deploy.sh` pushes the image |
| `ecs_cluster_name`, `ecs_service_name` | Used by `scripts/deploy.sh` |
| `atlas_cluster_host` | Atlas SRV host (without credentials) |
| `mongodb_uri` | Full connection string (sensitive) |

To remove everything: `terraform -chdir=infra destroy`.

## Deploying

After `terraform apply`, build and roll out the application:

```bash
scripts/deploy.sh            # tags the image with the current commit SHA (and latest)
```

The script logs in to ECR, builds an `arm64` image, pushes it, forces a new ECS deployment and waits until the service is stable. On the very first apply the service has no image yet, so its tasks fail until this script runs once.

## Design decisions

- **One document per franchise.** Branches and products are always changed through their franchise, so each operation is a single atomic write. Branch and product IDs are UUIDs generated by the application.
- **Optimistic locking.** `@Version` on the franchise document rejects concurrent overwrites. The API answers 409 and the client can retry.
- **Rules live in the domain.** Validation, uniqueness and the top-stock rule are plain Java in `domain/`, tested without Spring or a database. Request DTOs are also validated with Bean Validation so clients get field-level messages early.
- **Franchise name uniqueness is enforced twice.** The use case checks first for a clear error, and a case-insensitive unique index closes the race between two simultaneous requests.
- **Deterministic top stock.** Highest stock first; ties go to the name that sorts first alphabetically (ignoring case).
- **Functional endpoints.** `RouterFunction` and a handler instead of annotated controllers, with a single `WebExceptionHandler` producing every error body.
- **ARM64 on Fargate.** Matches images built on Apple Silicon and costs less than x86.

### Known trade-offs

- **Atlas network access is open (`0.0.0.0/0`).** Fargate tasks have no fixed egress IP and M0 clusters do not support private endpoints, so access is protected by the generated credentials. A production setup would use a dedicated cluster with PrivateLink or a NAT gateway with a fixed IP.
- **HTTP only.** The load balancer has no TLS certificate because there is no custom domain. Adding one would mean an ACM certificate and an HTTPS listener.
- **No single-resource GET endpoints.** They are not part of the requested contract, so the `Location` headers point at resources that cannot be fetched individually.
- **The load balancer health check uses `/v3/api-docs`**, since the application does not include Spring Boot Actuator.
