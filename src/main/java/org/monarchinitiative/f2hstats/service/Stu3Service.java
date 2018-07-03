package org.monarchinitiative.f2hstats.service;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.monarchinitiative.f2hstats.domain.StatsRun;
import org.monarchinitiative.f2hstats.domain.StatsRunPatient;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;

/**
 * Run the stats gatherer on a STU3 FHIR server
 * @author yateam
 *
 */
@Component
public class Stu3Service extends FhirService {

	private FhirContext ctx = FhirContext.forDstu3();
	
	@Override
	public FhirContext getFhirContext() {
		return ctx;
	}
	
	/**
	 * This will scrape the entire site for all patients if the API allows.
	 * - Use for smarthealth/r3 sandbox.
	 * @param maxPages
	 * @param run
	 * @param client
	 */
	public void allPatientApiRun(int maxPages, StatsRun run) {
		int page = 1;
		
		// Get all patients
		Bundle patientBundle = getClient().search().forResource(Patient.class).returnBundle(Bundle.class).execute();
		patientBundleLoop(maxPages, run, page, patientBundle);
	}

	/**
	 * This will iterate through patients born after 1920 by birthday. The hapiFhir endpoint only grabs the first 10K
	 * patients if you try to grab all, so this more targeted approach will get more patients
	 * - Use for the hapifhir sandbox
	 * @param maxPages
	 * @param run
	 * @param client
	 */
	public void patientsByBirthdayApiRun(int maxPages, StatsRun run) {
		int page = 1;
		
		// Search for all patients limits results to 10000 in hapi-fhir sandbox. Use progressive birthdays instead to get more patients
		for (int i=1920; i<2018; i++) {
			String firstDay = Integer.toString(i) + "-01-01";
			String lastDay = Integer.toString(i) + "-12-31";
			Bundle patientBundle = getClient().search().forResource(Patient.class).where(Patient.BIRTHDATE.afterOrEquals().day(firstDay)).and(Patient.BIRTHDATE.beforeOrEquals().day(lastDay)).returnBundle(Bundle.class).execute();
			page = patientBundleLoop(maxPages, run, page, patientBundle);
		}
	}
	
	private int patientBundleLoop(int maxPages, StatsRun run, int page, Bundle patientBundle) {
		System.out.println(patientBundle.hasTotal()?patientBundle.getTotal():"Total not provided");
		if (!patientBundle.hasTotal() || patientBundle.getTotal() > 0) {
			while (page <= maxPages && patientBundle != null) {
				System.out.println(patientBundle.getLink(Bundle.LINK_SELF).getUrl());
				processPatientBundle(run, patientBundle);
				patientBundle = (patientBundle.getLink(Bundle.LINK_NEXT) != null) ? getClient().loadPage().next(patientBundle).execute() : null;
				page++;
			}
		}
		return page;
	}

	/**
	 * Process a patient bundle, gathering all observations, attempting to convert, and saving
	 * results to the repo
	 * @param run
	 * @param patientBundle
	 * @param narrowByCategory whether the observations must be narrowed by category due to API restrictions
	 * @throws FHIRException
	 */
	private void processPatientBundle(StatsRun run, Bundle patientBundle) {
		List<BundleEntryComponent> components = patientBundle.getEntry();
		for (BundleEntryComponent component : components) {
			Patient patient = (Patient) component.getResource();
			String patientId = patient.getIdElement().getIdPart();
			//Bundle observationBundle = client.search().byUrl("Observation?patient="+patientId).returnBundle(Bundle.class).execute();
			Bundle observationBundle = getClient().search().forResource(Observation.class).where(new ReferenceClientParam("patient").hasId(patientId)).returnBundle(Bundle.class).execute();
			System.out.println("\t" + (observationBundle.hasTotal()?observationBundle.getTotal():"Total not provided"));
			
			// Only create the patient if there are observations
			if (!observationBundle.hasTotal() || observationBundle.getTotal() > 0) {
				StatsRunPatient statsRunPatient = createStatsRunPatient(run, patient);				
				while (observationBundle != null) {
					System.out.println("\t" + observationBundle.getLink(Bundle.LINK_SELF).getUrl());
					processObservationBundle(statsRunPatient, observationBundle);
					observationBundle = (observationBundle.getLink(Bundle.LINK_NEXT) != null) ? getClient().loadPage().next(observationBundle).execute() : null;
				}
			}

		}
	}

	private void processObservationBundle(StatsRunPatient runPatient, Bundle observationBundle) {
		List<BundleEntryComponent> components = observationBundle.getEntry();
		for (BundleEntryComponent component : components) {
			Observation observation = (Observation) component.getResource();
			processObservation(runPatient, observation);
		}
		
	}

}
