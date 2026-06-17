default:
    just -l

# Docker compose-u cu mailpit si ce-o mai fi
run:
    cd medapp/ && docker compose up --build

test:
    cd medapp/ && ./gradlew test

run-single:
    cd medapp && ./gradlew bootRun
