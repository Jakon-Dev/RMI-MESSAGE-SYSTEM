#!/bin/bash

read -p "Enter the server IP (default localhost): " SERVER_IP
SERVER_IP=${SERVER_IP:-localhost}  # Si el usuario no introduce nada, usa localhost

echo "Connecting to server at $SERVER_IP..."
java -cp . src.Client.Client $SERVER_IP
