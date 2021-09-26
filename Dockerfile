FROM openjdk:11-jre-slim
VOLUME /tmp
ARG JAR_FILE
ARG DB_URI
ARG DB_USER
ARG DB_PASSWORD
ENV JDBC_DATABASE_URL=$DB_URI
ENV JDBC_DATABASE_USERNAME=$DB_USER
ENV JDBC_DATABASE_PASSWORD=$DB_PASSWORD
COPY ${JAR_FILE} app.jar
CMD java -Djava.security.egd=file:/dev/./urandom -Dserver.port=$PORT -Dspring.profiles.active=prod -jar /app.jar
