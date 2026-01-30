#
# Copyright © 2025 Evolved Binary Ltd. (tech@evolvedbinary.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Use Eclipse Temurin JRE as base image
FROM eclipse-temurin:21-jre-alpine

# metadata
LABEL org.label-schema.description="BBL Validator" \
      org.label-schema.name="bbl-validator" \
      org.label-schema.vendor="Evolved Binary"
      
# Create a non-root user for running the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the JAR file and schemas from build
COPY maven/bbl-validator-${project.version}.jar /app/app.jar

# Change ownership to non-root user
RUN mkdir /app/schemas
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
