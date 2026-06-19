package com.mycompany.senaattendance.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A JustificationDetails.
 */
@Document(collection = "justification_details")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class JustificationDetails implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("state_justification")
    private StateJustification stateJustification;

    @NotNull
    @Size(max = 300)
    @Field("rejection_reason")
    private String rejectionReason;

    @NotNull
    @Size(max = 300)
    @Field("correction_text")
    private String correctionText;

    @Field("correction_file_url")
    private byte[] correctionFileUrl;

    @NotNull
    @Field("correction_file_url_content_type")
    private String correctionFileUrlContentType;

    @NotNull
    @Field("response_date")
    private Instant responseDate;

    @DBRef
    @Field("classSection")
    @JsonIgnoreProperties(value = { "scheduleses", "exceptionses", "instructor", "grade" }, allowSetters = true)
    private ClassSection classSection;

    @DBRef
    @Field("justification")
    @JsonIgnoreProperties(value = { "detailses", "justificationType", "student" }, allowSetters = true)
    private Justification justification;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public JustificationDetails id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StateJustification getStateJustification() {
        return this.stateJustification;
    }

    public JustificationDetails stateJustification(StateJustification stateJustification) {
        this.setStateJustification(stateJustification);
        return this;
    }

    public void setStateJustification(StateJustification stateJustification) {
        this.stateJustification = stateJustification;
    }

    public String getRejectionReason() {
        return this.rejectionReason;
    }

    public JustificationDetails rejectionReason(String rejectionReason) {
        this.setRejectionReason(rejectionReason);
        return this;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCorrectionText() {
        return this.correctionText;
    }

    public JustificationDetails correctionText(String correctionText) {
        this.setCorrectionText(correctionText);
        return this;
    }

    public void setCorrectionText(String correctionText) {
        this.correctionText = correctionText;
    }

    public byte[] getCorrectionFileUrl() {
        return this.correctionFileUrl;
    }

    public JustificationDetails correctionFileUrl(byte[] correctionFileUrl) {
        this.setCorrectionFileUrl(correctionFileUrl);
        return this;
    }

    public void setCorrectionFileUrl(byte[] correctionFileUrl) {
        this.correctionFileUrl = correctionFileUrl;
    }

    public String getCorrectionFileUrlContentType() {
        return this.correctionFileUrlContentType;
    }

    public JustificationDetails correctionFileUrlContentType(String correctionFileUrlContentType) {
        this.correctionFileUrlContentType = correctionFileUrlContentType;
        return this;
    }

    public void setCorrectionFileUrlContentType(String correctionFileUrlContentType) {
        this.correctionFileUrlContentType = correctionFileUrlContentType;
    }

    public Instant getResponseDate() {
        return this.responseDate;
    }

    public JustificationDetails responseDate(Instant responseDate) {
        this.setResponseDate(responseDate);
        return this;
    }

    public void setResponseDate(Instant responseDate) {
        this.responseDate = responseDate;
    }

    public ClassSection getClassSection() {
        return this.classSection;
    }

    public void setClassSection(ClassSection classSection) {
        this.classSection = classSection;
    }

    public JustificationDetails classSection(ClassSection classSection) {
        this.setClassSection(classSection);
        return this;
    }

    public Justification getJustification() {
        return this.justification;
    }

    public void setJustification(Justification justification) {
        this.justification = justification;
    }

    public JustificationDetails justification(Justification justification) {
        this.setJustification(justification);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JustificationDetails)) {
            return false;
        }
        return getId() != null && getId().equals(((JustificationDetails) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "JustificationDetails{" +
            "id=" + getId() +
            ", stateJustification='" + getStateJustification() + "'" +
            ", rejectionReason='" + getRejectionReason() + "'" +
            ", correctionText='" + getCorrectionText() + "'" +
            ", correctionFileUrl='" + getCorrectionFileUrl() + "'" +
            ", correctionFileUrlContentType='" + getCorrectionFileUrlContentType() + "'" +
            ", responseDate='" + getResponseDate() + "'" +
            "}";
    }
}
