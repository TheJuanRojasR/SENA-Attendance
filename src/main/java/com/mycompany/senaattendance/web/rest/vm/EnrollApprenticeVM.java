package com.mycompany.senaattendance.web.rest.vm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * View Model used to enroll an apprentice in a ficha (UC008).
 *
 * <p>The apprentice is identified by its {@code documentNumber} instead of a
 * {@link com.mycompany.senaattendance.domain.UserProfile} id: the backend resolves the
 * profile from it. The document format mirrors the one accepted by self-registration
 * (UC001): numeric only, up to 30 characters.
 *
 * <p>The ficha keeps the nested {@code grade: {id}} shape already used by the
 * apprentices REST contract. The academic state is not part of the request: the server
 * always enrolls the apprentice as Matriculado.
 */
public class EnrollApprenticeVM {

    @NotBlank
    @Size(min = 1, max = 30)
    @Pattern(regexp = "\\d+")
    private String documentNumber;

    @NotNull
    @Valid
    private GradeIdVM grade;

    public EnrollApprenticeVM() {
        // Empty constructor needed for Jackson.
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public GradeIdVM getGrade() {
        return grade;
    }

    public void setGrade(GradeIdVM grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "EnrollApprenticeVM{documentNumber='" + documentNumber + "', grade=" + grade + "}";
    }
}
