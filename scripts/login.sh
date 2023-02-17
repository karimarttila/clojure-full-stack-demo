#!/bin/bash

echo '{"username": "jartsa", "password": "joo"}' | http --json POST localhost:7171/api/login

#http POST http://localhost:7171/api/login username=jartsa password=joo Content-Type:application/json

#curl -H "Content-Type: application/json" -X POST -d '{"username": "jartsa", "password": "joo"}' http://localhost:7171/api/login
