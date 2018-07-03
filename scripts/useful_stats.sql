-- Successes by HPO Term/Negation
select count(*) as 'Count', group_concat(distinct hpo_term_id) as 'Term Id', hpo_term_name as 'Name', negated as 'Negated' from method_result where `hpo_term_id` is not null group by hpo_term_name, negated order by count(*) DESC;;

-- Successes by Method
select count(*) as 'Count', m.description from method_result r
	inner join method_type m on r.`method_type` = m.id
where hpo_term_id is not null group by method_type;

-- Conversion Failures by Exception Type
select count(*) as 'Count', e.`description` from stats_run_observation o
	join exception_type e on o.exception_type = e.id
group by o.exception_type

-- Method Failures by Exception Type
select count(*) as 'Count', e.`description` from method_result m
	join exception_type e on m.exception_type = e.id
group by m.exception_type

-- % Success of Observations with LOINCs that we've annotated
set @success_count = (select s.success_count from (
select count(distinct o.id) as 'success_count' from `stats_run_observation` o
inner join method_result r on r.`observation` = o.id
where r.hpo_term_id is not null) as s);

set @annotated_count = (select s.annotated_observation_count from (
select count(distinct o.id) as 'annotated_observation_count' from `stats_run_observation` o
inner join method_result r on r.`observation` = o.id
) as s);

select @success_count as 'Successfully Converted Observations' , @annotated_count as 'Num Annotated', @success_count/@annotated_count * 100 as '% Success';
