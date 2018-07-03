package org.monarchinitiative.f2hstats.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.monarchinitiative.f2hstats.domain.list.ExceptionType;
import org.monarchinitiative.f2hstats.domain.list.MethodType;

/**
 * The method tried and the result
 * 
 * @author yateam
 *
 */
@Entity
public class MethodResult extends AbstractEntity {

	@NotNull
	@ManyToOne
	StatsRunObservation observation;

	@NotNull
	@ManyToOne
	MethodType methodType;

	@ManyToOne
	ExceptionType exceptionType;

	String hpoTermId;

	String hpoTermName;

	Boolean negated;

	public StatsRunObservation getObservation() {
		return observation;
	}

	public void setObservation(StatsRunObservation observation) {
		this.observation = observation;
	}

	public MethodType getMethodType() {
		return methodType;
	}

	public void setMethodType(MethodType methodType) {
		this.methodType = methodType;
	}

	public ExceptionType getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(ExceptionType exceptionType) {
		this.exceptionType = exceptionType;
	}

	public String getHpoTermId() {
		return hpoTermId;
	}

	public void setHpoTermId(String hpoTermId) {
		this.hpoTermId = hpoTermId;
	}

	public String getHpoTermName() {
		return hpoTermName;
	}

	public void setHpoTermName(String hpoTermName) {
		this.hpoTermName = hpoTermName;
	}

	public Boolean getNegated() {
		return negated;
	}

	public void setNegated(Boolean negated) {
		this.negated = negated;
	}

}
