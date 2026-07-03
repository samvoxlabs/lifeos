#!/bin/bash

# Kill any process on port 8080
PORT=8080
echo "Checking for processes on port $PORT..."

if lsof -i :$PORT &>/dev/null; then
    PID=$(lsof -i :$PORT | grep LISTEN | awk '{print $2}' | head -1)
    if [ -n "$PID" ]; then
        echo "Killing process $PID on port $PORT..."
        kill -9 $PID
        sleep 1
        echo "Port $PORT freed"
    fi
else
    echo "Port $PORT is free"
fi

# Start the app
echo "Starting FamilyOS..."
./mvnw spring-boot:run
