package org.monarchinitiative.f2hstats.repository.list;

import org.monarchinitiative.f2hstats.domain.list.ExceptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExceptionTypeRepository extends JpaRepository<ExceptionType, Long> {

	public ExceptionType findByDescription(@Param("description") String description);
}

