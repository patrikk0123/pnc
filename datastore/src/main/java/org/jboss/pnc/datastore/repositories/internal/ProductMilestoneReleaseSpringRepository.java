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
package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import javax.enterprise.context.Dependent;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/30/16 Time: 1:46 PM
 */
@Dependent
public interface ProductMilestoneReleaseSpringRepository
        extends JpaRepository<ProductMilestoneRelease, Integer>, JpaSpecificationExecutor<ProductMilestoneRelease> {

    @Query("select r from ProductMilestoneRelease r where r.id = (select max(id) from ProductMilestoneRelease o where o.milestone = ?1)")
    ProductMilestoneRelease findLatestForMilestone(ProductMilestone milestone);
}