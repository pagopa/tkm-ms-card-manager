echo 'Waiting for DB to be operational...'
sleep 100s

./liquibase/liquibase --changeLogFile=./changelogs/card-manager/master-changelog.xml --url="jdbc:postgresql://host.docker.internal:5001/tkm_card_manager" --username=tkm_card_manager --password=tkm_card_manager --contexts="tag,baseline,insert,incremental,insert-dev,incremental-dev" --log-level=INFO --driver=org.postgresql.Driver --classpath=./liquibase/postgresql-42.5.0.jar update