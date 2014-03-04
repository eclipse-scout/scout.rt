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
package org.eclipse.scout.rt.shared.validate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.validate.annotations.FieldReference;
import org.eclipse.scout.rt.shared.validate.annotations.ValidateAnnotationMarker;

/**
 * Does basic input value validation on form data fields.
 */
public final class ValidationUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ValidationUtility.class);

  @SuppressWarnings("unchecked")
  public static final Collection<Annotation> NO_ANNOTATIONS = Collections.unmodifiableList(Collections.EMPTY_LIST);

  private ValidationUtility() {
  }

  /**
   * @return null iff value is numeric and 0, otherwise teh original value
   */
  public static Object treat0AsNull(Object value) {
    if (value != null) {
      // equality is provided and a value is set
      // if provided value corresponds to 0, provided null to the check method (reject validation)
      if (value instanceof Number) {
        if (((Number) value).longValue() == 0L) {
          return null;
        }
      }
    }
    return value;
  }

  /**
   * check if value is an array
   */
  public static void checkArray(Object array) throws ProcessingException {
    if (array == null || !array.getClass().isArray()) {
      throw new ProcessingException("value  is no array");
    }
  }

  public static void checkMandatoryValue(Object value) throws ProcessingException {
    if (value == null) {
      throw new ProcessingException("value  is required");
    }
  }

  public static void checkMandatoryArray(Object array) throws ProcessingException {
    checkArray(array);
    if (Array.getLength(array) == 0) {
      throw new ProcessingException("value  is required");
    }
  }

  public static void checkMinLength(Object value, Object minLength) throws ProcessingException {
    if (value == null || minLength == null) {
      return;
    }
    int min = ((Number) minLength).intValue();
    if (value instanceof String) {
      if (((String) value).length() < min) {
        throw new ProcessingException("value  is too short");
      }
    }
    else if (value.getClass().isArray()) {
      if (Array.getLength(value) < min) {
        throw new ProcessingException("value  is too short");
      }
    }
    else if (value instanceof Collection<?>) {
      if (((Collection<?>) value).size() < min) {
        throw new ProcessingException("value  is too short");
      }
    }
    else if (value instanceof Map<?, ?>) {
      if (((Map<?, ?>) value).size() < min) {
        throw new ProcessingException("value  is too short");
      }
    }
    else {
      if (value.toString().length() < min) {
        throw new ProcessingException("value  is too short");
      }
    }
  }

  public static void checkMaxLength(Object value, Object maxLength) throws ProcessingException {
    if (value == null || maxLength == null) {
      return;
    }
    int max = ((Number) maxLength).intValue();
    if (value instanceof String) {
      if (((String) value).length() > max) {
        throw new ProcessingException("value  is too long");
      }
    }
    else if (value.getClass().isArray()) {
      if (Array.getLength(value) > max) {
        throw new ProcessingException("value  is too long");
      }
    }
    else if (value instanceof Collection<?>) {
      if (((Collection<?>) value).size() > max) {
        throw new ProcessingException("value  is too long");
      }
    }
    else if (value instanceof Map<?, ?>) {
      if (((Map<?, ?>) value).size() > max) {
        throw new ProcessingException("value  is too long");
      }
    }
    else {
      if (value.toString().length() > max) {
        throw new ProcessingException("value  is too long");
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkMinValue(Object value, Object minValue) throws ProcessingException {
    if (value == null || minValue == null) {
      return;
    }
    minValue = TypeCastUtility.castValue(minValue, value.getClass());
    if (((Comparable) value).compareTo(minValue) < 0) {
      throw new ProcessingException("value  is too small");
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkMaxValue(Object value, Object maxValue) throws ProcessingException {
    if (value == null || maxValue == null) {
      return;
    }
    maxValue = TypeCastUtility.castValue(maxValue, value.getClass());
    if (((Comparable) value).compareTo(maxValue) > 0) {
      throw new ProcessingException("value  is too large");
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkCodeTypeValue(Object codeKey, ICodeType codeType) throws ProcessingException {
    if (codeKey == null || codeType == null) {
      return;
    }
    if (codeType.getCode(codeKey) == null) {
      throw new ProcessingException("value  " + codeKey + " is illegal for " + codeType.getClass().getSimpleName());
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkCodeTypeSet(Object codeKeySet, ICodeType codeType) throws ProcessingException {
    if (codeKeySet == null || codeType == null) {
      return;
    }
    if (!(codeKeySet instanceof Set<?>)) {
      throw new ProcessingException("value  is not a Set");
    }
    Set<?> codeKeys = (Set<?>) codeKeySet;
    if (codeKeys.isEmpty()) {
      return;
    }
    for (Object codeKey : codeKeys) {
      if (codeType.getCode(codeKey) == null) {
        throw new ProcessingException("value  " + codeKey + " is illegal for " + codeType.getClass().getSimpleName());
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkLookupCallValue(Object lookupKey, ILookupCall call) throws ProcessingException {
    if (lookupKey == null || call == null) {
      return;
    }
    call.setKey(lookupKey);
    if (call.getDataByKey().size() == 0) {
      throw new ProcessingException("value  " + lookupKey + " is illegal for " + call.getClass().getSimpleName());
    }
  }

  @SuppressWarnings("unchecked")
  public static void checkLookupCallSet(Object lookupKeySet, ILookupCall call) throws ProcessingException {
    if (lookupKeySet == null || call == null) {
      return;
    }
    if (!(lookupKeySet instanceof Set<?>)) {
      throw new ProcessingException("value  is not a set");
    }
    Set<?> lookupKeys = (Set<?>) lookupKeySet;
    if (lookupKeys.isEmpty()) {
      return;
    }
    for (Object lookupKey : lookupKeys) {
      call.setKey(lookupKey);
      if (call.getDataByKey().size() == 0) {
        throw new ProcessingException("value  " + lookupKey + " is illegal for " + call.getClass().getSimpleName());
      }
    }
  }

  /**
   * Checks if the string value (if not null) matches the regex.
   * If the regex is a string, the pattern created uses case insensitive {@link Pattern#CASE_INSENSITIVE} and
   * full-text-scan {@link Pattern#DOTALL}.
   * 
   * @param value
   * @param regex
   */
  public static void checkRegexMatchValue(Object value, Pattern regex) throws ProcessingException {
    if (value == null || regex == null) {
      return;
    }
    if (!(value instanceof String)) {
      throw new ProcessingException("value  value is no string");
    }
    //
    if (!regex.matcher((String) value).matches()) {
      throw new ProcessingException("value  is not valid");
    }
  }

  /**
   * see {@link #checkRegexMatchValue(Object, Pattern)}
   */
  public static void checkRegexMatchArray(Object arrayOfStrings, Pattern regex) throws ProcessingException {
    if (arrayOfStrings == null || regex == null) {
      return;
    }
    checkArray(arrayOfStrings);
    int len = Array.getLength(arrayOfStrings);
    if (len == 0) {
      return;
    }
    for (int i = 0; i < len; i++) {
      Object value = Array.get(arrayOfStrings, i);
      if (!(value instanceof String)) {
        throw new ProcessingException("value  value is no string");
      }
      if (!regex.matcher((String) value).matches()) {
        throw new ProcessingException("value  is not valid");
      }
    }
  }

  private static final HashMap<Class<?>, ClassMetaData> classMetaDataCache = new HashMap<Class<?>, ValidationUtility.ClassMetaData>();
  private static final ClassMetaData NO_CLASS_META_DATA = new ClassMetaData();

  /**
   * Collect list of annotations for each parameter based on the method declaration and all its super class method
   * declarations.
   * <p>
   * Since java knows multiple inheritance with interfaces and even loops, there is no deterministic way of collecting
   * annotations for a method. The default implementation collects the validation annotations in the order as
   * illustrated in the following example with the method "hello" that is overridden in every possible level.
   * 
   * <pre>
   * interface I1a {
   *   void hello(@MaxLength(1) String s);
   * }
   * 
   * interface I1b {
   *   void hello(@MaxLength(2) String s);
   * }
   * 
   * interface I1c {
   *   void hello(@MaxLength(3) String s);
   * }
   * 
   * class A1 implements I1a, I1b, I1c {
   *   void hello(@MaxLength(4) String s) {
   *   }
   * }
   * 
   * interface I2a extends I1a {
   *   void hello(@MaxLength(5) String s);
   * }
   * 
   * interface I2b extends I1a {
   *   void hello(@MaxLength(6) String s);
   * }
   * 
   * interface I2c extends I1a {
   *   void hello(@MaxLength(7) String s);
   * }
   * 
   * class A2 extends A1 implements I2a, I2b, I2c {
   *   void hello(@MaxLength(8) String s) {
   *   }
   * }
   * </pre>
   * 
   * The order of annotation traversal for A2.hello is A2, I2a, I2b, I2c, A1, I1a, I1b, I1c. This leads to using the
   * validation MaxLength(8).
   * <p>
   * The basic traversal ruls is
   * <ul>
   * <li>declaring class C of method</li>
   * <li>direct (first level) of implemented interfaces of C</li>
   * <li>super class of C</li>
   * <li>recursive up the superclass hierarchy</li>
   * </ul>
   */
  @SuppressWarnings("unchecked")
  public static Collection<Annotation>[] getParameterAnnotations(Method m) {
    MethodMetaData meta = getMethodMetaData(m);
    if (meta == NO_METHOD_META_DATA) {
      return new Collection[0];
    }
    //create a safe copy
    Collection<Annotation>[] copy = new Collection[meta.paramAnnotationList.length];
    System.arraycopy(meta.paramAnnotationList, 0, copy, 0, copy.length);
    return copy;
  }

  /**
   * cache for Class->ClassMetaData
   */
  @SuppressWarnings("unchecked")
  private static synchronized ClassMetaData getClassMetaData(Class<?> c) {
    ClassMetaData meta = classMetaDataCache.get(c);
    if (meta != null) {
      return meta;
    }
    HashMap<Field, List<Annotation>> fieldMap = new HashMap<Field, List<Annotation>>();
    HashMap<String, List<Annotation>> phantomFieldMap = new HashMap<String, List<Annotation>>();
    Class<?> tmp = c;
    while (tmp != null && tmp != Object.class) {
      Field[] fields = tmp.getDeclaredFields();
      if (fields != null && fields.length > 0) {
        for (Field field : fields) {
          FieldReference refAnnotation = field.getAnnotation(FieldReference.class);
          if ((refAnnotation != null) || (!field.isSynthetic() && (field.getModifiers() & Modifier.STATIC) == 0)) {
            ArrayList<Annotation> aList = new ArrayList<Annotation>();
            Annotation[] aArray = field.getAnnotations();
            if (aArray != null) {
              for (Annotation a : aArray) {
                if (a.annotationType().getAnnotation(ValidateAnnotationMarker.class) != null) {
                  aList.add(a);
                }
              }
            }
            if (refAnnotation != null) {
              String fname = field.getAnnotation(FieldReference.class).value();
              phantomFieldMap.put(fname, aList);
            }
            else {
              fieldMap.put(field, aList);
            }
          }
        }
      }
      //next
      tmp = tmp.getSuperclass();
    }
    if (phantomFieldMap.size() > 0) {
      for (Map.Entry<Field, List<Annotation>> entry : fieldMap.entrySet()) {
        List<Annotation> aList = phantomFieldMap.get(entry.getKey().getName());
        if (aList != null) {
          entry.getValue().addAll(aList);
        }
      }
      //warn for missing phantom fields
      for (Field f : fieldMap.keySet()) {
        phantomFieldMap.remove(f.getName());
      }
      for (String fname : phantomFieldMap.keySet()) {
        LOG.warn("The phantom field " + c.getName() + "#" + fname + " points to @FieldReference(" + fname + ") that does not exist");
      }
    }
    if (fieldMap.size() == 0) {
      meta = NO_CLASS_META_DATA;
    }
    else {
      meta = new ClassMetaData();
      meta.fields = new Field[fieldMap.size()];
      meta.fieldAnnotationList = new Collection[fieldMap.size()];
      int index = 0;
      for (Map.Entry<Field, List<Annotation>> entry : fieldMap.entrySet()) {
        meta.fields[index] = entry.getKey();
        Collection<Annotation> value = entry.getValue();
        if (value.size() == 0) {
          meta.fieldAnnotationList[index] = NO_ANNOTATIONS;
        }
        else {
          //make collection safe
          meta.fieldAnnotationList[index] = Collections.unmodifiableCollection(value);
        }
        //next index
        index++;
      }
    }
    classMetaDataCache.put(c, meta);
    return meta;
  }

  private static final HashMap<Method, MethodMetaData> methodMetaDataCache = new HashMap<Method, MethodMetaData>();
  private static final MethodMetaData NO_METHOD_META_DATA = new MethodMetaData();

  /**
   * cache for Method->MethodMetaData
   */
  @SuppressWarnings("unchecked")
  private static synchronized MethodMetaData getMethodMetaData(Method m) {
    MethodMetaData meta = methodMetaDataCache.get(m);
    if (meta != null) {
      return meta;
    }
    String name = m.getName();
    Class<?>[] types = m.getParameterTypes();
    if (types == null || types.length == 0) {
      methodMetaDataCache.put(m, NO_METHOD_META_DATA);
      return NO_METHOD_META_DATA;
    }
    int paramCount = types.length;
    Class<?> c = m.getDeclaringClass();
    ArrayList<Annotation[][]> stack = new ArrayList<Annotation[][]>();
    while (c != null && c != Object.class) {
      //check class
      try {
        stack.add(c.getMethod(name, types).getParameterAnnotations());
      }
      catch (Throwable t) {
        //method not found
      }
      //check direct declared interfaces
      Class<?>[] ifs = c.getInterfaces();
      if (ifs != null && ifs.length > 0) {
        for (Class<?> i : ifs) {
          try {
            stack.add(i.getMethod(name, types).getParameterAnnotations());
          }
          catch (Throwable t) {
            //method not found
          }
        }
      }
      //super class
      c = c.getSuperclass();
    }
    ArrayList<Annotation>[] annListPerParam = new ArrayList[paramCount];
    for (int p = 0; p < paramCount; p++) {
      annListPerParam[p] = new ArrayList<Annotation>(0);
    }
    for (int s = stack.size() - 1; s >= 0; s--) {
      Annotation[][] elem = stack.get(s);
      if (elem == null) {
        continue;
      }
      for (int p = 0; p < paramCount; p++) {
        for (Annotation a : elem[p]) {
          if (a.annotationType().getAnnotation(ValidateAnnotationMarker.class) != null) {
            annListPerParam[p].add(a);
          }
        }
      }
    }
    //create safe collections
    meta = new MethodMetaData();
    meta.paramAnnotationList = new Collection[paramCount];
    for (int p = 0; p < paramCount; p++) {
      meta.paramAnnotationList[p] = Collections.unmodifiableCollection(annListPerParam[p]);
    }
    methodMetaDataCache.put(m, meta);
    return meta;
  }

  /**
   * Traverse all objects in the subtree and call {@link #visitObject(Object, Annotation[])} on every traversed Object
   * in the
   * hierarchy. For fields the annotations are collected (including annotations placed on types with reference to the
   * field).
   * <p>
   * Override {@link #visitObject(Object, Annotation[])} for custom handling.
   */
  public static abstract class ValidateTreeVisitor {
    private HashMap<Field, Annotation[]> m_fieldToAnnotationsCache;
    private HashSet<Object> m_markedSet;

    public ValidateTreeVisitor() {
      m_fieldToAnnotationsCache = new HashMap<Field, Annotation[]>();
    }

    public void start(Object obj, Collection<Annotation> annotations) throws Exception {
      m_markedSet = new HashSet<Object>();
      processNode(obj, annotations == null ? NO_ANNOTATIONS : annotations);
      m_markedSet = null;
    }

    /**
     * Visit an object in the composite object tree.
     * If you want the visitor to continue and visit the subtree of this object, then call visitSubTree(), otherwiese
     * simply return to not visit the subtree under this Object.
     * <p>
     * Override this method for custom handling
     */
    protected void visitObject(Object obj, Collection<Annotation> annotationList) throws Exception {
      visitSubTree(obj);
    }

    /**
     * mark object so it is not visited again. objects passed to {@link #visitObject(Object)} are already marked.
     */
    protected void markObject(Object obj) throws Exception {
      m_markedSet.add(obj);
    }

    /**
     * visit the complete subtree under the object (not including the object).
     * <p>
     * Normally this method is not overridden.
     */
    protected void visitSubTree(Object obj) throws Exception {
      processSubTree(obj);
    }

    private void processNode(Object obj, Collection<Annotation> annotationList) throws Exception {
      if (obj == null && annotationList.size() == 0) {
        return;
      }
      if (obj != null) {
        if (m_markedSet.contains(obj)) {
          return;
        }
        markObject(obj);
      }
      visitObject(obj, annotationList);
    }

    private void processSubTree(Object obj) throws Exception {
      if (obj == null) {
        return;
      }
      Class<?> c = obj.getClass();
      //filter leaf nodes
      if (c == null) {
        return;
      }
      if (c.isPrimitive() || c == Byte.class || c == Short.class || c == Integer.class || c == Long.class || c == Float.class || c == Double.class || c == Character.class || c == Boolean.class || c == String.class) {
        return;
      }
      if (obj instanceof Type) {
        return;
      }
      //visit children
      if (c.isArray()) {
        int len = Array.getLength(obj);
        for (int i = 0; i < len; i++) {
          processNode(Array.get(obj, i), NO_ANNOTATIONS);
        }
        return;
      }
      if (obj instanceof Collection) {
        for (Object elem : (Collection) obj) {
          processNode(elem, NO_ANNOTATIONS);
        }
        return;
      }
      if (obj instanceof Map) {
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
          processNode(entry.getKey(), NO_ANNOTATIONS);
          processNode(entry.getValue(), NO_ANNOTATIONS);
        }
        return;
      }
      ClassMetaData meta = getClassMetaData(c);
      if (meta != NO_CLASS_META_DATA) {
        for (int i = 0; i < meta.fields.length; i++) {
          Field field = meta.fields[i];
          field.setAccessible(true);
          Object childObj = field.get(obj);
          //filter value
          if (childObj instanceof CharSequence) {
            childObj = ((CharSequence) childObj).toString();
          }
          //handle
          Collection<Annotation> ann = meta.fieldAnnotationList[i];
          processNode(childObj, ann != null ? ann : NO_ANNOTATIONS);
        }
      }
    }
  }

  private static class ClassMetaData {
    Field[] fields;
    Collection<Annotation>[] fieldAnnotationList;
  }

  private static class MethodMetaData {
    Collection<Annotation>[] paramAnnotationList;
  }
}
