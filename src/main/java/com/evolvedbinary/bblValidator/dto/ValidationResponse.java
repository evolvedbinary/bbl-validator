/*
 * Copyright © 2025 Evolved Binary Ltd. (tech@evolvedbinary.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolvedbinary.bblValidator.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Collections;
import java.util.List;

/**
 * Response object representing the result of a validation operation.
 */
@Serdeable
public final class ValidationResponse implements ResponseObject {

    private final boolean passed;
    private final List<ValidationFailure> failures;
    private final long executionTime;
    private final boolean utf8Valid;

    public ValidationResponse(final boolean passed,
                              final List<ValidationFailure> failures,
                              final long executionTime,
                              final boolean utf8Valid) {
        this.executionTime = executionTime;
        this.passed = passed;
        this.failures = failures != null ? failures : Collections.emptyList();
        this.utf8Valid = utf8Valid;
    }

    public boolean isPassed() {
        return passed;
    }   

    public List<ValidationFailure> getFailures() {
        return failures;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public boolean isUtf8Valid() { return utf8Valid; }

}
