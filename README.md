* create a folder postgres under data
* start all required services (MongoDb, Postgres, Prometheus, Grafan) with docker compose up
* start the two services sync and monitor (you may use your IDE or mvn)
* create a sample user:  curl -X POST http://localhost:8080/create-user
* connect to the relational database and have a look at the content of the table user
