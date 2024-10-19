
# Bygge

```bash	

cd DigLib-backend

rm -f docker/app.jar

./mvnw clean package

cd docker

cp ../target/*.jar ./app.jar

docker-compose up --build

```