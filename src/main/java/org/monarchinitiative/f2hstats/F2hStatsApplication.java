package org.monarchinitiative.f2hstats;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.exceptions.FHIRException;
import org.monarchinitiative.f2hstats.domain.StatsRun;
import org.monarchinitiative.f2hstats.repository.StatsRunRepository;
import org.monarchinitiative.f2hstats.service.FhirService;
import org.monarchinitiative.f2hstats.service.Stu2Service;
import org.monarchinitiative.f2hstats.service.Stu3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = { "org.monarchinitiative.f2hstats", "org.monarchinitiative.fhir2hpo" })
@EnableJpaRepositories(basePackages = { "org.monarchinitiative.f2hstats" })
public class F2hStatsApplication implements CommandLineRunner {
	
	//TODO: Add Flyway and a finalized db schema - change Hibernate settings to validate
	
	private static final String SANDBOX_R3 = "R3";
	private static final String SANDBOX_R2 = "R2";
	private static final String SANDBOX_HAPI3 = "HAPI3";
	private static final String SANDBOX_HAPI2 = "HAPI2";
	private static final String SANDBOX_EPIC = "EPIC";
	private static final String SANDBOX_MITRE = "MITRE";

	@Autowired
	StatsRunRepository statsRunRepository;

	@Autowired
	Stu2Service stu2Service;

	@Autowired
	Stu3Service stu3Service;
	
	/**
	 * Run the stats gatherer against one of the preconfigured sandboxes
	 * Args:
	 * -p 20 stop after 20 pages of patients (optional)
	 * -s sandbox the name of the sandbox to run (R3 by default)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(F2hStatsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		int maxPages = Integer.MAX_VALUE;
		String sandbox = "R3"; // Run on the R3 server by default
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-p")) {
				maxPages = Integer.parseInt(args[i + 1]);
			} else if (arg.equals("-s")) {
				sandbox = args[i + 1];
				if (!getSandboxOptions().contains(sandbox)) {
					throw new Exception("Sandbox must be one of the following: " + getSandboxOptions().stream().collect(Collectors.joining(",")));
				}
			}
		}

		runSandbox(maxPages, sandbox);
	}

	private static List<String> getSandboxOptions() {
		return Arrays.asList(SANDBOX_R3, SANDBOX_R2, SANDBOX_HAPI3, SANDBOX_HAPI2, SANDBOX_EPIC, SANDBOX_MITRE);
	}

	private void runSandbox(int maxPages, String sandbox) throws FHIRException {
		StatsRun run;
		switch (sandbox) {
			case SANDBOX_MITRE:
				stu3Service.setUrl("https://syntheticmass.mitre.org/fhir");
				run = createStatsRun(sandbox, stu3Service);
				stu3Service.allPatientApiRun(maxPages, run);
				break;
			case SANDBOX_HAPI3:
				stu3Service.setUrl("http://hapi.fhir.org/baseDstu3");
				run = createStatsRun(sandbox, stu3Service);
				stu3Service.patientsByBirthdayApiRun(maxPages, run);
				break;
			case SANDBOX_HAPI2:
				/* This sandbox had incorrect gender formats for several patients. The bundle can't be 
				 * parsed when this happens, and we can't determine the next set of patients to load.
				 * I deleted some of the patients, so we could get a little farther along, but gave up 
				 * after awhile. Patients deleted: 821, 9914, 6541, pid-17344
				 * curl -X "DELETE" http://hapi.fhir.org/baseDstu2/Patient/pid-17344 */
				stu2Service.setUrl("http://hapi.fhir.org/baseDstu2");
				run = createStatsRun(sandbox, stu2Service);
				stu2Service.allPatientApiRun(maxPages, run);
				break;
			case SANDBOX_EPIC:
				stu2Service.setUrl("https://open-ic.epic.com/FHIR/api/FHIR/DSTU2");
				run = createStatsRun(sandbox, stu2Service);
				stu2Service.patientsByNameApiRun(maxPages, run, Arrays.asList(
						"Argonaut,Jason", "Argonaut,Jessica", "Ragsdale,Flapjacks",
						"Ragsdale,Pancakes", "Ragsdale,Waffles","Ragsdale,Bacon",
						"Williams,Emily","Kirk,James"), true);
				break;
			case SANDBOX_R2:
				stu2Service.setUrl("https://r2.smarthealthit.org");
				run = createStatsRun(sandbox, stu2Service);
				stu2Service.allPatientApiRun(maxPages, run);
				break;
			case SANDBOX_R3:
				stu3Service.setUrl("https://r3.smarthealthit.org");
				run = createStatsRun(sandbox, stu3Service);
				stu3Service.allPatientApiRun(maxPages, run);
				break;
			default:
				break;
		}
	}

	private StatsRun createStatsRun(String server, FhirService service) {
		StatsRun run = new StatsRun(server, service.getUrl(), service.getVersion(), new Date());
		return statsRunRepository.save(run);
	}

}
