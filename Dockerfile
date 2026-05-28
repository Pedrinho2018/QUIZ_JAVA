FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

ARG SQLITE_JDBC_VERSION=3.46.1.3

COPY src ./src
COPY web ./web
COPY data ./data

ADD https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/${SQLITE_JDBC_VERSION}/sqlite-jdbc-${SQLITE_JDBC_VERSION}.jar /app/sqlite-jdbc.jar

RUN mkdir -p build && \
    javac -encoding UTF-8 --release 8 -cp sqlite-jdbc.jar -d build src/app/*.java src/model/*.java src/repository/*.java src/web/*.java

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build ./build
COPY --from=build /app/web ./web
COPY --from=build /app/data ./data
COPY --from=build /app/sqlite-jdbc.jar ./sqlite-jdbc.jar

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "build:sqlite-jdbc.jar", "app.Main"]
