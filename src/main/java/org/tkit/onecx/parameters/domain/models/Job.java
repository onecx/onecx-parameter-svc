package org.tkit.onecx.parameters.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "JOB")
public class Job extends TraceableEntity {

}
