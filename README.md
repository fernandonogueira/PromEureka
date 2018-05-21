# PromEureka

Simple Prometheus integration to work with Eureka using `json` files.

### Running

Run the `jar` adding `EUREKA_SERVICE_URL` and `JSON_FILE_PATH` environment variables.

For instance:
```bash
java -DEUREKA_SERVICE_URL=http://YOUR_EUREKA_ADDRESS \
     -DJSON_FILE_PATH=/opt/prometheus_config/discovery \
     -jar target/promeureka.jar
```

Or just create the docker container, and run it.

For instance:
```bash
docker build -t fernandonogueira/promeureka:1.0.4 . 

docker run -p 127.0.0.1:8080:8080/tcp \
           -e EUREKA_SERVICE_URL=http://YOUR_EUREKA_ADDRESS \
           -r JSON_FILE_PATH=/opt/prometheus_config/discovery \
           fernandonogueira/promeureka:1.0.4
```

### Sample Prometheus Configuration

```yaml
# my global config
global:
  scrape_interval:     15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).
  
scrape_configs:
  - job_name: 'eureka'
    metrics_path: '/management/prometheus'
    file_sd_configs:
      - files:
        - /opt/prometheus_config/discovery/*.json
        refresh_interval: 5m
```

Using this configuration, Prometheus will look for files ending with `.json` stored on the 
specified directory (`/opt/prometheus_config/discovery/`). 