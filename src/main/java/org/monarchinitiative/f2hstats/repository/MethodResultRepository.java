package org.monarchinitiative.f2hstats.repository;

import org.monarchinitiative.f2hstats.domain.MethodResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MethodResultRepository extends JpaRepository<MethodResult, Long> {

}

