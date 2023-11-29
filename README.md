# Customer Hub
This is a demo project of to store and retrieve customers data.

# Solution
A JVM based backend application using REST that manages customer hub system.

## Tech Stack
- Java 17
- Spring Boot 3.2.0
- Spring Data
- Mysql
- H2 (only for tests)
- Swagger (API First)
- Docker
- Prometheus and grafana
## Api First
This application is developed using [Api First](https://swagger.io/resources/articles/adopting-an-api-first-approach/) approach.
the api is defined in [api.yaml](src/main/resources/api/customerHub-openapi-v1.yml) file and is used to generate the api contents using [open-api-generator](https://openapi-generator.tech/)

## Local development

Full local development setup is available on this project.
First, copy the `.env.dist` file to `.env` and configure it to your platform:

    cp .env.dist .env

### Docker Compose support
This project contains a Docker Compose file named `compose.yaml`.
In this file, the following services have been defined: 

* mysql: [`mysql:latest`](https://hub.docker.com/_/mysql)

There is no need to start the docker compose manually. `spring-boot-docker-compose` will take care of it.

### Running the application
After building the project with `mvn clean compile` to let the Api contents be generated, you can run the application with:

    mvnw spring-boot:run
or

    mvn spring-boot:run
The application will be available on [http://localhost:8080](http://localhost:8080)
### Postman
Postman collection is available [here](local-dev-conf/postman/postman_collection.json)
You may import them in your postman and use them to test the application.

### Running the tests
Tests can be found [here](src/test/java/ad/lotfiz/assignment/customerhub). I developed unit + integration tests. The test coverage is 100%
![customerHub-test coverage.png](customerHub-test%20coverage.png)
Tests are using H2 database, while the application is using MySql database.
./mvn test

## Monitoring

Monitoring in this project is available using Prometheus and Grafana. The monitoring stack is available in the `docker-compose-monitoring.yml` file.
Endpoints are instrumented using Micrometer and Prometheus is configured to scrape metrics from the `/actuator/prometheus` endpoint.

### Local Monitoring

If you also want to develop monitoring dashboards in grafana locally you can run the `docker-compose-monitoring.yml` file:

    docker-compose -f docker-compose-monitoring.yml up -d

Prometheus will by default run on: [http://localhost:9090](http://localhost:9090) and grafana by default [http://localhost:3000](http://localhost:3000)


## CI/CD
Continuous integration is done using github actions. you can find the pipeline [here](.github/workflows/continuous-integration.yml) and [here](/.github/workflows/continuous-delivery.yml) and see it in action [here](https://github.com/mtlotfizad/customer-hub/actions)

### Images
After build the images of the project will be available in the docker hub [here](https://hub.docker.com/r/mohsenlzd/customer-data)



