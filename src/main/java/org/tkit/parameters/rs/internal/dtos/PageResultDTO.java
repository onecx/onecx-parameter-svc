package org.tkit.parameters.rs.internal.dtos;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class PageResultDTO<T> {

    /**
     * The total elements in the database.
     */
    @Schema(description = "The total elements in the resource.")
    private long totalElements;

    /**
     * The page index.
     */
    private int number;

    /**
     * The page size.
     */
    private int size;

    /**
     * The number of pages.
     */
    private long totalPages;

    /**
     * The data stream.
     */
    private List<T> stream;

}
