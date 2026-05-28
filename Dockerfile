FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY src ./src
COPY web ./web

RUN mkdir -p build && \
    javac --release 8 -d build src/app/*.java src/model/*.java src/web/*.java

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build ./build
COPY --from=build /app/web ./web

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "build", "app.Main"]
