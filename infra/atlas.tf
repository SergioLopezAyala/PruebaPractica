resource "mongodbatlas_project" "this" {
  name   = var.project_name
  org_id = var.atlas_org_id
}

resource "mongodbatlas_advanced_cluster" "this" {
  project_id   = mongodbatlas_project.this.id
  name         = "${var.project_name}-cluster"
  cluster_type = "REPLICASET"

  replication_specs = [
    {
      region_configs = [
        {
          electable_specs = {
            instance_size = "M0"
          }
          provider_name         = "TENANT"
          backing_provider_name = "AWS"
          region_name           = upper(replace(var.aws_region, "-", "_"))
          priority              = 7
        }
      ]
    }
  ]
}

resource "random_password" "db" {
  length  = 32
  special = false
}

resource "mongodbatlas_database_user" "app" {
  project_id         = mongodbatlas_project.this.id
  auth_database_name = "admin"
  username           = "${var.project_name}-app"
  password           = random_password.db.result

  roles {
    role_name     = "readWrite"
    database_name = var.database_name
  }

  scopes {
    name = mongodbatlas_advanced_cluster.this.name
    type = "CLUSTER"
  }
}

resource "mongodbatlas_project_ip_access_list" "anywhere" {
  project_id = mongodbatlas_project.this.id
  cidr_block = "0.0.0.0/0"
  comment    = "Fargate tasks (no fixed egress IP)"
}

locals {
  mongodb_uri = format(
    "%s/%s?retryWrites=true&w=majority",
    replace(
      mongodbatlas_advanced_cluster.this.connection_strings.standard_srv,
      "mongodb+srv://",
      "mongodb+srv://${mongodbatlas_database_user.app.username}:${random_password.db.result}@"
    ),
    var.database_name
  )
}
