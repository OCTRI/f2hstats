package org.monarchinitiative.f2hstats.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.monarchinitiative.f2hstats.domain.list.Loinc;

/**
 * Track the LOINCs associated with the Observation and whether or not the association is direct
 * 
 * @author yateam
 *
 */
@Entity
public class ObservationLoinc extends AbstractEntity {

	@ManyToOne
	@NotNull
	private StatsRunObservation observation;

	@ManyToOne
	@NotNull
	private Loinc loinc;

	// Whether or not the loinc is found directly on the observation (as the codeable concept)
	@NotNull
	private Boolean direct;

	public StatsRunObservation getObservation() {
		return observation;
	}

	public void setObservation(StatsRunObservation observation) {
		this.observation = observation;
	}

	public Loinc getLoinc() {
		return loinc;
	}

	public void setLoinc(Loinc loinc) {
		this.loinc = loinc;
	}

	public Boolean getDirect() {
		return direct;
	}

	public void setDirect(Boolean direct) {
		this.direct = direct;
	}

}
