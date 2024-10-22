-- #1a
-- EXPLAIN ANALYZE
SElECT
    SUBSTRING(artifact.identifier, 1, POSITION('.redhat-' IN artifact.identifier)) AS prefix,
    CASE WHEN productmilestone.id = 1626 THEN SUBSTRING(artifact.identifier, 'redhat-(.+)$') END AS milestone_1626_ver,
    CASE WHEN productmilestone.id = 2035 THEN SUBSTRING(artifact.identifier, 'redhat-(.+)$') END AS milestone_2035_ver
FROM artifact
JOIN
    deliverableartifact ON deliverableartifact.artifact_id = artifact.id
JOIN
    deliverableanalyzerreport ON deliverableanalyzerreport.operation_id = deliverableartifact.report_id
JOIN
    operation ON operation.id = deliverableanalyzerreport.operation_id
JOIN
    productmilestone ON productmilestone.id = operation.productmilestone_id
WHERE
    artifact.identifier ~ '^[^:]+:[^:]+:[^:]+:\d.\d.\d.redhat(?!.*[:].*)' AND
    productmilestone.id IN (1626, 2035)
GROUP BY prefix
HAVING
    COUNT(DISTINCT productmilestone.id) >= 2
-- ORDER BY prefix
;

-- #1b
SElECT
    SUBSTRING(artifact.identifier FROM '^[^:]+:[^:]+:[^:]+:\d.\d.\d.redhat') AS prefix,
    COUNT(CASE WHEN productmilestone.id = 1626 THEN SUBSTRING(artifact.identifier, 'redhat-(.+)$') END) AS milestone_1626_ver,
    COUNT(CASE WHEN productmilestone.id = 2035 THEN SUBSTRING(artifact.identifier, 'redhat-(.+)$') END) AS milestone_2035_ver
FROM artifact
JOIN
    deliverableartifact ON deliverableartifact.artifact_id = artifact.id
JOIN
    deliverableanalyzerreport ON deliverableanalyzerreport.operation_id = deliverableartifact.report_id
JOIN
    operation ON operation.id = deliverableanalyzerreport.operation_id
JOIN
    productmilestone ON productmilestone.id = operation.productmilestone_id
WHERE
    artifact.identifier ~ '^[^:]+:[^:]+:[^:]+:\d.\d.\d.redhat(?!.*[:].*)' AND
    productmilestone.id IN (1626, 2035)
GROUP BY prefix
HAVING
    COUNT(DISTINCT productmilestone.id) >= 2
;

-- #1c
SElECT
    artifact.identifier as identifier,
    SUBSTRING(artifact.identifier, 1, POSITION('.redhat-' IN artifact.identifier)) AS prefix,
    SUM(CASE WHEN productmilestone.id = 1626 THEN 1 ELSE 0 END) AS milestone_1626,
    SUM(CASE WHEN productmilestone.id = 2035 THEN 1 ELSE 0 END) AS milestone_2035,
    COUNT(productmilestone.id) as in_all
FROM productmilestone
JOIN
     product_milestone_delivered_artifacts_map ON product_milestone_delivered_artifacts_map.product_milestone_id = productmilestone.id
JOIN
     artifact ON artifact.id = product_milestone_delivered_artifacts_map.artifact_id
WHERE
    LENGTH(artifact.identifier) - LENGTH(REPLACE(artifact.identifier, ':', '')) < 4 AND
    artifact.identifier LIKE '%.redhat-%' AND
    productmilestone.id IN (1626, 2035)
GROUP BY identifier
;

-- #2
WITH aggregated AS (
    SELECT
        SUBSTRING(identifier FROM '^[^:]+:[^:]+:[^:]+:\d.\d.\d.redhat-') AS prefix,
        STRING_AGG(CASE WHEN productmilestone.id = 1626 THEN SUBSTRING(artifact.identifier, 'redhat-(.+)$') END, ', ') AS milestone_1626_ver,
        STRING_AGG(CASE WHEN productmilestone.id = 2035 THEN SUBSTRING(artifact.identifier, 'redhat-(.+)$') END, ', ') AS milestone_2035_ver
    FROM artifact
    JOIN deliverableartifact ON deliverableartifact.artifact_id = artifact.id
    JOIN deliverableanalyzerreport ON deliverableanalyzerreport.operation_id = deliverableartifact.report_id
    JOIN operation ON operation.id = deliverableanalyzerreport.operation_id
    JOIN productmilestone ON productmilestone.id = operation.productmilestone_id
    WHERE productmilestone.id IN (1626, 2035)
    GROUP BY prefix
    HAVING COUNT(DISTINCT productmilestone.id) >= 2 AND COUNT(productmilestone.id) < 5
)
SELECT
    prefix,
    'milestone_1626' AS milestone_type,
    unnest(string_to_array(milestone_1626_ver, ', ')) AS milestone
FROM aggregated
UNION ALL
SELECT
    prefix,
    'milestone_2035' AS milestone_type,
    unnest(string_to_array(milestone_2035_ver, ', ')) AS milestone
FROM aggregated
ORDER BY prefix, milestone_type, milestone;

-- #3
WITH aggregated AS (
    SELECT
        SUBSTRING(identifier FROM '^[^:]+:[^:]+:[^:]+:\d.\d.\d.redhat-') AS prefix,
        STRING_AGG(CASE WHEN productmilestone.id = 1626 THEN SUBSTRING(artifact.identifier, 'redhat-(.+)$') END, ', ') AS milestone_1626_ver,
        STRING_AGG(CASE WHEN productmilestone.id = 2035 THEN SUBSTRING(artifact.identifier, 'redhat-(.+)$') END, ', ') AS milestone_2035_ver
    FROM artifact
    JOIN deliverableartifact ON deliverableartifact.artifact_id = artifact.id
    JOIN deliverableanalyzerreport ON deliverableanalyzerreport.operation_id = deliverableartifact.report_id
    JOIN operation ON operation.id = deliverableanalyzerreport.operation_id
    JOIN productmilestone ON productmilestone.id = operation.productmilestone_id
    WHERE productmilestone.id IN (1626, 2035)
    GROUP BY prefix
    HAVING COUNT(DISTINCT productmilestone.id) >= 2 AND COUNT(productmilestone.id) < 5
)
SELECT
    prefix,
    unnest(string_to_array(milestone_1626_ver, ', ')) AS milestone_1626,
    NULL AS milestone_2035
FROM aggregated
UNION ALL
SELECT
    prefix,
    NULL AS milestone_1626,
    unnest(string_to_array(milestone_2035_ver, ', ')) AS milestone_2035
FROM aggregated
ORDER BY prefix, milestone_1626, milestone_2035;
