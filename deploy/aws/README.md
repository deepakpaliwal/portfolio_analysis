# AWS Deployment

Deploy the Portfolio Analysis application to AWS.

## Architecture

```
                    ┌─────────────────────────────────────────────┐
                    │                   AWS VPC                    │
                    │                                              │
  Internet ──► [ALB] ──► [ECS Fargate: API]  ──► [RDS PostgreSQL]│
                │         [ECS Fargate: UI ]       (Multi-AZ)    │
                │         [ECS Fargate: Batch]                   │
                │                                                 │
                │    [S3: UI Static Assets via CloudFront]       │
                │    [ECR: Docker Image Registry]                │
                │    [CloudWatch: Logs + Metrics]                │
                │    [Secrets Manager: DB creds, JWT secret]     │
                └─────────────────────────────────────────────────┘
```

## Deployment Options

### Option 1: ECS Fargate (Recommended)

Serverless container orchestration. No EC2 instances to manage.

```bash
# Deploy using CloudFormation
aws cloudformation deploy \
    --template-file deploy/aws/cloudformation.yml \
    --stack-name portfolio-analysis \
    --parameter-overrides \
        Environment=prod \
        DBPassword=<secure-password> \
    --capabilities CAPABILITY_IAM
```

### Option 2: EC2 + Docker Compose

For lower cost or when Fargate isn't suitable.

```bash
# Deploy to EC2 instance
./scripts/deploy-server.sh --host ec2-xx-xx-xx-xx.compute.amazonaws.com \
    --module all --profile prod --user ec2-user
```

## Prerequisites

1. AWS CLI configured with appropriate credentials
2. Docker images pushed to ECR
3. RDS PostgreSQL instance provisioned
4. Secrets stored in AWS Secrets Manager

## Push Images to ECR

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | \
    docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build and push
docker buildx build --platform linux/amd64 -t <account-id>.dkr.ecr.us-east-1.amazonaws.com/portfolio-api:latest -f portfolio-api/Dockerfile --push .
docker buildx build --platform linux/amd64 -t <account-id>.dkr.ecr.us-east-1.amazonaws.com/portfolio-batch:latest -f portfolio-batch/Dockerfile --push .
docker buildx build --platform linux/amd64 -t <account-id>.dkr.ecr.us-east-1.amazonaws.com/portfolio-ui:latest -f portfolio-ui/Dockerfile --push portfolio-ui/
```

## Environment Variables (via Secrets Manager / Parameter Store)

| Variable | Description |
|---|---|
| `DB_HOST` | RDS endpoint |
| `DB_PORT` | `5432` |
| `DB_NAME` | `portfolio_prod` |
| `DB_USERNAME` | From Secrets Manager |
| `DB_PASSWORD` | From Secrets Manager |
| `JWT_SECRET` | From Secrets Manager |
| `SPRING_PROFILES_ACTIVE` | `prod` |
