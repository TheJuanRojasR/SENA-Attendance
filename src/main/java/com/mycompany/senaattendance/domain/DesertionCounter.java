package com.mycompany.senaattendance.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A DesertionCounter.
 */
@Document(collection = "desertion_counter")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DesertionCounter implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("total_global_absences")
    private Integer totalGlobalAbsences;

    @Field("accumulated_late_arrivals")
    private Integer accumulatedLateArrivals;

    @Field("work_justifications_used")
    private Integer workJustificationsUsed;

    @Field("alerts_sent")
    private Boolean alertsSent;

    @DBRef
    @Field("student")
    @JsonIgnoreProperties(value = { "user", "documentType" }, allowSetters = true)
    private UserProfile student;

    @DBRef
    @Field("trimester")
    private Trimester trimester;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public DesertionCounter id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTotalGlobalAbsences() {
        return this.totalGlobalAbsences;
    }

    public DesertionCounter totalGlobalAbsences(Integer totalGlobalAbsences) {
        this.setTotalGlobalAbsences(totalGlobalAbsences);
        return this;
    }

    public void setTotalGlobalAbsences(Integer totalGlobalAbsences) {
        this.totalGlobalAbsences = totalGlobalAbsences;
    }

    public Integer getAccumulatedLateArrivals() {
        return this.accumulatedLateArrivals;
    }

    public DesertionCounter accumulatedLateArrivals(Integer accumulatedLateArrivals) {
        this.setAccumulatedLateArrivals(accumulatedLateArrivals);
        return this;
    }

    public void setAccumulatedLateArrivals(Integer accumulatedLateArrivals) {
        this.accumulatedLateArrivals = accumulatedLateArrivals;
    }

    public Integer getWorkJustificationsUsed() {
        return this.workJustificationsUsed;
    }

    public DesertionCounter workJustificationsUsed(Integer workJustificationsUsed) {
        this.setWorkJustificationsUsed(workJustificationsUsed);
        return this;
    }

    public void setWorkJustificationsUsed(Integer workJustificationsUsed) {
        this.workJustificationsUsed = workJustificationsUsed;
    }

    public Boolean getAlertsSent() {
        return this.alertsSent;
    }

    public DesertionCounter alertsSent(Boolean alertsSent) {
        this.setAlertsSent(alertsSent);
        return this;
    }

    public void setAlertsSent(Boolean alertsSent) {
        this.alertsSent = alertsSent;
    }

    public UserProfile getStudent() {
        return this.student;
    }

    public void setStudent(UserProfile userProfile) {
        this.student = userProfile;
    }

    public DesertionCounter student(UserProfile userProfile) {
        this.setStudent(userProfile);
        return this;
    }

    public Trimester getTrimester() {
        return this.trimester;
    }

    public void setTrimester(Trimester trimester) {
        this.trimester = trimester;
    }

    public DesertionCounter trimester(Trimester trimester) {
        this.setTrimester(trimester);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DesertionCounter)) {
            return false;
        }
        return getId() != null && getId().equals(((DesertionCounter) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DesertionCounter{" +
            "id=" + getId() +
            ", totalGlobalAbsences=" + getTotalGlobalAbsences() +
            ", accumulatedLateArrivals=" + getAccumulatedLateArrivals() +
            ", workJustificationsUsed=" + getWorkJustificationsUsed() +
            ", alertsSent='" + getAlertsSent() + "'" +
            "}";
    }
}
