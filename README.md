# Card Manager
This microservice holds all information about cards and the relations between cards, citizens and card tokens.

## Configuration
The following ENVIRONMENT variables are needed to deploy and run the application.

- **DB_SERVER** *The database connection i.e. localhost:5000/tkm_card_manager*
- **SERVER_PORT** *The spring boot port. Default value 8080*
- **ENABLE_KAFKA_APPENDER** *Uppercase boolean value that indicates if the logs is sent to the specific queue*
- **KAFKA_APPENDER_BOOTSTRAP_SERVERS** *The address of kafka broker connection i.e. localhost:9093*
- **KAFKA_APPENDER_TOPIC** *if ENABLE_KAFKA_APPENDER=TRUE is the queue topic name*
- **KAFKA_APPENDER_SECURITY_PROTOCOL** *Kafka security protocol. Default: SASL_SSL*
- **KAFKA_WRITE_QUEUE_TOPIC** *Topic name of write queue*
- **KAFKA_READ_QUEUE_GROUP** *Group id of read queue*
- **KAFKA_READ_MAX_NUMBER_OF_THREADS** *Parallel read queue consumer. Default: 1*
- **KAFKA_DELETE_QUEUE_TOPIC** *Topic name of delete queue*
- **KAFKA_DELETE_QUEUE_GROUP** *Group id of delete queue*
- **KAFKA_DELETE_MAX_NUMBER_OF_THREADS** *Parallel delete queue consumer. Default: 1*
- **KAFKA_READ_QUEUE_GROUP** *Topic name of read queue*
- **AZURE_KEYVAULT_PROFILE** *The prefix used to search for keys in the key vault (local/sit/uat/prod)*
- **AZURE_KEYVAULT_CLIENT_ID** *Azure Kevault authentication client id*
- **AZURE_KEYVAULT_CLIENT_KEY** *Azure Kevault authentication client key*
- **AZURE_KEYVAULT_TENANT_ID** *Azure Kevault authentication tenant id*
- **AZURE_KEYVAULT_URI** *Azure Kevault address*
- **AZURE_STORAGE_ENDPOINT** *Endpoint where blob storage connects*
- **RTD_HASHING_URL** *PM url to obtain the hash i.e. localhost:8080*
- **CONSENT_MANAGER_URL** *Consent manager url localhost:8080*
- **VISA_URL** *URL of Visa circuit service*

### Develop enviroment configuration
- Set **-Dspring.profiles.active=local** as jvm setting
- Add as enviroment variable **AZURE_KEYVAULT_CLIENT_ID=~~VALUE_TO_ADD~~;AZURE_KEYVAULT_CLIENT_KEY=~~VALUE_TO_ADD~~;AZURE_KEYVAULT_TENANT_ID=~~VALUE_TO_ADD~~;AZURE_KEYVAULT_URI=~~VALUE_TO_ADD~~**

## How to start SIT azure pipeline

1. Move into:
> develop

1. Run:<br>
   `$version=??` for poweshell or `version=??` for gitbash<br>
   `mvn --batch-mode release:clean release:prepare`<br>
   `git checkout -b tmp/${version} card-manager-${version}`<br>
   `git push --set-upstream origin tmp/${version}`<br>

2. Merge **tmp/${version}** into **release/sit**

## How to start UAT azure pipeline

1. Merge **release/sit** into **release/uat**

## How to start PROD azure pipeline

1. Merge **release/uat** into **master**