#!/bin/bash

docker-compose up --build
mvn allure:serve

