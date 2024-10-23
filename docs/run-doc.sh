#!/bin/bash

# Construir el contenedor
docker build -t redoc-server .

# Ejecutar el contenedor
docker run --rm -it -p 8081:8080 redoc-server
