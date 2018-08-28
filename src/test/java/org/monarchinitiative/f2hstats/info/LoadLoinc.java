package org.monarchinitiative.f2hstats.info;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.f2hstats.ApplicationConfig;
import org.monarchinitiative.f2hstats.domain.list.Loinc;
import org.monarchinitiative.f2hstats.repository.list.LoincRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private final static String URL = "https://www.HIPAASpace.com/api/loinc/getcode?q={{CODE}}&rt=json&token=3932f3b0-cfab-11dc-95ff-0800200c9a663932f3b0-cfab-11dc-95ff-0800200c9a66";
	
	@Autowired
	LoincRepository loincRepository;
	
	/**
	 * Loop through all LOINCs and call a webservice to get information on each. Test annotation is commented out
	 * because this is for informational purposes only and not required to pass for running the application.
	 * @throws IOException 
	 */
	@Ignore
	@Test
	public void getLoincInfo() throws IOException {
		List<Loinc> loincs = loincRepository.findAll();
		for (Loinc loinc : loincs) {
			URL url = new URL(URL.replace("{{CODE}}", loinc.getCode()));
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			InputStream is = urlConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);

			int numCharsRead;
			char[] charArray = new char[1024];
			StringBuffer sb = new StringBuffer();
			while ((numCharsRead = isr.read(charArray)) > 0) {
				sb.append(charArray, 0, numCharsRead);
			}
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode obj = mapper.readTree(sb.toString()).findValue("LOINC").get(0);
			if (obj != null && obj.get("SCALE_TYP") != null) {
				String scale = obj.get("SCALE_TYP").textValue();
				String long_common_name = obj.get("LONG_COMMON_NAME").textValue();
				System.out.println(loinc.getCode() + "," + scale + "," + long_common_name);
			} else {
				System.out.println(loinc.getCode() + ",,");
			}
		}
	}

}
