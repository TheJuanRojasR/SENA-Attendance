package com.mycompany.senaattendance.web.rest.vm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * View Model used to set the activation status of an existing
 * {@link com.mycompany.senaattendance.domain.Program}.
 *
 * <p>Carries the unique program {@code id} and the target {@code status} value.
 * {@code status} is read with a strict boolean deserializer so only JSON
 * {@code true}/{@code false} are accepted: numeric values ({@code 1}/{@code 0}),
 * strings or any other type are rejected while reading the request body.
 */
public class SetProgramActivatedVM {

    @NotBlank
    private String id;

    @NotNull
    @JsonDeserialize(using = StrictBooleanDeserializer.class)
    private Boolean status;

    public SetProgramActivatedVM() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SetProgramActivatedVM{id='" + id + "', status=" + status + "}";
    }

    /**
     * Strictly reads JSON {@code true}/{@code false}. Any other token (number,
     * string, etc.) is rejected so that {@code 1}/{@code 0} are never accepted.
     */
    public static class StrictBooleanDeserializer extends ValueDeserializer<Boolean> {

        @Override
        public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            JsonToken token = p.currentToken();
            if (token == JsonToken.VALUE_TRUE) {
                return Boolean.TRUE;
            }
            if (token == JsonToken.VALUE_FALSE) {
                return Boolean.FALSE;
            }
            return ctxt.reportInputMismatch(this, "Status must be a boolean value: true or false");
        }
    }
}
