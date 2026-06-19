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

# Deschide toate dashboardurile importante, gen de la admin la user la mailpit
open-webs:
    open "http://localhost:8080"

    #vezi sa fie in alt browser / machine whatever sa nu se bata cu tokenu de regular user de pe primu
    open "http://localhost:8080/admin"

    #mailpit
    open "http://localhost:8025"
