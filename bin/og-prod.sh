#!/usr/bin/env bash
set -ex
DOCKER_IMAGE=${DOCKER_IMAGE:-"ghcr.io/otobrglez/koofr-watch:latest"}
export DOCKER_HOST="ssh://low"
export DOCKER_COMPOSE_FLAGS=${DOCKER_COMPOSE_FLAGS:="-f docker/docker-compose.prod.yml"}

./bin/og-wrap.sh $@
