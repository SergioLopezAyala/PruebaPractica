variable "project_name" {
  description = "Prefix for every resource name."
  type        = string
  default     = "franchise-api"
}

variable "aws_region" {
  description = "AWS region for the application. The Atlas cluster is created in the same region."
  type        = string
  default     = "us-east-1"
}

variable "atlas_org_id" {
  description = "MongoDB Atlas organization ID where the project is created."
  type        = string
}

variable "database_name" {
  description = "MongoDB database used by the application."
  type        = string
  default     = "franchises"
}

variable "image_tag" {
  description = "Tag of the application image in ECR to deploy."
  type        = string
  default     = "latest"
}

variable "app_port" {
  description = "Port the application listens on inside the container."
  type        = number
  default     = 8080
}

variable "task_cpu" {
  description = "Fargate task CPU units."
  type        = number
  default     = 512
}

variable "task_memory" {
  description = "Fargate task memory in MiB."
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Number of application tasks to run."
  type        = number
  default     = 1
}
