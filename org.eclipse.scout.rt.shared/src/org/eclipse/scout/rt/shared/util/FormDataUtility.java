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
import java.util.Collection;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

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

  public static void checkMinLength(String displayName, Object value, Object minLength) throws ProcessingException {
    if (value == null || minLength == null) {
      return;
    }
    int min = ((Number) minLength).intValue();
    if (value instanceof String) {
      if (((String) value).length() < min) throw new ProcessingException(displayName + " is too short");
    }
    else if (value.getClass().isArray()) {
      if (Array.getLength(value) < min) throw new ProcessingException(displayName + " is too short");
    }
    else if (value instanceof Collection<?>) {
      if (((Collection<?>) value).size() < min) throw new ProcessingException(displayName + " is too short");
    }
    else if (value instanceof Map<?, ?>) {
      if (((Map<?, ?>) value).size() < min) throw new ProcessingException(displayName + " is too short");
    }
    else {
      if (value.toString().length() < min) throw new ProcessingException(displayName + " is too short");
    }
  }

  public static void checkMaxLength(String displayName, Object value, Object maxLength) throws ProcessingException {
    if (value == null || maxLength == null) {
      return;
    }
    int max = ((Number) maxLength).intValue();
    if (value instanceof String) {
      if (((String) value).length() > max) throw new ProcessingException(displayName + " is too long");
    }
    else if (value.getClass().isArray()) {
      if (Array.getLength(value) > max) throw new ProcessingException(displayName + " is too long");
    }
    else if (value instanceof Collection<?>) {
      if (((Collection<?>) value).size() > max) throw new ProcessingException(displayName + " is too long");
    }
    else if (value instanceof Map<?, ?>) {
      if (((Map<?, ?>) value).size() > max) throw new ProcessingException(displayName + " is too long");
    }
    else {
      if (value.toString().length() > max) throw new ProcessingException(displayName + " is too long");
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkMinValue(String displayName, Object value, Object minValue) throws ProcessingException {
    if (value == null || minValue == null) {
      return;
    }
    Comparable<? extends Object> min = ((Comparable<?>) minValue);
    if (value instanceof Comparable<?>) {
      if (((Comparable<Object>) value).compareTo(min) < 0) throw new ProcessingException(displayName + " is too small");
    }
    else {
      throw new ProcessingException(displayName + " is not comparable");
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkMaxValue(String displayName, Object value, Object maxValue) throws ProcessingException {
    if (value == null || maxValue == null) {
      return;
    }
    Comparable<? extends Object> max = ((Comparable<?>) maxValue);
    if (value instanceof Comparable<?>) {
      if (((Comparable<Object>) value).compareTo(max) > 0) throw new ProcessingException(displayName + " is too large");
    }
    else {
      throw new ProcessingException(displayName + " is not comparable");
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkCodeType(String displayName, Object codeKey, Object codeTypeClass) throws ProcessingException {
    if (codeKey == null || codeTypeClass == null) {
      return;
    }
    Class<? extends ICodeType<?>> cls = (Class<? extends ICodeType<?>>) codeTypeClass;
    ICodeType<?> codeType = CODES.getCodeType(cls);
    if (codeType == null) {
      throw new ProcessingException(displayName + " codeType " + cls.getSimpleName() + " does not exist");
    }
    if (codeType.getCode(codeKey) == null) {
      throw new ProcessingException(displayName + " " + codeKey + " is illegal for " + cls.getSimpleName());
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkLookupCall(String displayName, Object lookupKey, Object lookupCallClass) throws ProcessingException {
    if (lookupKey == null || lookupCallClass == null) {
      return;
    }
    Class<? extends LookupCall> cls = (Class<? extends LookupCall>) lookupCallClass;
    LookupCall call;
    try {
      call = cls.newInstance();
    }
    catch (Throwable t) {
      throw new ProcessingException(displayName + " can not verify " + cls.getSimpleName());
    }
    call.setKey(lookupKey);
    if (call.getDataByKey().length == 0) {
      throw new ProcessingException(displayName + " " + lookupKey + " is illegal for " + cls.getSimpleName());
    }
  }
}
