package org.monarchinitiative.f2hstats.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * A patient associated with a run. The json fields records the patient if FHIR STU3 format.
 * 
 * @author yateam
 *
 */
@Entity
public class StatsRunPatient extends AbstractEntity {

	@ManyToOne
	@NotNull
	StatsRun statsRun;

	@NotNull
	String fhirId;

	@Lob
	@Column(columnDefinition = "TEXT")
	String json;

	public StatsRun getStatsRun() {
		return statsRun;
	}

	public void setStatsRun(StatsRun statsRun) {
		this.statsRun = statsRun;
	}

	public String getFhirId() {
		return fhirId;
	}

	public void setFhirId(String fhirId) {
		this.fhirId = fhirId;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

}
