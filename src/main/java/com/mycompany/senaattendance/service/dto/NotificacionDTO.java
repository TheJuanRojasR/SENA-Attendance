package com.mycompany.senaattendance.service.dto;

import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.Notificacion} entity. The delivery
 * state ({@code estado}) and the read state ({@code read}) are independent, and the reference
 * points to the object that originated the notification (UC018).
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NotificacionDTO implements Serializable {

    private String id;

    private NotificacionTipo tipo;

    @Size(max = 500)
    private String mensaje;

    private NotificacionEstado estado;

    private Boolean read;

    private String referenceType;

    private String referenceId;

    private Instant createdDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NotificacionTipo getTipo() {
        return tipo;
    }

    public void setTipo(NotificacionTipo tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public NotificacionEstado getEstado() {
        return estado;
    }

    public void setEstado(NotificacionEstado estado) {
        this.estado = estado;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotificacionDTO)) {
            return false;
        }
        NotificacionDTO notificacionDTO = (NotificacionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, notificacionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NotificacionDTO{" +
            "id='" + getId() + "'" +
            ", tipo='" + getTipo() + "'" +
            ", mensaje='" + getMensaje() + "'" +
            ", estado='" + getEstado() + "'" +
            ", read='" + getRead() + "'" +
            ", referenceType='" + getReferenceType() + "'" +
            ", referenceId='" + getReferenceId() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            "}";
    }
}
