# Use Eclipse Temurin JRE as base image
FROM eclipse-temurin:21-jre-alpine

# metadata
LABEL org.label-schema.build-date=${maven.build.timestamp} \
      org.label-schema.description="BBL Validator" \
      org.label-schema.name="bbl-validator" \
      org.label-schema.url="${project.url}" \
      org.label-schema.vcs-ref=${build-commit-abbrev} \
      org.label-schema.vcs-url="${project.scm.url}" \
      org.label-schema.vendor="Evolved Binary"
      
# Create a non-root user for running the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the JAR file from Maven assembly
# The docker-maven-plugin with assembly descriptor 'artifact' places the JAR in maven/
COPY maven/*.jar /app/app.jar

# Copy schemas directory
COPY src/main/resources/schemas ./schemas

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Set JVM options
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=5m --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/version || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
