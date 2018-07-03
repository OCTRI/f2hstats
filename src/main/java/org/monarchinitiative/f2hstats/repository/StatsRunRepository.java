package org.monarchinitiative.f2hstats.repository;

import org.monarchinitiative.f2hstats.domain.StatsRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatsRunRepository extends JpaRepository<StatsRun, Long> {

}

