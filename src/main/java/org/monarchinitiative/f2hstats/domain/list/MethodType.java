package org.monarchinitiative.f2hstats.domain.list;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.monarchinitiative.f2hstats.domain.AbstractEntity;

/**
 * The types of methods that can be used to convert observations.
 * 
 * @author yateam
 *
 */
@Entity
public class MethodType extends AbstractEntity {

	@NotNull
	@Column(unique = true)
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
