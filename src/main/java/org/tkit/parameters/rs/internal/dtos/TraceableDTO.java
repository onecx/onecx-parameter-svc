package org.tkit.parameters.rs.internal.dtos;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class TraceableDTO {

    /**
     * The ID.
     */
    private String id = UUID.randomUUID().toString();

    /**
     * Optimistic lock version
     */
    private Integer modificationCount;

    /**
     * The creation date.
     */
    private OffsetDateTime creationDate;
    /**
     * The creation user.
     */
    private String creationUser;
    /**
     * The modification date.
     */
    private OffsetDateTime modificationDate;
    /**
     * The modification user.
     */
    private String modificationUser;

    /**
     * Overwrite the {@code toString} method for the logger.
     *
     * @return the className:ID
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + getId();
    }

    /**
     * {@inheritDoc }
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TraceableDTO other = (TraceableDTO) obj;
        Object guid = getId();
        Object otherGuid = other.getId();

        if (guid == null) {
            if (otherGuid != null) {
                return false;
            } else {
                return super.equals(obj);
            }
        } else if (!guid.equals(otherGuid)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc }
     *
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getId());
        return result;
    }
}
