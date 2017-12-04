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

package de.keybird.beagle.api;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="import")
public class Import {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String filename;

    private String checksum;

    private String error;

    private int pageCount;

    private ImportState state = ImportState.New;

    @Temporal(TemporalType.TIMESTAMP)
    private Date importDate;

    @Lob
    private byte[] payload;

    public Import() {

    }

    public Import(Import anImport) {
        this.id = anImport.id;
        this.filename = anImport.filename;
        this.checksum = anImport.checksum;
        this.error = anImport.error;
        this.pageCount = anImport.pageCount;
        this.importDate = anImport.importDate;
        this.state = anImport.state;
        this.payload = anImport.payload != null ? Arrays.copyOf(anImport.payload, anImport.payload.length) : null;
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public void setErrorMessage(String message) {
        this.error = message;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setPageCount(int count) {
        this.pageCount = count;
    }

    public int getPageCount() {
        return pageCount;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public ImportState getState() {
        return state;
    }

    public void setState(ImportState state) {
        this.state = state;
    }
}
