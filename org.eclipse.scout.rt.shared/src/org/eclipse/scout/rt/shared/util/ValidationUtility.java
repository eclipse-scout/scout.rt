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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

/**
 * Does basic input value validation on form data fields.
 */
public final class ValidationUtility {

  private ValidationUtility() {
  }

  /**
   * check if value is an array
   */
  public static void checkArray(String displayName, Object array) throws ProcessingException {
    if (array == null || !array.getClass().isArray()) {
      throw new ProcessingException(displayName + " is no array");
    }
  }

  public static void checkMandatoryValue(String displayName, Object value) throws ProcessingException {
    if (value == null) {
      throw new ProcessingException(displayName + " is required");
    }
  }

  public static void checkMandatoryArray(String displayName, Object array) throws ProcessingException {
    checkArray(displayName, array);
    if (Array.getLength(array) == 0) {
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

  public static void checkCodeTypeValue(String displayName, Object codeKey, ICodeType<?> codeType) throws ProcessingException {
    if (codeKey == null || codeType == null) {
      return;
    }
    if (codeType.getCode(codeKey) == null) {
      throw new ProcessingException(displayName + " " + codeKey + " is illegal for " + codeType.getClass().getSimpleName());
    }
  }

  public static void checkCodeTypeArray(String displayName, Object codeKeyArray, ICodeType<?> codeType) throws ProcessingException {
    if (codeKeyArray == null || codeType == null) {
      return;
    }
    checkArray(displayName, codeKeyArray);
    int len = Array.getLength(codeKeyArray);
    if (len == 0) {
      return;
    }
    for (int i = 0; i < len; i++) {
      Object codeKey = Array.get(codeKeyArray, i);
      if (codeType.getCode(codeKey) == null) {
        throw new ProcessingException(displayName + " " + codeKey + " is illegal for " + codeType.getClass().getSimpleName());
      }
    }
  }

  public static void checkLookupCallValue(String displayName, Object lookupKey, LookupCall call) throws ProcessingException {
    if (lookupKey == null || call == null) {
      return;
    }
    call.setKey(lookupKey);
    if (call.getDataByKey().length == 0) {
      throw new ProcessingException(displayName + " " + lookupKey + " is illegal for " + call.getClass().getSimpleName());
    }
  }

  public static void checkLookupCallArray(String displayName, Object lookupKeyArray, LookupCall call) throws ProcessingException {
    if (lookupKeyArray == null || call == null) {
      return;
    }
    checkArray(displayName, lookupKeyArray);
    int len = Array.getLength(lookupKeyArray);
    if (len == 0) {
      return;
    }
    for (int i = 0; i < len; i++) {
      Object lookupKey = Array.get(lookupKeyArray, i);
      call.setKey(lookupKey);
      if (call.getDataByKey().length == 0) {
        throw new ProcessingException(displayName + " " + lookupKey + " is illegal for " + call.getClass().getSimpleName());
      }
    }
  }

  /**
   * Checks if the string value (if not null) matches the regex.
   * If the regex is a string, the pattern created uses case insensitive {@link Pattern#CASE_INSENSITIVE} and
   * full-text-scan {@link Pattern#DOTALL}.
   * 
   * @param displayName
   * @param value
   * @param regex
   *          either a string or a prepared {@link Pattern}
   */
  public static void checkValueMatchesRegex(String displayName, Object value, Object regex) throws ProcessingException {
    if (value == null || regex == null) {
      return;
    }
    if (!(value instanceof String)) {
      throw new ProcessingException(displayName + " value is no string");
    }
    Pattern p;
    if (regex instanceof String) {
      p = Pattern.compile((String) regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    }
    else if (regex instanceof Pattern) {
      p = (Pattern) regex;
    }
    else {
      throw new ProcessingException(displayName + " regex is neither String nor Pattern");
    }
    //
    if (!p.matcher((String) value).matches()) {
      throw new ProcessingException(displayName + " is not valid");
    }
  }

  /**
   * see {@link #checkValueMatchesRegex(String, Object, Object)}
   */
  public static void checkArrayMatchesRegex(String displayName, Object arrayOfStrings, Object regex) throws ProcessingException {
    if (arrayOfStrings == null || regex == null) {
      return;
    }
    checkArray(displayName, arrayOfStrings);
    int len = Array.getLength(arrayOfStrings);
    if (len == 0) {
      return;
    }
    Pattern p;
    if (regex instanceof String) {
      p = Pattern.compile((String) regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    }
    else if (regex instanceof Pattern) {
      p = (Pattern) regex;
    }
    else {
      throw new ProcessingException(displayName + " regex is neither String nor Pattern");
    }
    //
    for (int i = 0; i < len; i++) {
      Object value = Array.get(arrayOfStrings, i);
      if (!(value instanceof String)) {
        throw new ProcessingException(displayName + " value is no string");
      }
      if (!p.matcher((String) value).matches()) {
        throw new ProcessingException(displayName + " is not valid");
      }
    }
  }

  /**
   * Traverse all objects by writing the object to a
   * void stream and calling {@link #visitObject(Object)} on every traversed Object in the hierarchy.
   */
  public static abstract class ObjectTreeVisitor extends ObjectOutputStream {
    public ObjectTreeVisitor() throws IOException {
      super(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
          //nop
        }
      });
      enableReplaceObject(true);
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
      try {
        boolean goOn = visitObject(obj);
        if (!goOn) {
          return null;
        }
      }
      catch (Throwable t) {
        //java issue throwing an IOException on top level writes an ioexception to the stream, we don't want that
        throw new ObjectTreeVisitorMarkerException(t);
      }
      return obj;
    }

    /**
     * @return true to continue visiting the subtree of this object or false to skip visiting the suptree of this
     *         object.
     */
    protected abstract boolean visitObject(Object obj) throws Exception;
  }

  public static final class ObjectTreeVisitorMarkerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ObjectTreeVisitorMarkerException(Throwable cause) {
      super(cause);
    }
  }

}
