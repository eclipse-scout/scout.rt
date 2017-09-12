/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.form;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CloneUtility;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.extension.AbstractContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;

@Bean
public abstract class AbstractFormData extends AbstractContributionComposite implements IPropertyHolder {
  public static final char FIELD_PATH_DELIM = '/';
  private static final long serialVersionUID = 1L;

  private Map<Class<?>, Class<? extends AbstractFormFieldData>> m_fieldDataReplacements;
  private Map<Class<? extends AbstractPropertyData>, AbstractPropertyData> m_propertyMap;
  private Map<Class<? extends AbstractFormFieldData>, AbstractFormFieldData> m_fieldMap;

  private List<Class<AbstractPropertyData>> getConfiguredPropertyDatas() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, AbstractPropertyData.class);
  }

  private List<Class<? extends AbstractFormFieldData>> getConfiguredFieldDatas() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<AbstractFormFieldData>> fca = ConfigurationUtility.filterClasses(dca, AbstractFormFieldData.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  @Override
  protected void initConfig() {
    // add properties
    List<Class<AbstractPropertyData>> configuredPropertyDatas = getConfiguredPropertyDatas();
    Map<Class<? extends AbstractPropertyData>, AbstractPropertyData> propMap = new HashMap<>(configuredPropertyDatas.size());
    for (Class<AbstractPropertyData> propertyDataClazz : configuredPropertyDatas) {
      AbstractPropertyData p = ConfigurationUtility.newInnerInstance(this, propertyDataClazz);
      propMap.put(p.getClass(), p);
    }
    if (!propMap.isEmpty()) {
      m_propertyMap = propMap;
    }

    // add fields
    List<Class<? extends AbstractFormFieldData>> formFieldDataClazzes = getConfiguredFieldDatas();
    m_fieldMap = new HashMap<>(formFieldDataClazzes.size());
    Map<Class<?>, Class<? extends AbstractFormFieldData>> replacements = ConfigurationUtility.getReplacementMapping(formFieldDataClazzes);
    if (!replacements.isEmpty()) {
      m_fieldDataReplacements = replacements;
    }
    for (Class<? extends AbstractFormFieldData> formFieldDataClazz : formFieldDataClazzes) {
      AbstractFormFieldData f = ConfigurationUtility.newInnerInstance(this, formFieldDataClazz);
      m_fieldMap.put(f.getClass(), f);
    }
  }

  @Override
  public AbstractPropertyData getPropertyById(String id) {
    if (m_propertyMap == null) {
      return null;
    }
    for (AbstractPropertyData p : m_propertyMap.values()) {
      if (p.getPropertyId().equalsIgnoreCase(id)) {
        return p;
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends AbstractPropertyData> T getPropertyByClass(Class<T> c) {
    if (m_propertyMap == null) {
      return null;
    }
    return (T) m_propertyMap.get(c);
  }

  @Override
  public <T extends AbstractPropertyData> void setPropertyByClass(Class<T> c, T v) {
    if (v == null) {
      if (m_propertyMap != null) {
        m_propertyMap.remove(c);
      }
    }
    else {
      if (m_propertyMap == null) {
        m_propertyMap = new HashMap<>();
      }
      m_propertyMap.put(c, v);
    }
  }

  @Override
  public AbstractPropertyData[] getAllProperties() {
    return m_propertyMap != null ? m_propertyMap.values().toArray(new AbstractPropertyData[m_propertyMap.size()]) : new AbstractPropertyData[0];
  }

  public AbstractFormFieldData getFieldById(String id) {
    String fieldDataId = FormDataUtility.getFieldDataId(id);
    for (AbstractFormFieldData f : m_fieldMap.values()) {
      if (f.getFieldId().equals(fieldDataId)) {
        return f;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractFormFieldData> T getFieldByClass(Class<T> c) {
    Class<? extends T> clazz = getReplacingFieldDataClass(c);
    return (T) m_fieldMap.get(clazz);
  }

  public <T extends AbstractFormFieldData> void setFieldByClass(Class<T> c, T v) {
    Class<? extends T> clazz = getReplacingFieldDataClass(c);
    if (v == null) {
      m_fieldMap.remove(clazz);
    }
    else {
      m_fieldMap.put(clazz, v);
    }
  }

  /**
   * Checks whether the form field data with the given class has been replaced by another field. If so, the replacing
   * form field data's class is returned. Otherwise the given class itself.
   *
   * @param c
   * @return Returns the possibly available replacing field data class for the given class.
   * @see Replace
   * @since 3.8.2
   */
  private <T extends AbstractFormFieldData> Class<? extends T> getReplacingFieldDataClass(Class<T> c) {
    if (m_fieldDataReplacements != null) {
      @SuppressWarnings("unchecked")
      Class<T> replacingFieldDataClass = (Class<T>) m_fieldDataReplacements.get(c);
      if (replacingFieldDataClass != null) {
        return replacingFieldDataClass;
      }
    }
    return c;
  }

  /**
   * @return all fields of the form data itself, not including fields in external field templates
   */
  public AbstractFormFieldData[] getFields() {
    return m_fieldMap.values().toArray(new AbstractFormFieldData[m_fieldMap.size()]);
  }

  /**
   * @return all fields of the form data and all its external template field datas in a map with qualified ids<br>
   *         The array of returned fields is the result of a top-down breadth-first tree traversal
   *         <p>
   *         Example:
   *
   *         <pre>
   * A
   *   U
   *     E
   *     F
   *   V
   * B
   *   X
   *   Y
   *         </pre>
   *
   *         would be returned as A B U V X Y E F
   */
  public Map<Integer, Map<String/* qualified field id */, AbstractFormFieldData>> getAllFieldsRec() {
    Map<Integer, Map<String, AbstractFormFieldData>> breadthFirstMap = new TreeMap<>();
    for (AbstractFormFieldData child : getFields()) {
      collectAllFieldsRec(child, breadthFirstMap, 0, "", false);
    }

    collectFieldDatasInContributions(this, breadthFirstMap, "");

    return breadthFirstMap;
  }

  private static void collectFieldDatasInContributions(IContributionOwner comp, Map<Integer, Map<String, AbstractFormFieldData>> breadthFirstMap, String prefix) {
    collectInContributedFormDatas(comp.getContributionsByClass(AbstractFormData.class), breadthFirstMap, prefix);
    collectInContributedFormFieldDatas(comp.getContributionsByClass(AbstractFormFieldData.class), breadthFirstMap, prefix);
  }

  private static void collectInContributedFormFieldDatas(Collection<AbstractFormFieldData> contributions, Map<Integer, Map<String, AbstractFormFieldData>> breadthFirstMap, String prefix) {
    for (AbstractFormFieldData ffd : contributions) {
      collectAllFieldsRec(ffd, breadthFirstMap, 0, prefix, !(ffd instanceof IHolder));
    }
  }

  private static void collectInContributedFormDatas(Collection<AbstractFormData> contributions, Map<Integer, Map<String, AbstractFormFieldData>> breadthFirstMap, String prefix) {
    for (AbstractFormData fd : contributions) {
      for (AbstractFormFieldData child : fd.getFields()) {
        collectAllFieldsRec(child, breadthFirstMap, 0, prefix, false);
      }
    }
  }

  private static void collectAllFieldsRec(AbstractFormFieldData field, Map<Integer/* level */, Map<String/* qualified field id */, AbstractFormFieldData>> breadthFirstMap, int level, String prefix, boolean isContributionTopLevelContainer) {
    Map<String/* qualified field id */, AbstractFormFieldData> subMap = breadthFirstMap.computeIfAbsent(level, k -> new HashMap<>());
    boolean isTopLevel = field.getClass().getDeclaringClass() == null;
    String fieldId = null;
    if (isTopLevel || isContributionTopLevelContainer) {
      fieldId = FormDataUtility.getFieldDataId(field);
    }
    else {
      fieldId = field.getFieldId();
    }

    if (!isContributionTopLevelContainer) {
      subMap.put(prefix + fieldId, field);
    }

    String fieldPrefix = prefix;
    if (!isContributionTopLevelContainer) {
      fieldPrefix = prefix + fieldId + FIELD_PATH_DELIM;
    }
    collectFieldDatasInContributions(field, breadthFirstMap, fieldPrefix);
    for (AbstractFormFieldData child : field.getFields()) {
      collectAllFieldsRec(child, breadthFirstMap, level + 1, fieldPrefix, false);
    }
  }

  /**
   * Searches the given form field data in this form data as well as in all externally referenced template field data.
   *
   * @param breadthFirstMap
   *          The breadth-first search map as returned by {@link AbstractFormData#getAllFieldsRec()}. If
   *          <code>null</code>, a new map is created.
   * @param valueTypeIdentifier
   *          The class identifier to be searched in the form data.
   * @return Returns the form data's {@link AbstractFormFieldData} of the given valueType or <code>null</code>, if it
   *         does not exist.
   */
  public AbstractFormFieldData findFieldByClass(Map<Integer, Map<String, AbstractFormFieldData>> breadthFirstMap, ClassIdentifier valueTypeIdentifier) {
    if (breadthFirstMap == null) {
      breadthFirstMap = getAllFieldsRec();
    }
    AbstractFormFieldData candidate = null;
    for (Map<String, AbstractFormFieldData> subMap : breadthFirstMap.values()) {
      for (Entry<String, AbstractFormFieldData> entry : subMap.entrySet()) {
        AbstractFormFieldData fd = entry.getValue();
        String fieldId = entry.getKey();
        if (matchesAllParts(valueTypeIdentifier, fieldId, fd)) {
          if (candidate != null) {
            throw new ProcessingException("Found more than one field for class: [" + fd.getClass() + "]");
          }
          candidate = fd;
        }
      }
    }
    return candidate;
  }

  /**
   * @return all properties of the form data and all its external template field data in a map with qualified ids<br>
   *         The array of returned fields is the result of a top-down breadth-first tree traversal
   *         <p>
   *         Example:
   *
   *         <pre>
   * A (p1, p4)
   *   U
   *     E (p3)
   *     F
   *   V
   * B
   *   X (p2)
   *   Y
   *         </pre>
   *
   *         would be returned as p1, p4, p2, p3
   */
  public Map<Integer, Map<String/* qualified property id */, AbstractPropertyData<?>>> getAllPropertiesRec() {
    Map<Integer, Map<String, AbstractPropertyData<?>>> breadthFirstMap = new TreeMap<>();
    Map<String, AbstractPropertyData<?>> rootMap = new HashMap<>();
    breadthFirstMap.put(0, rootMap);
    for (AbstractPropertyData<?> prop : getAllProperties()) {
      rootMap.put(prop.getClass().getSimpleName(), prop);
    }
    for (AbstractFormFieldData child : getFields()) {
      collectAllPropertiesRec(child, breadthFirstMap, 1, child.getFieldId() + FIELD_PATH_DELIM);
    }
    return breadthFirstMap;
  }

  private void collectAllPropertiesRec(AbstractFormFieldData field, Map<Integer/* level */, Map<String/* qualified field id */, AbstractPropertyData<?>>> breadthFirstMap, int level, String prefix) {
    Map<String/* qualified field id */, AbstractPropertyData<?>> subMap = breadthFirstMap.computeIfAbsent(level, k -> new HashMap<>());
    for (AbstractPropertyData<?> prop : field.getAllProperties()) {
      subMap.put(prefix + prop.getClass().getSimpleName(), prop);
    }
    for (AbstractFormFieldData child : field.getFields()) {
      collectAllPropertiesRec(child, breadthFirstMap, level + 1, prefix + child.getFieldId() + FIELD_PATH_DELIM);
    }
  }

  /**
   * Searches the given property data in this form data as well as in all externally referenced template field data.
   *
   * @param breadthFirstMap
   *          The breadth-first search map as returned by {@link AbstractFormData#getAllPropertiesRec()}. If
   *          <code>null</code>, a new map is created.
   * @param valueType
   *          The type to be searched in the form data.
   * @return Returns the form data's {@link AbstractPropertyData} of the given valueType or <code>null</code>, if it
   *         does not exist.
   */
  public AbstractPropertyData<?> findPropertyByClass(Map<Integer, Map<String, AbstractPropertyData<?>>> breadthFirstMap, ClassIdentifier valueTypeClassIdentifier) {
    if (breadthFirstMap == null) {
      breadthFirstMap = getAllPropertiesRec();
    }
    AbstractPropertyData<?> candidate = null;
    for (Map<String, AbstractPropertyData<?>> subMap : breadthFirstMap.values()) {
      for (Entry<String, AbstractPropertyData<?>> entry : subMap.entrySet()) {
        String propertyId = entry.getKey();
        AbstractPropertyData<?> pd = entry.getValue();
        if (matchesAllParts(valueTypeClassIdentifier, propertyId, pd)) {
          if (candidate != null) {
            throw new ProcessingException("Found more than one property for class: [" + pd.getClass() + "]");
          }
          candidate = pd;
        }
      }
    }
    return candidate;
  }

  public AbstractFormData deepCopy() {
    try {
      return CloneUtility.createDeepCopyBySerializing(this);
    }
    catch (Exception e) {
      throw new ProcessingException("Could not create deep copy", e);
    }
  }

  /**
   * Checks whether the given fully qualified fieldId matches all parts of the given class identifier. The last segment
   * is checked against the given objects's type.
   *
   * @param valueTypeIdentifier
   * @param fullyQualifiedFieldId
   *          The fully qualified fieldId.
   * @param obj
   *          The object representing the last segment.
   * @return Returns <code>true</code> if all segments of the given class identifier are part of the fully qualified
   *         field id. <code>false</code> otherwise.
   */
  private boolean matchesAllParts(ClassIdentifier valueTypeIdentifier, String fullyQualifiedFieldId, Object obj) {
    // check last segment by class
    if (obj == null || obj.getClass() != valueTypeIdentifier.getLastSegment()) {
      return false;
    }
    // check other segments by id
    Class<?>[] classes = valueTypeIdentifier.getClasses();
    String[] fieldIdParts = fullyQualifiedFieldId.split("[/]");
    int i = classes.length - 2;
    int j = fieldIdParts.length - 2;
    while (i >= 0 && j >= 0) {
      String fieldId = classes[i].getName();
      int i1 = Math.max(fieldId.lastIndexOf('$'), fieldId.lastIndexOf('.'));
      fieldId = fieldId.substring(i1 + 1);
      if (fieldIdParts[j].equals(fieldId)) {
        i--;
      }
      j--;
    }
    return i < 0;
  }

  @Override
  public String toString() {
    return FormDataUtility.toString(this, false);
  }
}
