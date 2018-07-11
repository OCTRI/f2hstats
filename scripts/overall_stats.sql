/* Successes by HPO Term/Negation */
SELECT group_concat(DISTINCT sr.server) AS 'Servers', count(mr.id) AS 'Count', 
	group_concat(DISTINCT mr.hpo_term_id) AS 'Term Id', mr.hpo_term_name AS 'Term Name', 
	mr.negated AS 'Negated'
FROM method_result mr
	INNER JOIN stats_run_observation o ON mr.observation = o.id
	INNER JOIN stats_run_patient p ON p.id = o.patient
	INNER JOIN stats_run sr ON p.`stats_run` = sr.id
WHERE mr.hpo_term_id IS NOT NULL
GROUP BY mr.hpo_term_name, mr.negated 
ORDER BY count(mr.id) DESC;

/* Summary of Loinc Ids encountered */
SELECT l.code AS 'Code', count(DISTINCT ol.id) AS 'Count', group_concat(DISTINCT ol.direct separator ';') AS 'Direct', 
 group_concat(DISTINCT sr.server) AS 'Servers' 
FROM `observation_loinc` ol
	INNER JOIN loinc l ON l.id = ol.`loinc`
	INNER JOIN stats_run_observation sro ON sro.id = ol.observation
	INNER JOIN stats_run_patient srp ON srp.id = sro.patient
	INNER JOIN stats_run sr ON srp.`stats_run` = sr.id
GROUP BY ol.loinc, l.code
ORDER BY count(DISTINCT ol.id) DESC, code;

/* Summary of Successes by Method */
SELECT group_concat(DISTINCT sr.server) AS 'Servers', count(r.id) AS 'Count', m.description AS 'Method' 
FROM method_result r
	INNER JOIN method_type m ON r.`method_type` = m.id
	INNER JOIN stats_run_observation o ON r.observation = o.id
	INNER JOIN stats_run_patient p ON p.id = o.patient
	INNER JOIN stats_run sr ON p.`stats_run` = sr.id
WHERE hpo_term_id IS NOT NULL
GROUP BY r.method_type, m.description;

/* Conversion Failures by Exception Type */
SELECT group_concat(DISTINCT sr.server) AS 'Servers', count(o.id) AS 'Count', e.`description` AS 'Exception' 
FROM stats_run_observation o
	INNER JOIN exception_type e ON o.exception_type = e.id
	INNER JOIN stats_run_patient p ON p.id = o.patient
	INNER JOIN stats_run sr ON p.`stats_run` = sr.id
GROUP BY o.exception_type, e.description;

/* Method Failures by Exception Type */
SELECT group_concat(DISTINCT sr.server) AS 'Servers', count(r.id) AS 'Count', e.`description` AS 'Exception' 
FROM method_result r
	INNER JOIN exception_type e ON r.exception_type = e.id
	INNER JOIN stats_run_observation o ON r.observation = o.id
	INNER JOIN stats_run_patient p ON p.id = o.patient
	INNER JOIN stats_run sr ON p.`stats_run` = sr.id
GROUP BY r.exception_type, e.description;

/* % Success */
SELECT sr.server AS 'Server', count(o.id) AS 'Total Observations', count(sub1.annotated) AS 'Num Annotated',
	count(sub2.successful) AS 'Num Successful', count(sub1.annotated)/count(o.id) * 100 AS '% Annotated',
	count(sub2.successful)/count(sub1.annotated) * 100 AS '% Success of Annotated'
FROM `stats_run_observation` o
	LEFT JOIN (SELECT DISTINCT ao.id as 'annotated' FROM stats_run_observation ao INNER JOIN method_result r ON ao.id = r.`observation`) AS sub1 ON sub1.annotated = o.id 
	LEFT JOIN (SELECT DISTINCT ao.id as 'successful' FROM stats_run_observation ao INNER JOIN method_result r ON ao.id = r.`observation` WHERE r.hpo_term_id is not null) AS sub2 ON sub2.successful = o.id 
	INNER JOIN stats_run_patient p ON p.id = o.patient
	INNER JOIN stats_run sr ON p.`stats_run` = sr.id
GROUP BY sr.id;

