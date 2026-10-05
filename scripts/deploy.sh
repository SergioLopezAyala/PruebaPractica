#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

tag="${1:-$(git rev-parse --short HEAD)}"

tf_output() {
  terraform -chdir=infra output -raw "$1"
}

repository="$(tf_output ecr_repository_url)"
cluster="$(tf_output ecs_cluster_name)"
service="$(tf_output ecs_service_name)"
api_url="$(tf_output api_url)"
registry="${repository%%/*}"
region="$(echo "$registry" | cut -d. -f4)"

echo "Logging in to $registry"
aws ecr get-login-password --region "$region" | docker login --username AWS --password-stdin "$registry"

echo "Building $repository:$tag"
docker build --platform linux/arm64 -t "$repository:$tag" -t "$repository:latest" .
docker push "$repository:$tag"
docker push "$repository:latest"

echo "Rolling out $service"
aws ecs update-service --region "$region" --cluster "$cluster" --service "$service" \
  --force-new-deployment > /dev/null
aws ecs wait services-stable --region "$region" --cluster "$cluster" --services "$service"

echo "Deployed $tag to $api_url"
