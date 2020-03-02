/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.causewayclient.remotespi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Objects;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(value = "npm")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = NpmBuiltArtifact.NpmBuiltArtifactBuilder.class)
public class NpmBuiltArtifact extends BuiltArtifact {

    @NonNull
    private final String name;
    @NonNull
    private final String version;

    @Builder
    public NpmBuiltArtifact(
            String name,
            String version,
            int id,
            String filename,
            String architecture,
            String md5,
            String artifactPath,
            String repositoryPath,
            int size) {
        super(id, filename, architecture, md5, artifactPath, repositoryPath, size);
        this.name = Objects.requireNonNull(name, "Artifact name must be set");
        this.version = Objects.requireNonNull(version, "Artifact version must be set");
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class NpmBuiltArtifactBuilder {

    }
}