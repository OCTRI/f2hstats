package org.monarchinitiative.f2hstats;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "org.monarchinitiative.f2hstats", "org.monarchinitiative.fhir2hpo" })
public class ApplicationConfig {

}
