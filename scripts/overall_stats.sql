/* Successes by HPO Term/Negation */
select group_concat(distinct CASE 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu3' THEN 'HAPI3' 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu2' THEN 'HAPI2' 
	WHEN sr.`server_base`='https://syntheticmass.mitre.org/fhir' THEN 'MITRE' 
	WHEN sr.`server_base`='https://r3.smarthealthit.org' THEN 'R3' 
	WHEN sr.`server_base`='https://r2.smarthealthit.org' THEN 'R2' 
	WHEN sr.`server_base`='https://open-ic.epic.com/FHIR/api/FHIR/DSTU2' THEN 'EPIC' 
	ELSE sr.server_base END) as 'Servers', 
count(mr.id) as 'Count', group_concat(distinct mr.hpo_term_id) as 'Term Id', mr.hpo_term_name as 'Term Name', mr.negated as 'Negated' from method_result mr
	inner join stats_run_observation o on mr.observation = o.id
	inner join stats_run_patient p on p.id = o.patient
	inner join stats_run sr on p.`stats_run` = sr.id
where mr.hpo_term_id is not null group by mr.hpo_term_name, mr.negated order by count(mr.id) DESC;

/* Summary of Loinc Ids encountered */
select l.code as 'Code', count(distinct ol.id) as 'Count', group_concat(distinct ol.direct separator ';') as 'Direct', group_concat(distinct CASE 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu3' THEN 'HAPI3' 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu2' THEN 'HAPI2' 
	WHEN sr.`server_base`='https://syntheticmass.mitre.org/fhir' THEN 'MITRE' 
	WHEN sr.`server_base`='https://r3.smarthealthit.org' THEN 'R3' 
	WHEN sr.`server_base`='https://r2.smarthealthit.org' THEN 'R2' 
	WHEN sr.`server_base`='https://open-ic.epic.com/FHIR/api/FHIR/DSTU2' THEN 'EPIC' 
	ELSE sr.server_base END) as 'Servers' 
FROM `observation_loinc` ol
	join loinc l on l.id = ol.`loinc`
	join stats_run_observation sro on sro.id = ol.observation
	join stats_run_patient srp on srp.id = sro.patient
	join stats_run sr on srp.`stats_run` = sr.id
group by ol.loinc, l.code
order by count(distinct ol.id) desc, code;

/* Summary of Successes by Method */
select group_concat(distinct CASE 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu3' THEN 'HAPI3' 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu2' THEN 'HAPI2' 
	WHEN sr.`server_base`='https://syntheticmass.mitre.org/fhir' THEN 'MITRE' 
	WHEN sr.`server_base`='https://r3.smarthealthit.org' THEN 'R3' 
	WHEN sr.`server_base`='https://r2.smarthealthit.org' THEN 'R2' 
	WHEN sr.`server_base`='https://open-ic.epic.com/FHIR/api/FHIR/DSTU2' THEN 'EPIC' 
	ELSE sr.server_base END) as 'Servers', 
count(r.id) as 'Count', m.description as 'Method' from method_result r
	inner join method_type m on r.`method_type` = m.id
	inner join stats_run_observation o on r.observation = o.id
	inner join stats_run_patient p on p.id = o.patient
	inner join stats_run sr on p.`stats_run` = sr.id
where hpo_term_id is not null group by r.method_type, m.description;

/* Conversion Failures by Exception Type */
select group_concat(distinct CASE 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu3' THEN 'HAPI3' 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu2' THEN 'HAPI2' 
	WHEN sr.`server_base`='https://syntheticmass.mitre.org/fhir' THEN 'MITRE' 
	WHEN sr.`server_base`='https://r3.smarthealthit.org' THEN 'R3' 
	WHEN sr.`server_base`='https://r2.smarthealthit.org' THEN 'R2' 
	WHEN sr.`server_base`='https://open-ic.epic.com/FHIR/api/FHIR/DSTU2' THEN 'EPIC' 
	ELSE sr.server_base END) as 'Servers', 
count(o.id) as 'Count', e.`description` as 'Exception' from stats_run_observation o
	join exception_type e on o.exception_type = e.id
	inner join stats_run_patient p on p.id = o.patient
	inner join stats_run sr on p.`stats_run` = sr.id
group by o.exception_type, e.description;

/* Method Failures by Exception Type */
select group_concat(distinct CASE 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu3' THEN 'HAPI3' 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu2' THEN 'HAPI2' 
	WHEN sr.`server_base`='https://syntheticmass.mitre.org/fhir' THEN 'MITRE' 
	WHEN sr.`server_base`='https://r3.smarthealthit.org' THEN 'R3' 
	WHEN sr.`server_base`='https://r2.smarthealthit.org' THEN 'R2' 
	WHEN sr.`server_base`='https://open-ic.epic.com/FHIR/api/FHIR/DSTU2' THEN 'EPIC' 
	ELSE sr.server_base END) as 'Servers', 
count(r.id) as 'Count', e.`description` as 'Exception' from method_result r
	join exception_type e on r.exception_type = e.id
	inner join stats_run_observation o on r.observation = o.id
	inner join stats_run_patient p on p.id = o.patient
	inner join stats_run sr on p.`stats_run` = sr.id
group by r.exception_type, e.description;

/* % Success */
select CASE 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu3' THEN 'HAPI3' 
	WHEN sr.`server_base`='http://hapi.fhir.org/baseDstu2' THEN 'HAPI2' 
	WHEN sr.`server_base`='https://syntheticmass.mitre.org/fhir' THEN 'MITRE' 
	WHEN sr.`server_base`='https://r3.smarthealthit.org' THEN 'R3' 
	WHEN sr.`server_base`='https://r2.smarthealthit.org' THEN 'R2' 
	WHEN sr.`server_base`='https://open-ic.epic.com/FHIR/api/FHIR/DSTU2' THEN 'EPIC' 
	ELSE sr.server_base END as 'Servers', 
count(o.id) as 'Total Observations', count(sub1.annotated) as 'Num Annotated',
count(sub2.successful) as 'Num Successful',
count(sub1.annotated)/count(o.id) * 100 as '% Annotated',
count(sub2.successful)/count(sub1.annotated) * 100 as '% Success of Annotated'
 from `stats_run_observation` o
	left join (select distinct ao.id as 'annotated' from stats_run_observation ao inner join method_result r on ao.id = r.`observation`) as sub1 on sub1.annotated = o.id 
	left join (select distinct ao.id as 'successful' from stats_run_observation ao inner join method_result r on ao.id = r.`observation` where r.hpo_term_id is not null) as sub2 on sub2.successful = o.id 
	inner join stats_run_patient p on p.id = o.patient
	inner join stats_run sr on p.`stats_run` = sr.id
group by sr.id;

