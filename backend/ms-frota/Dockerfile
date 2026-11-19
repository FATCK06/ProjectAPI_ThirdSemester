# syntax=docker/dockerfile:1

# Imagem do ms-frota. Quem sobe e o backend/docker-compose.yml; este arquivo so
# diz como montar o servico.
#
# Dois estagios: o primeiro compila com o JDK e o Maven Wrapper do proprio
# servico; o segundo leva so o JRE e o resultado. JDK e Maven nao vao para a
# imagem final.

# ─── Build ───────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS build
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

# O cache do .m2 fica fora da imagem e e reaproveitado entre builds: so o
# primeiro baixa as dependencias. Testes ficam de fora (rodam na IDE ou no CI).
# "sh ./mvnw" porque o mvnw nem sempre chega executavel (clone no Windows).
RUN --mount=type=cache,target=/root/.m2 \
    sh ./mvnw -B -q package -DskipTests \
 && cp target/*.jar application.jar \
 && java -Djarmode=tools -jar application.jar extract --layers --destination extraido

# ─── Execucao ────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app

# Uma camada por grupo, da que menos muda para a que mais muda: alterar so o
# codigo reaproveita as camadas das dependencias no proximo build.
COPY --from=build /build/extraido/dependencies/ ./
COPY --from=build /build/extraido/spring-boot-loader/ ./
COPY --from=build /build/extraido/snapshot-dependencies/ ./
COPY --from=build /build/extraido/application/ ./

USER app
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "application.jar"]
