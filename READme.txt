To run the application you need to start zookeeper :
bin/zookeeper-server-start.bat config/zookeeper.properties

(windows)
bin\windows\zookeeper-server-start.bat config\zookeeper.properties

Then you need to start in another terminal the kafka server:
bin/kafka-server-start.bat config/server.properties

(windows)
bin\windows\kafka-server-start.bat config\server.properties

then in another terminal you go to the root directory and you run the consumer :
sbt "runMain com.Drone.Consumer.DroneConsumerApp"

then in the another terminal you go to the root directory and you run the producer :
sbt "runMain com.Drone.Producer.DroneProducerApp"

A swing application will pop-up you have to setup your stream option and you can press Start.
2 json files will be created a file containing all the data of drones and a file with the alert


for the data ingestion you have to put the csv file in the root directory of the project
then run in the another terminal you go to the root directory and you run the producer csv ingestion :

sbt "runMain com.Drone.Producer.CSVProducerApp"