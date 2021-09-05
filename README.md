* start all required services (MongoDb, Postgres, Prometheus, Grafan) with docker compose up
* start the two services sync and monitor (you may use your IDE or mvn)
* create a sample user:  curl -X POST http://localhost:8080/create-user
* connect to the relational database and have a look at the content of the table user
* have a look at http://localhost:3000/d/msm/mongo-streams-monitoring to view the grafana dashboard
* under http://localhost:9090/alerts you'll find the alerts

Teh corresponding medium article: https://medium.com/@mseemann.io/synchronize-a-mongodb-collection-to-a-relational-database-with-eventual-consistency-b31346f17128