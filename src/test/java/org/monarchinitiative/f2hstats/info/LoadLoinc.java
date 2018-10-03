package org.monarchinitiative.f2hstats.info;

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.f2hstats.ApplicationConfig;
import org.monarchinitiative.f2hstats.domain.list.Loinc;
import org.monarchinitiative.f2hstats.repository.list.LoincRepository;
import org.monarchinitiative.fhir2hpo.loinc.DefaultLoinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.Loinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.loinc.exception.LoincException;
import org.monarchinitiative.fhir2hpo.loinc.exception.LoincNotAnnotatedException;
import org.monarchinitiative.fhir2hpo.service.AnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
public class LoadLoinc {

	// This sample token may not always work. Got from https://www.hipaaspace.com/medical_web_services/test.drive.restful.web.services?type=LOINC
	private final static String URL = "https://www.hipaaspace.com/api/loinc/getcode?q={{CODE}}&rt=json&token=3932f3b0-cfab-11dc-95ff-0800200c9a663932f3b0-cfab-11dc-95ff-0800200c9a66";
	
	@Autowired
	LoincRepository loincRepository;
	
	@Autowired
	AnnotationService annotationService;
	
	/**
	 * TODO: The HIPAASPACE web service no longer works with the supplied token. We would have to implement real auth to 
	 * make use of it.
	 * Loop through all LOINCs and call a webservice to get information on each. Test annotation is commented out
	 * because this is for informational purposes only and not required to pass for running the application.
	 * @throws IOException 
	 * @throws LoincException 
	 * @throws LoincNotAnnotatedException 
	 */
	@Ignore
	@Test
	public void getLoincInfo() throws IOException, LoincNotAnnotatedException, LoincException {
		System.out.println("LOINC, Annotated, Interpretable");
		List<Loinc> loincs = loincRepository.findAll();
		for (Loinc loinc : loincs) {
			Boolean annotated = false;
			Boolean interpretable = false;
			try {
				Loinc2HpoAnnotation annotation = annotationService.getAnnotations(new LoincId(loinc.getCode()));
				annotated = true;
				if (annotation instanceof DefaultLoinc2HpoAnnotation) {
					interpretable = true;
				}
			} catch (LoincNotAnnotatedException e) {
				// Not annotated or interpretable
			}
			System.out.println(loinc.getCode() + "," + annotated + "," + interpretable);
//			URL url = new URL(URL.replace("{{CODE}}", loinc.getCode()));
//			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//			InputStream is = urlConnection.getInputStream();
//			InputStreamReader isr = new InputStreamReader(is);
//
//			int numCharsRead;
//			char[] charArray = new char[1024];
//			StringBuffer sb = new StringBuffer();
//			while ((numCharsRead = isr.read(charArray)) > 0) {
//				sb.append(charArray, 0, numCharsRead);
//			}
//			
//			ObjectMapper mapper = new ObjectMapper();
//			JsonNode obj = mapper.readTree(sb.toString()).findValue("LOINC").get(0);
//			if (obj != null && obj.get("SCALE_TYP") != null) {
//				String scale = obj.get("SCALE_TYP").textValue();
//				String long_common_name = obj.get("LONG_COMMON_NAME").textValue();
//				System.out.println(loinc.getCode() + "," + scale + "," + long_common_name);
//			} else {
//				System.out.println(loinc.getCode() + ",,");
//			}
		}
	}

}
