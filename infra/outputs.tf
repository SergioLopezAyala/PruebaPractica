output "api_url" {
  description = "Public base URL of the API."
  value       = "http://${aws_lb.this.dns_name}"
}

output "ecr_repository_url" {
  description = "ECR repository to push the application image to."
  value       = aws_ecr_repository.app.repository_url
}

output "ecs_cluster_name" {
  description = "ECS cluster running the application."
  value       = aws_ecs_cluster.this.name
}

output "ecs_service_name" {
  description = "ECS service running the application."
  value       = aws_ecs_service.app.name
}

output "atlas_cluster_host" {
  description = "Atlas cluster SRV address (without credentials)."
  value       = mongodbatlas_advanced_cluster.this.connection_strings.standard_srv
}

output "mongodb_uri" {
  description = "Full connection string, for running the app locally against Atlas."
  value       = local.mongodb_uri
  sensitive   = true
}
