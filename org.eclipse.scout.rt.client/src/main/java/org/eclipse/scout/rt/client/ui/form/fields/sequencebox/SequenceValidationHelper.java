/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox;

import java.util.Arrays;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Helper for validation of a from/to field pair. Both fields must use the same value type and should have an own label
 * which is referenced in case of a validation error.
 *
 * @see AbstractFromToSequenceBox for a from/to field pair which should be grouped into a sequence box (using a common
 * label for both fields)
 */
@ApplicationScoped
public class SequenceValidationHelper {

  /**
   * Compares start field and end field. If start field is greater than end field, the current modified field gets an
   * error status.
   */
  public <T extends Comparable<T>> void checkFromTo(IValueField<T> startField, IValueField<T> endField) {
    //check null values
    if (startField.getValue() == null || endField.getValue() == null) {
      clearInvalidSequenceStatus(startField, endField);
      return;
    }
    //check if the fields are of the same type
    if (ObjectUtility.notEquals(startField.getValue().getClass(), endField.getValue().getClass())) {
      clearInvalidSequenceStatus(startField, endField);
      return;
    }
    //compare values
    if (ObjectUtility.compareTo(startField.getValue(), endField.getValue()) > 0) {
      IValueField<T> errorField = startField;
      if (endField.isValueChanging() && !StringUtility.isNullOrEmpty(endField.getLabel())) {
        errorField = endField;
      }
      InvalidSequenceStatus errorStatus = new InvalidSequenceStatus(TEXTS.get("XMustBeGreaterThanOrEqualY", startField.getLabel(), endField.getLabel()));
      errorField.addErrorStatus(errorStatus);
      return;
    }
    clearInvalidSequenceStatus(startField, endField);
  }

  protected void clearInvalidSequenceStatus(IValueField<?>... fields) {
    Arrays.stream(fields).forEach(f -> f.removeErrorStatus(InvalidSequenceStatus.class));
  }
}
