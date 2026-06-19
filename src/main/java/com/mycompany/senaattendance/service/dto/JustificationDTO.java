package com.mycompany.senaattendance.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.Justification} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class JustificationDTO implements Serializable {

    private String id;

    @NotNull
    @Size(max = 300)
    private String description;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private byte[] evidence;

    private String evidenceContentType;

    @NotNull
    private JustificationTypeDTO justificationType;

    @NotNull
    private UserProfileDTO student;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public byte[] getEvidence() {
        return evidence;
    }

    public void setEvidence(byte[] evidence) {
        this.evidence = evidence;
    }

    public String getEvidenceContentType() {
        return evidenceContentType;
    }

    public void setEvidenceContentType(String evidenceContentType) {
        this.evidenceContentType = evidenceContentType;
    }

    public JustificationTypeDTO getJustificationType() {
        return justificationType;
    }

    public void setJustificationType(JustificationTypeDTO justificationType) {
        this.justificationType = justificationType;
    }

    public UserProfileDTO getStudent() {
        return student;
    }

    public void setStudent(UserProfileDTO student) {
        this.student = student;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JustificationDTO)) {
            return false;
        }

        JustificationDTO justificationDTO = (JustificationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, justificationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "JustificationDTO{" +
            "id='" + getId() + "'" +
            ", description='" + getDescription() + "'" +
            ", startDate='" + getStartDate() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", evidence='" + getEvidence() + "'" +
            ", justificationType=" + getJustificationType() +
            ", student=" + getStudent() +
            "}";
    }
}
