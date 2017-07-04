/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * The JPA entity class that contains configuration of the SCM repositories.
 *
 * @author Jakub Bartecek
 */
@Entity
public class RepositoryConfiguration implements GenericEntity<Integer> {

    private static final long serialVersionUID = 4248038054068607536L;

    private static final String SEQ_NAME = "repository_configuration_id_seq";

    @Id
    @SequenceGenerator(name = SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    private Integer id;

    /**
     * URL to the internal SCM repository, which is the main repository used for the builds.
     * New commits can be added to this repository, during the pre-build steps of the build process.
     */
    @Size(max = 255)
    @Column(unique = true, nullable = false, updatable = false)
    @Getter
    @Setter
    private String internalUrl;

    /**
     * URL to the upstream SCM repository.
     */
    @Size(max = 255)
    @Getter
    @Setter
    private String externalUrl;

    /**
     * Declares whether the pre-build repository synchronization should happen or not.
     */
    @Getter
    @Setter
    private boolean preBuildSyncEnabled = true;

    @OneToMany(mappedBy = "repositoryConfiguration")
    @Getter
    @Setter
    private Set<BuildConfiguration> buildConfigurations = new HashSet<>();

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepositoryConfiguration)) return false;

        RepositoryConfiguration that = (RepositoryConfiguration) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public static class Builder {
        private Integer id;

        private String internalUrl;

        private String externalUrl;

        private boolean preBuildSyncEnabled = true;

        private Set<BuildConfiguration> buildConfigurations = new HashSet<>();

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public RepositoryConfiguration build() {
            RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
            repositoryConfiguration.setId(id);
            repositoryConfiguration.setInternalUrl(internalUrl);
            repositoryConfiguration.setExternalUrl(externalUrl);
            repositoryConfiguration.setPreBuildSyncEnabled(preBuildSyncEnabled);
            repositoryConfiguration.setBuildConfigurations(buildConfigurations);
            return repositoryConfiguration;
        }

        public Builder internalUrl(String internalUrl) {
            this.internalUrl = internalUrl;
            return this;
        }

        public Builder externalUrl(String externalUrl) {
            this.externalUrl = externalUrl;
            return this;
        }

        public Builder preBuildSyncEnabled(boolean preBuildSyncEnabled) {
            this.preBuildSyncEnabled = preBuildSyncEnabled;
            return this;
        }

        public Builder buildConfigurations(Set<BuildConfiguration> buildConfigurations) {
            this.buildConfigurations = buildConfigurations;
            return this;
        }
    }
}
