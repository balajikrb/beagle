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

package de.keybird.beagle.elastic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.searchbox.core.BulkResult;

public class BulkResultWrapper {

    private final BulkResult result;

    public BulkResultWrapper(BulkResult result) {
        this.result = Objects.requireNonNull(result);
    }

    public boolean isSucceeded() {
        return result.isSucceeded();
    }

    public String getErrorMessage() {
        return result.getErrorMessage();
    }

    public <T> List<FailedItem<T>> getFailedItems(List<T> items) {
        final List<FailedItem<T>> failedItems = new ArrayList<>();
        for (int i=0; i<result.getItems().size(); i++) {
            final BulkResult.BulkResultItem bulkResultItem = result.getItems().get(i);
            if (bulkResultItem.error != null && !bulkResultItem.error.isEmpty()) {
                final Exception cause = convertToException(bulkResultItem.error);
                final T failedObject = items.get(i);
                final FailedItem failedItem = new FailedItem(failedObject, cause);
                failedItems.add(failedItem);
            }
        }
        return failedItems;
    }

    public <T> List<T> getSuccessItems(List<T> items) {
        final List<T> successItems = new ArrayList<>();
        for (int i=0; i<result.getItems().size(); i++) {
            final BulkResult.BulkResultItem bulkResultItem = result.getItems().get(i);
            if (bulkResultItem.error == null || "".equalsIgnoreCase(bulkResultItem.error)) {
                final T item = items.get(i);
                successItems.add(item);
            }
        }
        return successItems;
    }

    private static Exception convertToException(String error) {
        // Read error data
        final JsonObject errorObject = new JsonParser().parse(error).getAsJsonObject();
        final String errorType = errorObject.get("type").getAsString();
        final String errorReason = errorObject.get("reason").getAsString();
        final JsonElement errorCause = errorObject.get("caused_by");

        // Create Exception
        final String errorMessage = String.format("%s: %s", errorType, errorReason);
        if (errorCause != null) {
            return new Exception(errorMessage, convertToException(errorCause.toString()));
        }
        return new Exception(errorMessage);
    }
}
