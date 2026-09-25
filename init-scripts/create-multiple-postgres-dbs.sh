#!/bin/bash
# This script runs automatically the first time the postgres container starts
# (docker-entrypoint-initdb.d convention). It reads the comma-separated
# POSTGRES_MULTIPLE_DATABASES env var and creates one database per name,
# so user-service, inventory-service, and order-service each get their own
# isolated database inside the same Postgres instance (local dev only ???
# on AWS you'd likely use separate RDS instances per service, or at least
# separate schemas/credentials).

set -e
set -u

function create_database() {
	local database=$1
	echo "Creating database '$database'"
	psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
	    CREATE DATABASE $database;
	    GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
	echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
	for db in $(echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
		create_database "$db"
	done
	echo "Multiple databases created"
fi