
#Bygge

```bash	
rm -f app.jar

./mvnw clean package

cp ../target/*.jar ./app.jar

docker-compose up --build

```