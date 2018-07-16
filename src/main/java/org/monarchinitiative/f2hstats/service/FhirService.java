package org.monarchinitiative.f2hstats.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.monarchinitiative.f2hstats.domain.MethodResult;
import org.monarchinitiative.f2hstats.domain.ObservationLoinc;
import org.monarchinitiative.f2hstats.domain.StatsRun;
import org.monarchinitiative.f2hstats.domain.StatsRunObservation;
import org.monarchinitiative.f2hstats.domain.StatsRunPatient;
import org.monarchinitiative.f2hstats.domain.list.ExceptionType;
import org.monarchinitiative.f2hstats.domain.list.Loinc;
import org.monarchinitiative.f2hstats.domain.list.MethodType;
import org.monarchinitiative.f2hstats.repository.MethodResultRepository;
import org.monarchinitiative.f2hstats.repository.ObservationLoincRepository;
import org.monarchinitiative.f2hstats.repository.StatsRunObservationRepository;
import org.monarchinitiative.f2hstats.repository.StatsRunPatientRepository;
import org.monarchinitiative.f2hstats.repository.list.ExceptionTypeRepository;
import org.monarchinitiative.f2hstats.repository.list.LoincRepository;
import org.monarchinitiative.f2hstats.repository.list.MethodTypeRepository;
import org.monarchinitiative.fhir2hpo.fhir.util.ObservationUtil;
import org.monarchinitiative.fhir2hpo.hpo.HpoConversionResult;
import org.monarchinitiative.fhir2hpo.hpo.MethodConversionResult;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.loinc.exception.LoincException;
import org.monarchinitiative.fhir2hpo.service.ObservationAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * The abstract FHIR service with all autowired repository dependencies. The extending
 * service must implement the method getFhirContext() and should add relevant
 * methods for getting patients and observations, converting, and saving results to the repo.
 */
@Service
public abstract class FhirService {

	@Autowired
	protected StatsRunPatientRepository statsRunPatientRepository;

	@Autowired
	protected StatsRunObservationRepository statsRunObservationRepository;

	@Autowired
	protected MethodResultRepository methodResultRepository;

	@Autowired
	protected ExceptionTypeRepository exceptionTypeRepository;

	@Autowired
	protected MethodTypeRepository methodTypeRepository;
	
	@Autowired
	protected LoincRepository loincRepository;
	
	@Autowired
	protected ObservationLoincRepository observationLoincRepository;

	@Autowired
	protected ObservationAnalysisService observationAnalysisService;
	
	private FhirContext stu3Ctx = FhirContext.forDstu3();
	
	private String url;
	private IGenericClient client;
	
	public abstract FhirContext getFhirContext();

	public void setUrl(String url) {
		this.url = url;
		FhirContext ctx = getFhirContext();
		ctx.getRestfulClientFactory().setSocketTimeout(30 * 1000); // Extend the timeout for hapi-fhir sandbox
		client = ctx.newRestfulGenericClient(url);
	}
	
	public String getUrl() {
		return url;
	}

	public IGenericClient getClient() {
		return client;
	}
	
	public String getVersion() {
		return getFhirContext().getVersion().getVersion().name();
	}	

	// The parser will always be version 3 since the persisted json will always be a STU33 object
	public IParser getParser() {
		return stu3Ctx.newJsonParser();
	}

	/**
	 * Create a StatsRunPatient and save it to the repo
	 * @param run the StatsRun from the repo
	 * @param fhirPatient the FHIR patient
	 * @return the saved StatsRunPatient
	 */
	protected StatsRunPatient createStatsRunPatient(StatsRun run, Patient fhirPatient) {
		fhirPatient.setPhoto(null); // json can be too long if there are attachments
		StatsRunPatient statsRunPatient = new StatsRunPatient();
		statsRunPatient.setStatsRun(run);
		statsRunPatient.setFhirId(fhirPatient.getIdElement().getIdPart());
		statsRunPatient.setJson(getParser().encodeResourceToString(fhirPatient));
		return statsRunPatientRepository.save(statsRunPatient);
	}

	/**
	 * Create a StatsRunObservation for each direct Loinc and save it to the repo. Also save all Loinc information
	 * related to the observation.
	 * @param patient the StatsRunPatient from the repo
	 * @param observation the FHIR observation
	 * @return the saved StatsRunObservations
	 */
	protected List<StatsRunObservation> createStatsRunObservations(StatsRunPatient patient, Observation fhirObservation) {
		
		Set<LoincId> directLoincIds;
		
		try {
			directLoincIds = ObservationUtil.getLoincIdsOfObservation(fhirObservation);
		} catch (LoincException e) {
			// No Loincs. Still create the observation so we can understand how often this happens.
			StatsRunObservation statsRunObservation = new StatsRunObservation();
			statsRunObservation.setPatient(patient);
			statsRunObservation.setFhirId(fhirObservation.getIdElement().getIdPart());
			statsRunObservation.setJson(getParser().encodeResourceToString(fhirObservation));
			statsRunObservation = statsRunObservationRepository.save(statsRunObservation);
			return Arrays.asList(statsRunObservation);
		}
		
		List<StatsRunObservation> observations = new ArrayList<>();
		for (LoincId directLoincId : directLoincIds) {
			Loinc loinc = getOrCreateLoinc(directLoincId.getCode());
			StatsRunObservation statsRunObservation = new StatsRunObservation();
			statsRunObservation.setLoinc(loinc);
			statsRunObservation.setPatient(patient);
			statsRunObservation.setFhirId(fhirObservation.getIdElement().getIdPart());
			statsRunObservation.setJson(getParser().encodeResourceToString(fhirObservation));
			statsRunObservation = statsRunObservationRepository.save(statsRunObservation);
			// Parse all the Loinc info and save
			parseLoincInfo(fhirObservation, statsRunObservation);
			observations.add(statsRunObservation);
		}
		
		return observations;
	}

	/**
	 * Parse information about LOINC codes from the FHIR observation and save it to the repo.
	 * @param fhirObservation the FHIR observation
	 * @param runObservation the runObservation in the repo
	 */
	protected void parseLoincInfo(Observation fhirObservation, StatsRunObservation runObservation) {
		try {
			Set<LoincId> directLoincIds = ObservationUtil.getLoincIdsOfObservation(fhirObservation);
			for (LoincId directLoincId : directLoincIds) {
				Loinc loinc = getOrCreateLoinc(directLoincId.getCode());
				ObservationLoinc observationLoinc = new ObservationLoinc();
				observationLoinc.setObservation(runObservation);
				observationLoinc.setLoinc(loinc);
				observationLoinc.setDirect(true);
				observationLoincRepository.save(observationLoinc);
			}
			Set<LoincId> indirectLoincs = ObservationUtil.getComponentLoincIdsOfObservation(fhirObservation);
			for (LoincId indirectLoinc : indirectLoincs) {
				Loinc loinc = getOrCreateLoinc(indirectLoinc.getCode());
				ObservationLoinc observationLoinc = new ObservationLoinc();
				observationLoinc.setObservation(runObservation);
				observationLoinc.setLoinc(loinc);
				observationLoinc.setDirect(false);
				observationLoincRepository.save(observationLoinc);				
			}
		} catch (LoincException e) {
			// Do nothing
		}
	}

	protected void createMethodResult(StatsRunObservation runObservation,
			MethodConversionResult methodConversionResult) {
		MethodResult methodResult = new MethodResult();
		methodResult.setObservation(runObservation);
		methodResult.setMethodType(getOrCreateMethodType(methodConversionResult.getMethod()));
		if (methodConversionResult.hasTerm()) {
			methodResult.setHpoTermId(methodConversionResult.getTerm().getHpoTerm().getId().getId());
			methodResult.setHpoTermName(methodConversionResult.getTerm().getHpoTerm().getName());
			methodResult.setNegated(methodConversionResult.getTerm().isNegated());
		} else {
			methodResult.setExceptionType(getOrCreateExceptionType(methodConversionResult.getException()));
		}
		methodResultRepository.save(methodResult);
	}

	/**
	 * Get the Loinc entry from the repo, creating one if it doesn't exist.
	 * @param code
	 * @return the Loinc entry corresponding to the given code
	 */
	protected Loinc getOrCreateLoinc(String code) {
		Loinc loinc = loincRepository.findByCode(code);
		if (loinc == null) {
			loinc = new Loinc();
			loinc.setCode(code);
			loinc = loincRepository.save(loinc);
		}
		return loinc;
	}

	/**
	 * Get the ExceptionType from the repo, creating one if it doesn't exist.
	 * @param code
	 * @return the ExceptionType corresponding to the given description
	 */
	protected ExceptionType getOrCreateExceptionType(Exception exception) {
		ExceptionType exceptionType = exceptionTypeRepository.findByDescription(exception.getClass().getName());
		if (exceptionType == null) {
			exceptionType = new ExceptionType();
			exceptionType.setDescription(exception.getClass().getName());
			exceptionType = exceptionTypeRepository.save(exceptionType);
		}
		return exceptionType;
	}

	/**
	 * Get the MethodType from the repo, creating one if it doesn't exist.
	 * @param code
	 * @return the MethodType corresponding to the given description
	 */
	protected MethodType getOrCreateMethodType(String description) {
		MethodType methodType = methodTypeRepository.findByDescription(description);
		if (methodType == null) {
			methodType = new MethodType();
			methodType.setDescription(description);
			methodType = methodTypeRepository.save(methodType);
		}
		return methodType;
	}
	
	/**
	 * This common method processes the observation, attempts to convert, and saves results to the repo.
	 * @param runPatient
	 * @param fhirObservation
	 */
	protected void processObservation(StatsRunPatient runPatient, Observation fhirObservation) {
		List<StatsRunObservation> runObservations = createStatsRunObservations(runPatient, fhirObservation);
		List<HpoConversionResult> results = observationAnalysisService.analyzeObservation(fhirObservation);
		for (HpoConversionResult result : results) {
			StatsRunObservation runObservation;
			List<StatsRunObservation> matchingLoinc = runObservations.stream().filter(o -> {
				if (result.getLoincId() != null) {
					return o.getLoinc().getCode().equals(result.getLoincId().getCode());
				} else {
					// No Loinc - a single StatsRunObservation should exist
					return true;
				}
			}).collect(Collectors.toList());
			
			// Flawed logic if assertion is not true
			assert matchingLoinc.size() == 1;
			runObservation = matchingLoinc.get(0);
			
			if (result.hasException()) {
				ExceptionType exceptionType = getOrCreateExceptionType(result.getException());
				runObservation.setExceptionType(exceptionType);
			} else {
				for (MethodConversionResult methodConversionResult : result.getMethodResults().values()) {
					createMethodResult(runObservation, methodConversionResult);
				}
			}
			statsRunObservationRepository.save(runObservation);
		}
	}


}
