#!/bin/bash

read -p "Enter the port for the RMI Registry (default 1099): " PORT
PORT=${PORT:-1099}  # Si el usuario no introduce nada, usa 1099

echo "Starting RMI Registry on port $PORT..."
rmiregistry $PORT &  # Ejecuta el RMI Registry en segundo plano

sleep 2  # Espera 2 segundos para asegurarse de que el registro esté iniciado

echo "Starting Server..."
java -cp . src.Server.Server $PORT
