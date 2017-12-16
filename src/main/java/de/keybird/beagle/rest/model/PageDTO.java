/**
 * This file is part of Beagle.
 * Copyright (c) 2017 Markus von Rüden.
 *
 * Beagle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beagle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beagle. If not, see http://www.gnu.org/licenses/.
 */

package de.keybird.beagle.rest.model;

import java.util.Objects;

import de.keybird.beagle.api.Page;
import de.keybird.beagle.api.PageState;

public class PageDTO {

    private Long id;

    private String name;

    private PageState state;

    private String checksum;

    private String errorMessage;

    private int pageNumber;

    public PageDTO() {

    }

    public PageDTO(Page page) {
        setChecksum(page.getChecksum());
        setErrorMessage(page.getErrorMessage());
        setId(page.getId());
        setName(page.getName());
        setPageNumber(page.getPageNumber());
        setState(page.getState());
    }

    // TODO MVR add payload URL
//    private byte[] payload;

    // TODO MVR add thumbnail URL
//    private byte[] thumbnail;

    // TODO MVR add document URL
//    private Long documentId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PageState getState() {
        return state;
    }

    public void setState(PageState state) {
        this.state = state;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PageDTO pageDTO = (PageDTO) o;
        final boolean equals = Objects.equals(pageNumber, pageDTO.pageNumber)
                && Objects.equals(id, pageDTO.id)
                && Objects.equals(name, pageDTO.name)
                && Objects.equals(state, pageDTO.state)
                && Objects.equals(checksum, pageDTO.checksum)
                && Objects.equals(errorMessage, pageDTO.errorMessage);
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, state, checksum, errorMessage, pageNumber);
    }
}
