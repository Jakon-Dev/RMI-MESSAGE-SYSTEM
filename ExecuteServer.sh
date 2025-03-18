#!/bin/bash

read -p "Enter the port for the RMI Registry (default 1099): " PORT
PORT=${PORT:-1099}

echo "Starting RMI Registry on port $PORT..."
rmiRegistry "$PORT" &

sleep 2

echo "Starting Server..."
java -cp . src/Server/Server "$PORT"