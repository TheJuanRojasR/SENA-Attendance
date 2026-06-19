package com.mycompany.senaattendance.service.dto;

import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.JustificationDetails} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class JustificationDetailsDTO implements Serializable {

    private String id;

    private StateJustification stateJustification;

    @NotNull
    @Size(max = 300)
    private String rejectionReason;

    @NotNull
    @Size(max = 300)
    private String correctionText;

    private byte[] correctionFileUrl;

    private String correctionFileUrlContentType;

    @NotNull
    private Instant responseDate;

    @NotNull
    private ClassSectionDTO classSection;

    @NotNull
    private JustificationDTO justification;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StateJustification getStateJustification() {
        return stateJustification;
    }

    public void setStateJustification(StateJustification stateJustification) {
        this.stateJustification = stateJustification;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCorrectionText() {
        return correctionText;
    }

    public void setCorrectionText(String correctionText) {
        this.correctionText = correctionText;
    }

    public byte[] getCorrectionFileUrl() {
        return correctionFileUrl;
    }

    public void setCorrectionFileUrl(byte[] correctionFileUrl) {
        this.correctionFileUrl = correctionFileUrl;
    }

    public String getCorrectionFileUrlContentType() {
        return correctionFileUrlContentType;
    }

    public void setCorrectionFileUrlContentType(String correctionFileUrlContentType) {
        this.correctionFileUrlContentType = correctionFileUrlContentType;
    }

    public Instant getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Instant responseDate) {
        this.responseDate = responseDate;
    }

    public ClassSectionDTO getClassSection() {
        return classSection;
    }

    public void setClassSection(ClassSectionDTO classSection) {
        this.classSection = classSection;
    }

    public JustificationDTO getJustification() {
        return justification;
    }

    public void setJustification(JustificationDTO justification) {
        this.justification = justification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JustificationDetailsDTO)) {
            return false;
        }

        JustificationDetailsDTO justificationDetailsDTO = (JustificationDetailsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, justificationDetailsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "JustificationDetailsDTO{" +
            "id='" + getId() + "'" +
            ", stateJustification='" + getStateJustification() + "'" +
            ", rejectionReason='" + getRejectionReason() + "'" +
            ", correctionText='" + getCorrectionText() + "'" +
            ", correctionFileUrl='" + getCorrectionFileUrl() + "'" +
            ", responseDate='" + getResponseDate() + "'" +
            ", classSection=" + getClassSection() +
            ", justification=" + getJustification() +
            "}";
    }
}
