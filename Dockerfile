FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/accurib.jar /accurib/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/accurib/app.jar"]
