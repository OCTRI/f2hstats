package org.monarchinitiative.f2hstats.service;

import java.util.List;

import org.hl7.fhir.convertors.NullVersionConverterAdvisor30;
import org.hl7.fhir.convertors.VersionConvertor_10_30;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Patient;
import org.monarchinitiative.f2hstats.domain.StatsRun;
import org.monarchinitiative.f2hstats.domain.StatsRunPatient;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;

/**
 * Service for gathering stats on a STU2 FHIR server
 * 
 * @author yateam
 *
 */
@Component
public class Stu2Service extends FhirService {

	private FhirContext ctx = FhirContext.forDstu2Hl7Org();
	private VersionConvertor_10_30 converter = new VersionConvertor_10_30(new NullVersionConverterAdvisor30());

	@Override
	public FhirContext getFhirContext() {
		return ctx;
	}

	/**
	 * This will scrape the entire site for all patients if the API allows.
	 * - Use for smarthealth/r2 sandbox and HAPI FHIR dstu2 sandbox.
	 * 
	 * @param maxPages
	 * @param run
	 * @param client
	 * @throws FHIRException
	 */
	public void allPatientApiRun(int maxPages, StatsRun run) {
		int page = 1;

		// Get all patients
		Bundle patientBundle = getClient().search().forResource(Patient.class).returnBundle(Bundle.class).execute();
		patientBundleLoop(maxPages, run, page, patientBundle, false);
	}

	/**
	 * 
	 * Given a list of patient names formatted "Family,Given", scrape the site for only those patients.
	 * - Use for Open Epic sandbox. This must be narrowed by category (laboratory, vital-signs) in order to get a result
	 * 
	 * @param maxPages
	 * @param run
	 * @param names
	 * @param narrowByCategory
	 * @throws FHIRException
	 */
	public void patientsByNameApiRun(int maxPages, StatsRun run, List<String> names, Boolean narrowByCategory) {
		int page = 1;

		for (String name : names) {
			String[] split = name.split(",");
			Bundle patientBundle = getClient().search()
					.byUrl("Patient?family=" + split[0].trim() + "&given=" + split[1].trim())
					.returnBundle(Bundle.class).execute();
			page = patientBundleLoop(maxPages, run, page, patientBundle, narrowByCategory);
		}
	}

	private int patientBundleLoop(int maxPages, StatsRun run, int page, Bundle patientBundle, Boolean narrowByCategory) {
		System.out.println(patientBundle.hasTotal() ? patientBundle.getTotal() : "Total not provided");
		if (!patientBundle.hasTotal() || patientBundle.getTotal() > 0) {
			while (page <= maxPages && patientBundle != null) {
				try {
					System.out.println(patientBundle.getLink(Bundle.LINK_SELF).getUrl());
					processPatientBundle(run, patientBundle, narrowByCategory);
					patientBundle = (patientBundle.getLink(Bundle.LINK_NEXT) != null)
						? getClient().loadPage().next(patientBundle).execute() : null;
					page++;
				} catch (Exception e) {
					e.printStackTrace();
					// If an exception was thrown processing the bundle or getting the next, stop.
					patientBundle = null;
				}
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
	private void processPatientBundle(StatsRun run, Bundle patientBundle, Boolean narrowByCategory) {
		List<BundleEntryComponent> components = patientBundle.getEntry();
		for (BundleEntryComponent component : components) {
			try {
				org.hl7.fhir.instance.model.Patient patient = (Patient) component.getResource();
				org.hl7.fhir.dstu3.model.Patient stu3Patient = converter.convertPatient(patient);
				String patientId = patient.getIdElement().getIdPart();
				Bundle observationBundle = null;
				if (narrowByCategory) {
					observationBundle = getClient().search()
							.byUrl("Observation?patient=" + patientId + "&category=vital-signs,laboratory")
							.returnBundle(Bundle.class).execute();
				} else {
					observationBundle = getClient().search().byUrl("Observation?patient=" + patientId)
							.returnBundle(Bundle.class).execute();
				}
				System.out.println(
						"\t" + (observationBundle.hasTotal() ? observationBundle.getTotal() : "Total not provided"));
	
				// Only create the patient if there are observations
				if (!observationBundle.hasTotal() || observationBundle.getTotal() > 0) {
					StatsRunPatient statsRunPatient = createStatsRunPatient(run, stu3Patient);
					while (observationBundle != null) {
						System.out.println("\t" + observationBundle.getLink(Bundle.LINK_SELF).getUrl());
						processObservationBundle(statsRunPatient, observationBundle);
						observationBundle = (observationBundle.getLink(Bundle.LINK_NEXT) != null)
								? getClient().loadPage().next(observationBundle).execute() : null;
					}
				}
			} catch (Exception e) {
				// Record the error and try the next patient
				System.out.println("Patient Resource" + component.getResource().getId() + " could not be converted.");
			}
		}
	}

	private void processObservationBundle(StatsRunPatient runPatient, Bundle observationBundle) throws FHIRException {
		List<BundleEntryComponent> components = observationBundle.getEntry();
		for (BundleEntryComponent component : components) {
			try {
				Observation observation = (Observation) component.getResource();
				org.hl7.fhir.dstu3.model.Observation stu3Observation = converter.convertObservation(observation);
				processObservation(runPatient, stu3Observation);
			} catch (Exception e) {
				// Record the error and try the next observation
				System.out.println("Observation Resource " + component.getResource().getId() + " could not be converted.");
			}
		}

	}

}
