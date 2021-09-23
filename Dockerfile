FROM  library/openjdk:11-jdk

ADD cestzam-ws/target/cestzam-ws-thorntail.jar /app.jar
ADD runtime/thorntail/config.yml /default-config.yml

CMD java -jar /app.jar -s/default-config.yml
