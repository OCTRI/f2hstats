package org.monarchinitiative.f2hstats.domain.list;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.monarchinitiative.f2hstats.domain.AbstractEntity;

/**
 * The list of LOINC codes encountered
 * 
 * @author yateam
 *
 */
@Entity
public class Loinc extends AbstractEntity {

	@NotNull
	@Column(unique = true)
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
