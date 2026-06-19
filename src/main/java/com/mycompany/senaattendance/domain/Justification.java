package com.mycompany.senaattendance.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Justification.
 */
@Document(collection = "justification")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Justification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Size(max = 300)
    @Field("description")
    private String description;

    @NotNull
    @Field("start_date")
    private LocalDate startDate;

    @NotNull
    @Field("end_date")
    private LocalDate endDate;

    @Field("evidence")
    private byte[] evidence;

    @NotNull
    @Field("evidence_content_type")
    private String evidenceContentType;

    @DBRef
    @Field("details")
    @JsonIgnoreProperties(value = { "classSection", "justification" }, allowSetters = true)
    private Set<JustificationDetails> detailses = new HashSet<>();

    @DBRef
    @Field("justificationType")
    private JustificationType justificationType;

    @DBRef
    @Field("student")
    @JsonIgnoreProperties(value = { "user", "documentType" }, allowSetters = true)
    private UserProfile student;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public Justification id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public Justification description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public Justification startDate(LocalDate startDate) {
        this.setStartDate(startDate);
        return this;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public Justification endDate(LocalDate endDate) {
        this.setEndDate(endDate);
        return this;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public byte[] getEvidence() {
        return this.evidence;
    }

    public Justification evidence(byte[] evidence) {
        this.setEvidence(evidence);
        return this;
    }

    public void setEvidence(byte[] evidence) {
        this.evidence = evidence;
    }

    public String getEvidenceContentType() {
        return this.evidenceContentType;
    }

    public Justification evidenceContentType(String evidenceContentType) {
        this.evidenceContentType = evidenceContentType;
        return this;
    }

    public void setEvidenceContentType(String evidenceContentType) {
        this.evidenceContentType = evidenceContentType;
    }

    public Set<JustificationDetails> getDetailses() {
        return this.detailses;
    }

    public void setDetailses(Set<JustificationDetails> justificationDetailses) {
        if (this.detailses != null) {
            this.detailses.forEach(i -> i.setJustification(null));
        }
        if (justificationDetailses != null) {
            justificationDetailses.forEach(i -> i.setJustification(this));
        }
        this.detailses = justificationDetailses;
    }

    public Justification detailses(Set<JustificationDetails> justificationDetailses) {
        this.setDetailses(justificationDetailses);
        return this;
    }

    public Justification addDetails(JustificationDetails justificationDetails) {
        this.detailses.add(justificationDetails);
        justificationDetails.setJustification(this);
        return this;
    }

    public Justification removeDetails(JustificationDetails justificationDetails) {
        this.detailses.remove(justificationDetails);
        justificationDetails.setJustification(null);
        return this;
    }

    public JustificationType getJustificationType() {
        return this.justificationType;
    }

    public void setJustificationType(JustificationType justificationType) {
        this.justificationType = justificationType;
    }

    public Justification justificationType(JustificationType justificationType) {
        this.setJustificationType(justificationType);
        return this;
    }

    public UserProfile getStudent() {
        return this.student;
    }

    public void setStudent(UserProfile userProfile) {
        this.student = userProfile;
    }

    public Justification student(UserProfile userProfile) {
        this.setStudent(userProfile);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Justification)) {
            return false;
        }
        return getId() != null && getId().equals(((Justification) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Justification{" +
            "id=" + getId() +
            ", description='" + getDescription() + "'" +
            ", startDate='" + getStartDate() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", evidence='" + getEvidence() + "'" +
            ", evidenceContentType='" + getEvidenceContentType() + "'" +
            "}";
    }
}
