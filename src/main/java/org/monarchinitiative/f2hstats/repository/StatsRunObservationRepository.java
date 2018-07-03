package org.monarchinitiative.f2hstats.repository;

import org.monarchinitiative.f2hstats.domain.StatsRunObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatsRunObservationRepository extends JpaRepository<StatsRunObservation, Long> {

}

