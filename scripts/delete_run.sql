-- Delete the run id and all associated data
set @run = 2;

-- Nothing below needs to change
set @min_pat_id = (select min(id) from stats_run_patient where `stats_run` = @run);
set @max_pat_id = (select max(id) from stats_run_patient where `stats_run` = @run);

set @min_ob_id = (select min(id) from stats_run_observation where `patient` >= @min_pat_id and patient <= @max_pat_id);
set @max_ob_id = (select max(id) from stats_run_observation where `patient` >= @min_pat_id and patient <= @max_pat_id);

delete from method_result where observation >= @min_ob_id && observation <= @max_ob_id;
delete from stats_run_observation where id >= @min_ob_id && id <= @max_ob_id;
delete from stats_run_patient where id >= @min_pat_id && id <= @max_pat_id;
delete from stats_run where id = @run;
