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

# Copy JAR file to Docker directory
update_jar() {
    print_status "Removing existing JAR file from Docker directory..."
    rm -f docker/app.jar
    print_success "Existing JAR file removed successfully"
    print_status "Copying JAR file to Docker directory..."
    cp target/*.jar docker/app.jar
    print_success "JAR file copied successfully"
}

# Check MySQL container status
check_mysql_status() {
    print_status "Checking MySQL container status..."
    
    TIMEOUT=60  # 1 minute timeout
    INTERVAL=5  # Check every 5 seconds
    
    ELAPSED=0
    while [ $ELAPSED -lt $TIMEOUT ]; do
        if docker inspect --format='{{.State.Health.Status}}' mySQL 2>/dev/null | grep -q "healthy"; then
            print_success "MySQL container is healthy"
            return 0
        fi
        sleep $INTERVAL
        ELAPSED=$((ELAPSED + INTERVAL))
        echo "Waiting for MySQL to be healthy... ($ELAPSED seconds elapsed)"
    done
    
    echo -e "${RED}Error: MySQL container is not healthy. Please check MySQL container status.${NC}"
    exit 1
}

# Rebuild and restart only the Spring Boot container
rebuild_spring_boot_container() {
    print_status "Rebuilding Spring Boot container..."
    cd docker
    
    # Stop and remove only the Spring Boot container
    docker-compose stop app
    docker-compose rm -f app
    
    # Rebuild and start only the Spring Boot container
    docker-compose up --build -d app
    
    print_success "Spring Boot container rebuilt successfully"
}

# Check Spring Boot container status
check_spring_boot_status() {
    print_status "Checking Spring Boot container status..."
    
    TIMEOUT=60  # 1 minute timeout
    INTERVAL=5  # Check every 5 seconds
    
    ELAPSED=0
    while [ $ELAPSED -lt $TIMEOUT ]; do
        if docker container inspect diglib 2>/dev/null | grep -q "running"; then
            print_success "Spring Boot container is running"
            return 0
        fi
        sleep $INTERVAL
        ELAPSED=$((ELAPSED + INTERVAL))
        echo "Waiting for Spring Boot container to start... ($ELAPSED seconds elapsed)"
    done
    
    echo -e "${RED}Error: Spring Boot container failed to start${NC}"
    docker-compose logs app
    exit 1
}

# Main execution
main() {
    print_status "Starting rebuild process..."
    
    # Check if we're in the correct directory
    if [ ! -f "pom.xml" ]; then
        echo -e "${RED}Error: pom.xml not found. Please run this script from the project root directory.${NC}"
        exit 1
    fi
    
    check_required_commands
    check_mysql_status
    build_spring_boot
    update_jar
    rebuild_spring_boot_container
    check_spring_boot_status
    
    print_success "Rebuild completed successfully!"
    echo -e "${GREEN}Spring Boot application is now running at http://localhost:8080${NC}"
}

# Run main function
main