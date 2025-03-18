#!/bin/bash

read -p "Enter the server IP (default localhost): " SERVER_IP
SERVER_IP=${SERVER_IP:-localhost}

echo "Connecting to server at $SERVER_IP..."
java -cp . src/Client/Client "$SERVER_IP"
