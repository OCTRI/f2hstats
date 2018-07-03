package org.monarchinitiative.f2hstats.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.monarchinitiative.f2hstats.domain.list.ExceptionType;

/**
 * An observation associated with a patient and run. The json fields records
 * the observation, and an exception may be associated if the observation could
 * not be converted.
 * 
 * @author yateam
 *
 */
@Entity
public class StatsRunObservation extends AbstractEntity {

	@ManyToOne
	@NotNull
	StatsRunPatient patient;

	@NotNull
	String fhirId;

	@Lob
	@Column(columnDefinition = "TEXT")
	String json;

	@ManyToOne
	ExceptionType exceptionType;

	public StatsRunPatient getPatient() {
		return patient;
	}

	public void setPatient(StatsRunPatient patient) {
		this.patient = patient;
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

	public ExceptionType getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(ExceptionType exceptionType) {
		this.exceptionType = exceptionType;
	}

}
