package com.mycompany.senaattendance.domain;

import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Program.
 */
@Document(collection = "program")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Program extends AbstractAuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Size(max = 200)
    @Field("name")
    private String name;

    @NotNull
    @Size(max = 10)
    @Field("initials")
    private String initials;

    @NotNull
    @Size(max = 30)
    @Field("code")
    private String code;

    @NotNull
    @Field("trimesters")
    @Min(value = 1)
    @Max(value = 9)
    private Integer trimesters;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public Program id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Program name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitials() {
        return this.initials;
    }

    public Program initials(String initials) {
        this.setInitials(initials);
        return this;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getCode() {
        return this.code;
    }

    public Program code(String code) {
        this.setCode(code);
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getTrimesters() {
        return this.trimesters;
    }

    public Program trimesters(Integer trimesters) {
        this.setTrimesters(trimesters);
        return this;
    }

    public void setTrimesters(Integer trimesters) {
        this.trimesters = trimesters;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Program)) {
            return false;
        }
        return getId() != null && getId().equals(((Program) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Program{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", initials='" + getInitials() + "'" +
            ", code='" + getCode() + "'" +
            ", trimesters=" + getTrimesters() +
            "}";
    }
}
