#!/bin/bash

docker-compose -f compose.yaml stop
docker-compose -f compose.yaml rm -f
docker-compose -f compose.yaml up -d
