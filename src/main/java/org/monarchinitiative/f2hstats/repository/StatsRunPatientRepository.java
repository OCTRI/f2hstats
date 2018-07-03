package org.monarchinitiative.f2hstats.repository;

import org.monarchinitiative.f2hstats.domain.StatsRunPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatsRunPatientRepository extends JpaRepository<StatsRunPatient, Long> {

}

