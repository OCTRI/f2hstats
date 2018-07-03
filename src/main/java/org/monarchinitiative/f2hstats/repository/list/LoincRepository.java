package org.monarchinitiative.f2hstats.repository.list;

import org.monarchinitiative.f2hstats.domain.list.Loinc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoincRepository extends JpaRepository<Loinc, Long> {

	public Loinc findByCode(@Param("code") String code);
}

