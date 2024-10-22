/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.dto.DeliveredArtifactComparison;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.DeliverableAnalyzerOperation_;
import org.jboss.pnc.model.DeliverableAnalyzerReport_;
import org.jboss.pnc.model.DeliverableArtifact_;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;

import javax.ejb.Stateless;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
public class ProductMilestoneRepositoryImpl extends AbstractRepository<ProductMilestone, Integer>
        implements ProductMilestoneRepository {

    public ProductMilestoneRepositoryImpl() {
        super(ProductMilestone.class, Integer.class);
    }

    @Override
    public long countBuiltArtifactsInMilestone(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<Artifact> artifacts = query.from(Artifact.class);
        Join<BuildRecord, org.jboss.pnc.model.ProductMilestone> builtArtifactsMilestones = artifacts
                .join(Artifact_.buildRecord)
                .join(BuildRecord_.productMilestone);

        query.select(cb.count(artifacts.get(Artifact_.id)));
        query.where(cb.equal(builtArtifactsMilestones.get(ProductMilestone_.id), id));

        return entityManager.createQuery(query).getSingleResult();
    }

    // 111 SElECT
    // artifact.identifier as identifier,
    // SUBSTRING(artifact.identifier, 1, POSITION('.redhat-' IN artifact.identifier)) AS prefix,
    // SUM(CASE WHEN productmilestone.id = 1626 THEN 1 ELSE 0 END) AS milestone_1626,
    // SUM(CASE WHEN productmilestone.id = 2035 THEN 1 ELSE 0 END) AS milestone_2035,
    // COUNT(productmilestone.id) as in_all
    // FROM artifact
    // JOIN
    // deliverableartifact ON deliverableartifact.artifact_id = artifact.id
    // JOIN
    // deliverableanalyzerreport ON deliverableanalyzerreport.operation_id = deliverableartifact.report_id
    // JOIN
    // operation ON operation.id = deliverableanalyzerreport.operation_id
    // JOIN
    // productmilestone ON productmilestone.id = operation.productmilestone_id
    // WHERE
    // LENGTH(artifact.identifier) - LENGTH(REPLACE(artifact.identifier, ':', '')) < 4 AND
    // artifact.identifier LIKE '%.redhat-%' AND
    // productmilestone.id IN (1626, 2035)
    // GROUP BY identifier
    // ;

    // 222 SElECT
    // artifact.identifier as identifier,
    // SUBSTRING(artifact.identifier, 1, POSITION('.redhat-' IN artifact.identifier)) AS prefix,
    // SUM(CASE WHEN productmilestone.id = 1626 THEN 1 ELSE 0 END) AS milestone_1626,
    // SUM(CASE WHEN productmilestone.id = 2035 THEN 1 ELSE 0 END) AS milestone_2035,
    // COUNT(productmilestone.id) as in_all
    // FROM productmilestone
    // JOIN
    // product_milestone_delivered_artifacts_map ON product_milestone_delivered_artifacts_map.product_milestone_id =
    // productmilestone.id
    // JOIN
    // artifact ON artifact.id = product_milestone_delivered_artifacts_map.artifact_id
    // WHERE
    // LENGTH(artifact.identifier) - LENGTH(REPLACE(artifact.identifier, ':', '')) < 4 AND
    // artifact.identifier LIKE '%.redhat-%' AND
    // productmilestone.id IN (1626, 2035)
    // GROUP BY identifier
    // ;
    @Override
    public List<DeliveredArtifactComparison> compareDeliveredArtifacts(List<Integer> milestoneIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);

        /*
         * Root<ProductMilestone> pm = query.from(ProductMilestone.class); Join<ProductMilestone, Artifact> arts =
         * pm.join(ProductMilestone_.deliveredArtifacts);
         */

        Root<org.jboss.pnc.model.DeliverableAnalyzerOperation> deliverableAnalyzerOperations = query
                .from(org.jboss.pnc.model.DeliverableAnalyzerOperation.class);
        Join<DeliverableAnalyzerOperation, ProductMilestone> productMilestones = deliverableAnalyzerOperations
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<org.jboss.pnc.model.DeliverableArtifact, Artifact> artifacts = deliverableAnalyzerOperations
                .join(DeliverableAnalyzerOperation_.report)
                .join(DeliverableAnalyzerReport_.artifacts)
                .join(DeliverableArtifact_.artifact);

        List<Selection<?>> selects = new ArrayList<>();
        selects.add(artifacts.get(Artifact_.identifier));
        selects.add(
                cb.substring(
                        artifacts.get(Artifact_.identifier),
                        cb.literal(1),
                        cb.locate(artifacts.get(Artifact_.identifier), ".redhat-")));

        for (Integer milestoneId : milestoneIds) {
            CriteriaBuilder.SimpleCase<Integer, Integer> aCase = cb
                    .selectCase(productMilestones.get(ProductMilestone_.id));
            aCase.when(milestoneId, cb.literal(1));
            aCase.otherwise(cb.literal(0));
            selects.add(aCase);
        }

        query.multiselect(selects);
        query.where(
                cb.like(artifacts.get(Artifact_.identifier), "%:%:%:%.redhat-%"),
                cb.notLike(artifacts.get(Artifact_.identifier), "%:%:%:%:%"),
                productMilestones.get(ProductMilestone_.id).in(milestoneIds));
        query.groupBy(artifacts.get(Artifact_.identifier));

        List<Tuple> result = entityManager.createQuery(query).getResultList();
        for (Tuple tuple : result) {
            String identifier = tuple.get(0, String.class);
            String prefix = tuple.get(1, String.class);
            String identifier = tuple.get(0, String.class);
        }

        return null;
    }
};