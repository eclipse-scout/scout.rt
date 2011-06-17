/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.util;

import java.lang.reflect.Array;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * Does basic input value validation on form data fields.
 */
public final class FormDataUtility {

  private FormDataUtility() {
  }

  public static void checkRequired(String displayName, Object value) throws ProcessingException {
    if (value == null) {
      throw new ProcessingException(displayName + " is required");
    }
  }

  /**
   * Validate a string.
   * <p>
   * No check is performed if {@link AbstractPropertyData#isValueSet()}=false
   * <p>
   * When the field type is not string a validation exception is thrown
   * <p>
   * When the field value is not valid a validation exception is thrown
   * <p>
   * see {@link #validateStringValue(String, boolean, Integer)}
   */
  public static void validateStringProperty(AbstractPropertyData<?> field, boolean required, Integer maxLength) throws ProcessingException {
    if (field == null || (!required && !field.isValueSet())) {
      return;
    }
    validateStringValue(field.getClass().getSimpleName(), (String) field.getValue(), required, maxLength);
  }

  /**
   * Validate a string.
   * <p>
   * No check is performed if {@link AbstractValueFieldData<?>#isValueSet()}=false
   * <p>
   * When the field type is not string a validation exception is thrown
   * <p>
   * When the field value is not valid a validation exception is thrown
   * <p>
   * see {@link #validateStringValue(String, boolean, Integer)}
   */
  public static void validateStringField(AbstractValueFieldData<?> field, boolean required, Integer maxLength) throws ProcessingException {
    if (field == null || (!required && !field.isValueSet())) {
      return;
    }
    validateStringValue(field.getClass().getSimpleName(), (String) field.getValue(), required, maxLength);
  }

  /**
   * Validate a string.
   * <p>
   * When the value is not valid a validation exception is thrown
   */
  public static void validateStringValue(String displayName, String value, boolean required, Integer maxLength) throws ProcessingException {
    if (required) {
      checkRequired(displayName, value);
    }
    if (value == null) {
      return;
    }
    if (maxLength != null && value.length() > maxLength) {
      throw new ProcessingException(displayName + " is too long");
    }
  }

  /**
   * Validate the number.
   * <p>
   * No check is performed if {@link AbstractPropertyData#isValueSet()}=false
   * <p>
   * When the field type is not a numeric type, a validation exception is thrown
   * <p>
   * When the field value is not valid a validation exception is thrown
   * <p>
   * see {@link #validateLongValue(Long, boolean, Long, Long)}
   */
  public static void validateLongProperty(AbstractPropertyData<?> field, boolean required, Long min, Long max) throws ProcessingException {
    if (field == null || (!required && !field.isValueSet())) {
      return;
    }
    validateLongValue(field.getClass().getSimpleName(), (Long) field.getValue(), required, min, max);
  }

  /**
   * Validate the number.
   * <p>
   * No check is performed if {@link AbstractValueFieldData<?>#isValueSet()}=false
   * <p>
   * When the field type is not a numeric type, a validation exception is thrown
   * <p>
   * When the field value is not valid a validation exception is thrown
   * <p>
   * see {@link #validateLongValue(Long, boolean, Long, Long)}
   */
  public static void validateLongField(AbstractValueFieldData<?> field, boolean required, Long min, Long max) throws ProcessingException {
    if (field == null || (!required && !field.isValueSet())) {
      return;
    }
    validateLongValue(field.getClass().getSimpleName(), (Long) field.getValue(), required, min, max);
  }

  /**
   * Validate the number.
   * <p>
   * When the value is not valid a validation exception is thrown
   */
  public static void validateLongValue(String displayName, Long value, boolean required, Long min, Long max) throws ProcessingException {
    if (required) {
      checkRequired(displayName, value);
    }
    if (value == null) {
      return;
    }
    if (min != null && value < min) {
      throw new ProcessingException(displayName + " " + value + " is outside range");
    }
    if (max != null && value > max) {
      throw new ProcessingException(displayName + " " + value + " is outside range");
    }
  }

  /**
   * Validate the value.
   * <p>
   * No check is performed if {@link AbstractPropertyData#isValueSet()}=false
   * <p>
   * When the field type is not a numeric type, then a validation exception is thrown
   * <p>
   * When the field value is not valid a validation exception is thrown
   * <p>
   * see {@link #validateDoubleValue(Double, boolean, Double, Double)}
   */
  public static void validateDoubleProperty(AbstractPropertyData<?> field, boolean required, Double min, Double max) throws ProcessingException {
    if (field == null || (!required && !field.isValueSet())) {
      return;
    }
    validateDoubleValue(field.getClass().getSimpleName(), (Double) field.getValue(), required, min, max);
  }

  /**
   * Validate the value.
   * <p>
   * No check is performed if {@link AbstractValueFieldData<?>#isValueSet()}=false
   * <p>
   * When the field type is not a numeric type, then a validation exception is thrown
   * <p>
   * When the field value is not valid a validation exception is thrown
   * <p>
   * see {@link #validateDoubleValue(Double, boolean, Double, Double)}
   */
  public static void validateDoubleField(AbstractValueFieldData<?> field, boolean required, Double min, Double max) throws ProcessingException {
    if (field == null || (!required && !field.isValueSet())) {
      return;
    }
    validateDoubleValue(field.getClass().getSimpleName(), (Double) field.getValue(), required, min, max);
  }

  /**
   * Validate the value.
   * <p>
   * When the value is not valid a validation exception is thrown
   */
  public static void validateDoubleValue(String displayName, Double value, boolean required, Double min, Double max) throws ProcessingException {
    if (required) {
      checkRequired(displayName, value);
    }
    if (value == null) {
      return;
    }
    if (min != null && value < min) {
      throw new ProcessingException(displayName + " " + value + " is outside range");
    }
    if (max != null && value > max) {
      throw new ProcessingException(displayName + " " + value + " is outside range");
    }
  }

  /**
   * Validate the code key.
   * <p>
   * No check is performed if {@link AbstractPropertyData#isValueSet()}=false
   * <p>
   * When the field value is not valid a validation exception is thrown
   * <p>
   * see {@link #validateCodeValue(Object, boolean, Class)}
   */
  public static void validateCodeProperty(AbstractPropertyData<?> field, boolean required, Class<? extends ICodeType> codeTypeClass) throws ProcessingException {
    if (field == null || (!required && !field.isValueSet())) {
      return;
    }
    validateCodeValue(field.getClass().getSimpleName(), field.getValue(), required, codeTypeClass);
  }

  /**
   * Validate the code key.
   * <p>
   * No check is performed if {@link AbstractValueFieldData<?>#isValueSet()}=false
   * <p>
   * When the field value is not valid a validation exception is thrown
   * <p>
   * see {@link #validateCodeValue(Object, boolean, Class)}
   */
  public static void validateCodeField(AbstractValueFieldData<?> field, boolean required, Class<? extends ICodeType> codeTypeClass) throws ProcessingException {
    if (field == null || (!required && !field.isValueSet())) {
      return;
    }
    validateCodeValue(field.getClass().getSimpleName(), field.getValue(), required, codeTypeClass);
  }

  /**
   * Validate the code key.
   * <p>
   * When the value is not valid a validation exception is thrown
   */
  public static void validateCodeValue(String displayName, Object key, boolean required, Class<? extends ICodeType> codeTypeClass) throws ProcessingException {
    if (required) {
      checkRequired(displayName, key);
    }
    if (key == null) {
      return;
    }
    if (codeTypeClass != null) {
      ICodeType<?> codeType = CODES.getCodeType(codeTypeClass);
      if (codeType.getCode(key) == null) {
        throw new ProcessingException(displayName + " " + key + " is illegal");
      }
    }
  }

  /**
   * Validate an array and its length.
   * <p>
   * No check is performed if {@link AbstractPropertyData#isValueSet()}=false
   * <p>
   * When the field type is not an array validation exception is thrown
   * <p>
   * When the field value is not valid a validation exception is thrown
   * <p>
   * see {@link #validateStringValue(String, boolean, Integer)}
   */
  public static void validateArrayProperty(AbstractPropertyData<?> field, boolean required, Integer maxLength) throws ProcessingException {
    if (field == null || (!required && !field.isValueSet())) {
      return;
    }
    validateArrayValue(field.getClass().getSimpleName(), field.getValue(), required, maxLength);
  }

  /**
   * Validate an array and its length.
   * <p>
   * No check is performed if {@link AbstractValueFieldData<?>#isValueSet()}=false
   * <p>
   * When the field type is not an array a validation exception is thrown
   * <p>
   * When the field value is not valid a validation exception is thrown
   * <p>
   * see {@link #validateStringValue(String, boolean, Integer)}
   */
  public static void validateArrayField(AbstractValueFieldData<?> field, boolean required, Integer maxLength) throws ProcessingException {
    if (field == null || (!required && !field.isValueSet())) {
      return;
    }
    validateArrayValue(field.getClass().getSimpleName(), field.getValue(), required, maxLength);
  }

  /**
   * Validate an array and its length.
   * <p>
   * When the value is not valid a validation exception is thrown
   */
  public static void validateArrayValue(String displayName, Object value, boolean required, Integer maxLength) throws ProcessingException {
    if (required) {
      checkRequired(displayName, value);
    }
    if (value == null) {
      return;
    }
    if (!value.getClass().isArray()) {
      throw new ProcessingException(displayName + " is not an array");
    }
    if (maxLength != null && Array.getLength(value) > maxLength) {
      throw new ProcessingException(displayName + " is too long");
    }
  }
}
