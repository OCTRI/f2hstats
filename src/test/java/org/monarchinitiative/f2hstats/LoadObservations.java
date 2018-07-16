package org.monarchinitiative.f2hstats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.exceptions.FHIRException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.f2hstats.domain.StatsRunObservation;
import org.monarchinitiative.f2hstats.repository.StatsRunObservationRepository;
import org.monarchinitiative.f2hstats.service.Stu3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ca.uhn.fhir.parser.IParser;

/**
 * This wires up the real database for one-off testing without actually running the stats
 * gatherer. It can be used to interrogate the database.
 * 
 * @author yateam
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
@ContextConfiguration(classes = ApplicationConfig.class)
public class LoadObservations {

	@Autowired
	StatsRunObservationRepository statsRunObservationRepository;

	@Autowired
	Stu3Service stu3Service;
	
	/**
	 * Summarize the valueString observations in the database. Prints a pipe-delimited
	 * list of the normalized string, the count of encounters, and the servers the string
	 * was found on.
	 */
	@Test
	public void testGetObservationsWithValueStrings() {

		IParser parser = stu3Service.getParser();
		// The string and the servers it is found on
		Map<String, List<String>> valueStrings = new HashMap<>();

		StatsRunObservation exampleObservation = new StatsRunObservation();
		exampleObservation.setJson("valueString");

		ExampleMatcher exampleMatcher = ExampleMatcher.matching()
				.withIgnoreCase().withStringMatcher(StringMatcher.CONTAINING);

		List<StatsRunObservation> observations = statsRunObservationRepository
				.findAll(Example.of(exampleObservation, exampleMatcher));
		for (StatsRunObservation observation : observations) {
			Observation fhirObservation = (Observation) parser.parseResource(observation.getJson());
			String server = observation.getPatient().getStatsRun().getServer();
			try {
				String valueString = fhirObservation.getValueStringType().asStringValue().toLowerCase();
				if (valueStrings.containsKey(valueString)) {
					List<String> servers = valueStrings.get(valueString);
					servers.add(server);
				} else {
					List<String> list = new ArrayList<>();
					list.add(server);
					valueStrings.put(valueString, list);
				}
			} catch (FHIRException e) {
				System.out.println(
						"Could not convert observation to value string: " + fhirObservation.getIdElement().getIdPart());
			}
		}
		
		System.out.println(valueStrings.entrySet().stream().map(it -> it.getKey() + "|" + it.getValue().size() + "|" +
				it.getValue().stream().distinct().collect(Collectors.joining(","))).collect(Collectors.joining("\n")));

	}

}
