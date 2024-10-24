# DigLib Initial Deployment Guide

This guide explains how to perform the initial setup and deployment of the DigLib application to Google Cloud Platform.

## Prerequisites
- Google Cloud SDK installed
- Docker installed
- Access to Google Cloud Console with billing enabled
- Project created in Google Cloud

## 1. Initial Setup

### 1.1 Environment Variables
Create a copy of `.env.example` as `.env` and fill in the values:
```bash
cp .env.example .env
```

Required variables:
- `ROOT_PASSWORD`: MySQL root administrator password (for database management)
- `DB_PASS`: Password for the application's database user (used by the application to connect)

> **Note**: You need two different passwords because:
> - ROOT_PASSWORD is for the MySQL root user (administrator) who has full access to manage the database instance
> - DB_PASS is for the application's user (main) who only has access to the specific database (diglib)
> This separation follows the principle of least privilege - your application runs with minimal required permissions.

### 1.2 Source Environment Variables
```bash
source .env
```

### 1.3 Initialize Google Cloud
```bash
# Login to Google Cloud
gcloud auth login

# Set the project
gcloud config set project $PROJECT_ID

# Enable required APIs
gcloud services enable \
    cloudbuild.googleapis.com \
    run.googleapis.com \
    artifactregistry.googleapis.com \
    sqladmin.googleapis.com
```

## 2. Database Setup

### 2.1 Create Cloud SQL Instance
```bash
gcloud sql instances create $DB_INSTANCE_NAME \
    --database-version=MYSQL_8_0 \
    --tier=db-f1-micro \
    --region=$REGION \
    --root-password=$ROOT_PASSWORD \
    --storage-type=SSD \
    --availability-type=zonal
```

### 2.2 Create Database and User
```bash
# Create the database
gcloud sql databases create $DB_NAME --instance=$DB_INSTANCE_NAME

# Create the application user
gcloud sql users create $DB_USER \
    --instance=$DB_INSTANCE_NAME \
    --password=$DB_PASS
```

## 3. Storage Setup

Create the Cloud Storage bucket:
```bash
gcloud storage buckets create gs://$BUCKET_NAME \
    --location=$REGION \
    --uniform-bucket-level-access
```

## 4. Secret Management

Create secrets for database access:
```bash
# Create secrets
echo -n "$DB_USER" | gcloud secrets create diglib-db-user --data-file=-
echo -n "$DB_PASS" | gcloud secrets create diglib-db-pass --data-file=-

# Get the service account email
export SERVICE_ACCOUNT=$(gcloud iam service-accounts list \
    --filter="EMAIL:${PROJECT_ID}-compute@developer.gserviceaccount.com" \
    --format="value(EMAIL)")

# Grant access to the secrets
gcloud secrets add-iam-policy-binding diglib-db-user \
    --member="serviceAccount:$SERVICE_ACCOUNT" \
    --role="roles/secretmanager.secretAccessor"

gcloud secrets add-iam-policy-binding diglib-db-pass \
    --member="serviceAccount:$SERVICE_ACCOUNT" \
    --role="roles/secretmanager.secretAccessor"
```

## 5. GitLab CI/CD Setup

1. Go to your GitLab project's Settings > CI/CD > Variables
2. Add the following variables:
   - `GCP_SERVICE_KEY`: The JSON key file content from your service account
   - `PROJECT_ID`: Your Google Cloud project ID
   - `REGION`: Your chosen region
   - `CLOUD_RUN_SERVICE`: diglib
   - `DB_INSTANCE_NAME`: Your Cloud SQL instance name

## 6. Verification Steps

### 6.1 Verify Database Connection
```bash
# Get the connection name
export INSTANCE_CONNECTION_NAME=$(gcloud sql instances describe $DB_INSTANCE_NAME \
    --format='value(connectionName)')

# Test connection (will prompt for password)
mysql -u $DB_USER -p \
    --host=127.0.0.1 \
    --port=3306 \
    $DB_NAME
```

### 6.2 Verify Storage Access
```bash
# Test bucket access
gsutil ls gs://$BUCKET_NAME
```

## Troubleshooting

### Common Issues
1. Database Connection Issues:
   - Verify the connection name in INSTANCE_CONNECTION_NAME
   - Check if the user and password are correct
   - Ensure the service account has necessary permissions

2. Storage Issues:
   - Verify bucket permissions
   - Check if the service account has storage access

3. Deployment Issues:
   - Check Cloud Run logs: `gcloud runs logs read $CLOUD_RUN_SERVICE`
   - Verify all environment variables are set correctly

### Getting Help
- Check Cloud Run logs: `gcloud logging read "resource.type=cloud_run_revision"`
- Check SQL instance logs: `gcloud sql instances describe $DB_INSTANCE_NAME`
- View service status: `gcloud run services describe $CLOUD_RUN_SERVICE`

## Security Notes
1. Never commit `.env` file to version control
2. Regularly rotate database passwords and service account keys
3. Use least-privilege principle for service accounts
4. Enable audit logging for sensitive operations

Need help? Check the [troubleshooting guide](#troubleshooting) or raise an issue in the repository.