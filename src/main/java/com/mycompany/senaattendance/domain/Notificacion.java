package com.mycompany.senaattendance.domain;

import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A notification of one user (UC018). The delivery state ({@code estado}) and the read state
 * ({@code read}) are independent: a delivered notification can still be unread. The optional
 * reference points to the object that originated the notification (a justification, an alert),
 * so the inbox can link back to its detail.
 */
@Document(collection = "notificacion")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Notificacion extends AbstractAuditingEntity<String> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @DBRef
    @Field("user")
    private User user;

    @Field("tipo")
    private NotificacionTipo tipo;

    @Field("estado")
    private NotificacionEstado estado;

    @Size(max = 500)
    @Field("mensaje")
    private String mensaje;

    @Field("read")
    private Boolean read;

    @Field("reference_type")
    private String referenceType;

    @Field("reference_id")
    private String referenceId;

    public String getId() {
        return this.id;
    }

    public Notificacion id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return this.user;
    }

    public Notificacion user(User user) {
        this.setUser(user);
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public NotificacionTipo getTipo() {
        return this.tipo;
    }

    public Notificacion tipo(NotificacionTipo tipo) {
        this.setTipo(tipo);
        return this;
    }

    public void setTipo(NotificacionTipo tipo) {
        this.tipo = tipo;
    }

    public NotificacionEstado getEstado() {
        return this.estado;
    }

    public Notificacion estado(NotificacionEstado estado) {
        this.setEstado(estado);
        return this;
    }

    public void setEstado(NotificacionEstado estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return this.mensaje;
    }

    public Notificacion mensaje(String mensaje) {
        this.setMensaje(mensaje);
        return this;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Boolean getRead() {
        return this.read;
    }

    public Notificacion read(Boolean read) {
        this.setRead(read);
        return this;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getReferenceType() {
        return this.referenceType;
    }

    public Notificacion referenceType(String referenceType) {
        this.setReferenceType(referenceType);
        return this;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return this.referenceId;
    }

    public Notificacion referenceId(String referenceId) {
        this.setReferenceId(referenceId);
        return this;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notificacion)) {
            return false;
        }
        return getId() != null && getId().equals(((Notificacion) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Notificacion{" +
            "id=" + getId() +
            ", tipo='" + getTipo() + "'" +
            ", estado='" + getEstado() + "'" +
            ", mensaje='" + getMensaje() + "'" +
            ", read='" + getRead() + "'" +
            ", referenceType='" + getReferenceType() + "'" +
            ", referenceId='" + getReferenceId() + "'" +
            "}";
    }
}
