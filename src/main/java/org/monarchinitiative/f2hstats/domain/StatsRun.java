package org.monarchinitiative.f2hstats.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * A run of the stats gatherer. A run might occur on the same server base multiple times.
 * 
 * @author yateam
 *
 */
@Entity
public class StatsRun extends AbstractEntity {

	@NotNull
	String server;

	@NotNull
	String serverBase;

	@NotNull
	String fhirVersion;

	@NotNull
	Date runDate;

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public StatsRun() {

	}

	public StatsRun(String server, String serverBase, String fhirVersion, Date runDate) {
		this.server = server;
		this.serverBase = serverBase;
		this.fhirVersion = fhirVersion;
		this.runDate = runDate;
	}

	public String getServerBase() {
		return serverBase;
	}

	public void setServerBase(String serverBase) {
		this.serverBase = serverBase;
	}

	public String getFhirVersion() {
		return fhirVersion;
	}

	public void setFhirVersion(String fhirVersion) {
		this.fhirVersion = fhirVersion;
	}

	public Date getRunDate() {
		return runDate;
	}

	public void setRunDate(Date runDate) {
		this.runDate = runDate;
	}

}
