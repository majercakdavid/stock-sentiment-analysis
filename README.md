# Running the docker images

  ## Kafka 
    1. `docker-compose -f docker-compose-kafka.yml up -d --build`
    2. `docker-compose -f docker-compose-kafka.yml ps`
    3. Open: http://localhost:9021/

  ## Elasticsearch + Kibana
    1. `docker-compose -f docker-compose-elk.yml up -d --build`
    2. `docker-compose -f docker-compose-elk.yml ps`
    3. Open: http://localhost:9200/ for Elastic search and http://localhost:5601/ for Kibana