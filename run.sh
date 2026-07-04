#!/bin/bash

set -e

CONTAINER_NAME="lifeos-postgres"
DB_PORT="${DB_PORT:-5432}"
APP_PORT=8080

echo "=========================================="
echo "FamilyOS Startup Script"
echo "=========================================="

# Check and start Docker container
echo ""
echo "📦 Checking Docker database container..."

if docker ps --filter "name=$CONTAINER_NAME" --format "{{.Names}}" | grep -q "^$CONTAINER_NAME$"; then
    echo "✅ Container '$CONTAINER_NAME' is already running"
else
    if docker ps -a --filter "name=$CONTAINER_NAME" --format "{{.Names}}" | grep -q "^$CONTAINER_NAME$"; then
        echo "⚡ Starting existing container '$CONTAINER_NAME'..."
        docker start "$CONTAINER_NAME"
        echo "✅ Container started"
    else
        echo "🚀 Launching new container using docker-compose..."
        docker-compose -f docker/compose.yml up -d
        echo "✅ Container created and started"
    fi
    
    echo "⏳ Waiting for database to be ready..."
    sleep 3
fi

# Check database connectivity
echo "🔍 Verifying database connectivity on port $DB_PORT..."
if timeout 10 bash -c "cat < /dev/null > /dev/tcp/127.0.0.1/$DB_PORT"; then
    echo "✅ Database is accessible"
else
    echo "❌ Database is not accessible. Please check your Docker setup."
    exit 1
fi

# Kill any process on port 8080
echo ""
echo "🔗 Checking for processes on port $APP_PORT..."

if lsof -i :$APP_PORT &>/dev/null; then
    PID=$(lsof -i :$APP_PORT | grep LISTEN | awk '{print $2}' | head -1)
    if [ -n "$PID" ]; then
        echo "Killing process $PID on port $APP_PORT..."
        kill -9 $PID
        sleep 1
        echo "✅ Port $APP_PORT freed"
    fi
else
    echo "✅ Port $APP_PORT is free"
fi

# Start the app
echo ""
echo "🚀 Starting FamilyOS application..."
echo "=========================================="
./mvnw spring-boot:run
