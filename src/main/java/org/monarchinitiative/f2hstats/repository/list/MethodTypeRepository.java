package org.monarchinitiative.f2hstats.repository.list;

import org.monarchinitiative.f2hstats.domain.list.MethodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MethodTypeRepository extends JpaRepository<MethodType, Long> {

	public MethodType findByDescription(@Param("description") String description);
}

