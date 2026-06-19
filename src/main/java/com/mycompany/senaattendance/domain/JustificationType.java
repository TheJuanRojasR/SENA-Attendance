package com.mycompany.senaattendance.domain;

import com.mycompany.senaattendance.domain.enumeration.State;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A JustificationType.
 */
@Document(collection = "justification_type")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class JustificationType implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Size(max = 100)
    @Field("name")
    private String name;

    @Field("limit_per_trimester")
    private Integer limitPerTrimester;

    @NotNull
    @Field("state")
    private State state;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public JustificationType id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public JustificationType name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLimitPerTrimester() {
        return this.limitPerTrimester;
    }

    public JustificationType limitPerTrimester(Integer limitPerTrimester) {
        this.setLimitPerTrimester(limitPerTrimester);
        return this;
    }

    public void setLimitPerTrimester(Integer limitPerTrimester) {
        this.limitPerTrimester = limitPerTrimester;
    }

    public State getState() {
        return this.state;
    }

    public JustificationType state(State state) {
        this.setState(state);
        return this;
    }

    public void setState(State state) {
        this.state = state;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JustificationType)) {
            return false;
        }
        return getId() != null && getId().equals(((JustificationType) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "JustificationType{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", limitPerTrimester=" + getLimitPerTrimester() +
            ", state='" + getState() + "'" +
            "}";
    }
}
