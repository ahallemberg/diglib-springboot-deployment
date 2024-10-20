#!/bin/bash

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Error handling
set -e
trap 'catch $? $LINENO' ERR

catch() {
    if [ "$1" != "0" ]; then
        echo -e "${RED}Error $1 occurred on line $2${NC}"
    fi
}

# Function to print status messages
print_status() {
    echo -e "${YELLOW}>>> $1${NC}"
}

# Function to print success messages
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to check if command exists
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}Error: $1 is required but not installed.${NC}"
        exit 1
    fi
}

# Check required commands
check_required_commands() {
    print_status "Checking required commands..."
    check_command "docker"
    check_command "docker-compose"
    check_command "mvn"
    print_success "All required commands are available"
}

# Build Spring Boot application
build_spring_boot() {
    print_status "Building Spring Boot application..."
    ./mvnw clean package
    print_success "Spring Boot application built successfully"
}

# Build and start Docker containers
start_docker_containers() {
    print_status "Building and starting Docker containers..."
    cd docker
    
    # Stop any running containers
    docker-compose down
    
    # Remove old containers, networks, and images
    docker-compose rm -f
    
    # Build and start containers
    docker-compose up --build -d
    
    print_success "Docker containers started successfully"
}

# Check container status
check_container_status() {
    print_status "Checking container status..."
    
    # Wait for containers to be healthy
    TIMEOUT=300  # 5 minutes timeout
    INTERVAL=10  # Check every 10 seconds
    
    ELAPSED=0
    while [ $ELAPSED -lt $TIMEOUT ]; do
        if docker-compose ps | grep -q "healthy"; then
            print_success "All containers are healthy"
            return 0
        fi
        sleep $INTERVAL
        ELAPSED=$((ELAPSED + INTERVAL))
        echo "Waiting for containers to be healthy... ($ELAPSED seconds elapsed)"
    done
    
    echo -e "${RED}Error: Containers did not become healthy within timeout period${NC}"
    docker-compose logs
    exit 1
}

# Main execution
main() {
    print_status "Starting build and deploy process..."
    
    # Check if we're in the correct directory
    if [ ! -f "pom.xml" ]; then
        echo -e "${RED}Error: pom.xml not found. Please run this script from the project root directory.${NC}"
        exit 1
    fi
    
    
    check_required_commands
    build_spring_boot
    copy_files
    start_docker_containers
    check_container_status
    
    print_success "Build and deploy completed successfully!"
    echo -e "${GREEN}Application is now running at http://localhost:8080${NC}"
}

# Run main function
main