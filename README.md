
# Card Manager  
The microservice fulfills the role of holder of all information and relations between citizens, cards and associated tokens.  
    
 ## Configuration  
  The follow ENVIRONMENT variables need to deploy and run the application.  
   
- **DB_SERVER** *The database connection i.e. localhost:5000/tkm_card_manager*  
- **SERVER_PORT** *The spring boot port. Default value 8080*  
- **KAFKA_APPENDER_BOOTSTRAP_SERVERS** *The address of kafka broker connection i.e. localhost:9093*  
- **ENABLE_KAFKA_APPENDER** *Uppercase boolean value that indicates if the logs is sent to the specific queue*  
- **KAFKA_APPENDER_TOPIC** *if ENABLE_KAFKA_APPENDER=TRUE is the queue topic name*  
- **AZURE_KEYVAULT_PROFILE** *The prefix used to search for keys in the key vault (local/sit/uat/prod)*  
- **KAFKA_APPENDER_SECURITY_PROTOCOL** *Kafka security protocol. Default: SASL_SSL*  
- **AZURE_KEYVAULT_CLIENT_ID** *Azure Kevault authentication client id*  
- **AZURE_KEYVAULT_CLIENT_KEY** *Azure Kevault authentication client key*  
- **AZURE_KEYVAULT_TENANT_ID** *Azure Kevault authentication tenant id*  
- **AZURE_KEYVAULT_URI** *Azure Kevault address* 
- **RTD_HASHING_URL** *PM url to obtain the hash i.e. localhost:8080*  
- **CONSENT_MANAGER_URL** *Consent manager url localhost:8080*  
- **KAFKA_WRITE_QUEUE_TOPIC** *Topic name of write queue*  
- **KAFKA_READ_QUEUE_GROUP** *Group id of read queue*  
- **KAFKA_DELETE_QUEUE_TOPIC** *Topic name of delete queue*  
- **KAFKA_DELETE_QUEUE_GROUP** *Group id of delete queue*  
- **KAFKA_READ_QUEUE_GROUP** *Topic name of read queue*  
  
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