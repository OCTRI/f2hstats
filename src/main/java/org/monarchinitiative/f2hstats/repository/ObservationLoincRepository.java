package org.monarchinitiative.f2hstats.repository;

import org.monarchinitiative.f2hstats.domain.ObservationLoinc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObservationLoincRepository extends JpaRepository<ObservationLoinc, Long> {

}

