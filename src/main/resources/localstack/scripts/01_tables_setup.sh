#!/bin/bash

enable debug
# set -x
aws dynamodb create-table --table-name Students

##### Students table creation #####
aws --endpoint-url=http://localhost:4566 \
    dynamodb create-table \
    --table-name students-local-table \
    --attribute-definitions \
        AttributeName=pk,AttributeType=S \
        AttributeName=sk,AttributeType=S \
    --key-schema \
        AttributeName=pk,KeyType=HASH \
        AttributeName=sk,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST


##### Creación de la tabla resources-local-table #####
aws --endpoint-url=http://localhost:4566 \
    dynamodb create-table \
    --table-name resources-local-table \
    --attribute-definitions \
      AttributeName=pk,AttributeType=S \
    --key-schema AttributeName=pk,KeyType=HASH \
     --billing-mode PAY_PER_REQUEST
