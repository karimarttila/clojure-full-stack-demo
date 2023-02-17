#!/bin/bash

curl -H "Content-Type: application/json" -X POST -d '{"ping":"Jee!"}' http://localhost:7171/api/ping