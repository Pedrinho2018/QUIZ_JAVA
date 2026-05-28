FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY src ./src
COPY web ./web
COPY data ./data
COPY lib ./lib

RUN mkdir -p build && \
    javac -encoding UTF-8 --release 8 -cp lib/sqlite-jdbc.jar -d build src/app/*.java src/model/*.java src/repository/*.java src/web/*.java

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build ./build
COPY --from=build /app/web ./web
COPY --from=build /app/data ./data
COPY --from=build /app/lib ./lib

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "build:lib/sqlite-jdbc.jar", "app.Main"]
