default:
    just -l

# Run the app in production mode via Docker Compose
prod:
    docker compose up medapp --build

# Run the app in development mode with hot-reloading
dev:
    docker compose up medapp-dev --watch --build

test:
    cd medapp/ && ./gradlew test

run-single:
    cd medapp && ./gradlew bootRun
