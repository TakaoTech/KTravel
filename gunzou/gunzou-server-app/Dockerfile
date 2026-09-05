# :gunzou-server, packaged for a container runtime.
#
# Two stages so the image carries a runtime and a jar and nothing else: no Gradle, no sources, no
# build cache. The build stage is the expensive one and it is cached on the dependency layer, so
# editing a source file does not re-download the world.
#
# Built and run by hand:
#   docker build -f gunzou/gunzou-server-app/Dockerfile -t gunzou-server .
#   docker run --rm -p 8080:8080 \
#     -e NAVIGATOR_ACCESS_TOKENS=a-token \
#     -e NAVIGATOR_PROVIDER_API_KEY=$HERE_API_KEY \
#     gunzou-server
#
# The Ktor Gradle plugin can also do this on its own (`:gunzou-server-app:buildImage`). This file
# exists because it is what a deployment platform expects to be handed, and because it pins the base
# images rather than inheriting whatever the plugin defaults to this month.

FROM amazoncorretto:25 AS build
WORKDIR /src

# The wrapper and the build definition first, so the dependency resolution layer survives a source
# change. Everything the build touches has to be listed: an absent settings.gradle.kts fails late
# and confusingly.
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY config ./config
COPY gunzou/gunzou-api/build.gradle.kts ./gunzou/gunzou-api/
COPY gunzou/gunzou-server/build.gradle.kts ./gunzou/gunzou-server/
COPY gunzou/gunzou-server-app/build.gradle.kts ./gunzou/gunzou-server-app/
COPY gunzou/gunzou-here-client/build.gradle.kts ./gunzou/gunzou-here-client/

COPY gunzou ./gunzou

# Only the deployable module and what it needs. `--no-daemon` because a daemon that outlives the
# build stage is a process holding memory in a layer that is about to be thrown away.
RUN ./gradlew --no-daemon :gunzou-server-app:shadowJar -x detekt

FROM amazoncorretto:25-alpine AS runtime

# Not root. A routing server needs to bind one port and read its environment, and nothing it does
# needs the privileges that come with uid 0 if it is ever made to run something it should not.
RUN addgroup -S navigator && adduser -S -G navigator navigator
USER navigator

WORKDIR /app
COPY --from=build /src/gunzou/gunzou-server-app/build/libs/gunzou-server-app-all.jar ./navigator.jar

# The platform usually assigns one through $PORT; 8080 is what application.conf falls back to.
EXPOSE 8080

# Liveness only, which is what this endpoint reports: it deliberately says nothing about whether the
# routing providers behind it are reachable, so a provider outage does not get the instance killed
# and restarted into the same outage.
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://127.0.0.1:${PORT:-8080}/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/navigator.jar"]
