/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.json.JSONArray;
import org.json.JSONObject;

@Bean
public class DefaultValuesFilter {

  public static final String PROP_DEFAULTS = "defaults";
  public static final String PROP_OBJECT_TYPE_HIERARCHY = "objectTypeHierarchy";

  /**
   * Map holding the defaults object for a given object type from the configuration file.
   * <p>
   * <code>ObjectType -> { PropertyName -> DefaultValue, ... }</code>
   */
  private final Map<String, Map<String, Object>> m_defaults = new HashMap<>();

  /**
   * Map holding the full object type hierarchy for all known object types. The object type hierarchy is a list of
   * object types from the type itself (equal to the map entry's key) to the most far parent type.
   * <p>
   * <code>ObjectType -> [ ObjectType, ParentOfObjectType, ..., TopLevelObjectType ]</code>
   */
  private final Map<String, List<String>> m_objectTypeHierarchyFlat = new HashMap<>();

  /**
   * @param defaultValuesConfiguration
   *          a {@link JSONObject} with two properties: {@value #PROP_DEFAULTS} and {@value #PROP_OBJECT_TYPE_HIERARCHY}
   */
  public void importConfiguration(JSONObject defaultValuesConfiguration) {
    if (defaultValuesConfiguration == null) {
      return;
    }
    importConfiguration(defaultValuesConfiguration.optJSONObject(PROP_DEFAULTS), defaultValuesConfiguration.optJSONObject(PROP_OBJECT_TYPE_HIERARCHY));
  }

  public void importConfiguration(JSONObject jsonDefaults, JSONObject jsonObjectTypeHierarchy) {
    importDefaults(jsonDefaults);
    importObjectTypeHierarchy(jsonObjectTypeHierarchy);
  }

  protected void importDefaults(JSONObject jsonDefaults) {
    if (jsonDefaults == null) {
      return;
    }
    for (Iterator it = jsonDefaults.keys(); it.hasNext();) {
      String type = (String) it.next();
      JSONObject jsonProperties = jsonDefaults.optJSONObject(type);
      for (Iterator it2 = jsonProperties.keys(); it2.hasNext();) {
        String prop = (String) it2.next();
        Object value = jsonProperties.opt(prop);
        // Add to map
        Map<String, Object> propMap = m_defaults.computeIfAbsent(type, k -> new HashMap<>());
        Object oldValue = propMap.get(prop);
        if (value instanceof JSONObject && oldValue instanceof JSONObject) {
          // Combine
          JsonObjectUtility.mergeProperties((JSONObject) oldValue, (JSONObject) value);
        }
        else {
          // Override (cannot be combined)
          propMap.put(prop, value);
        }
      }
    }
  }

  protected void importObjectTypeHierarchy(JSONObject jsonObjectTypeHierarchy) {
    // Generate hierarchy from configuration file
    generateObjectTypeHierarchyRec(jsonObjectTypeHierarchy, null, m_objectTypeHierarchyFlat);

    // For all object types in the defaults that don't have a hierarchy yet, add a dummy hierarchy with one element
    for (String objectType : m_defaults.keySet()) {
      if (!m_objectTypeHierarchyFlat.containsKey(objectType)) {
        m_objectTypeHierarchyFlat.put(objectType, Collections.singletonList(objectType));
      }
    }
  }

  protected void generateObjectTypeHierarchyRec(JSONObject json, List<String> currentParentObjectTypes, Map<String, List<String>> targetMap) {
    if (json == null) {
      return;
    }
    if (targetMap == null) {
      throw new IllegalArgumentException("Argument 'targetMap' must not be null");
    }
    for (Iterator it = json.keys(); it.hasNext();) {
      String objectType = (String) it.next();
      Object subHierarchy = json.opt(objectType);

      // Create a copy of the current object type list and add the current type to the front
      List<String> newParentObjectTypes = new ArrayList<>();
      newParentObjectTypes.add(objectType);
      if (currentParentObjectTypes != null) {
        newParentObjectTypes.addAll(currentParentObjectTypes);
      }

      if (subHierarchy instanceof JSONObject && ((JSONObject) subHierarchy).length() > 0) {
        generateObjectTypeHierarchyRec((JSONObject) subHierarchy, newParentObjectTypes, targetMap);
      }

      // Store current result
      List<String> existingParentObjectTypes = targetMap.get(objectType);
      if (existingParentObjectTypes != null) {
        throw new IllegalStateException("Object type '" + objectType + "' has ambiguous parent object types: [" + CollectionUtility.format(existingParentObjectTypes) + "] vs. [" + CollectionUtility.format(newParentObjectTypes) + "]");
      }
      targetMap.put(objectType, newParentObjectTypes);
    }
  }

  protected final Map<String, Map<String, Object>> getDefaults() {
    return m_defaults;
  }

  protected final Map<String, List<String>> getObjectTypeHierarchyFlat() {
    return m_objectTypeHierarchyFlat;
  }

  public void filter(JSONObject json) {
    if (json == null) {
      return;
    }
    filter(json, json.optString("objectType", null));
  }

  public void filter(JSONObject json, String objectType) {
    if (json == null || objectType == null) {
      return;
    }
    List<String> objectTypeHierarchy = m_objectTypeHierarchyFlat.get(objectType);
    if (objectTypeHierarchy == null) {
      // Remove model variant and try again
      objectType = objectType.replaceAll(Pattern.quote(ModelVariant.SEPARATOR) + ".*", "");
      objectTypeHierarchy = m_objectTypeHierarchyFlat.get(objectType);
    }
    if (objectTypeHierarchy == null) {
      // Unknown type, no default values
      return;
    }
    FilterState filterState = new FilterState();
    for (String t : objectTypeHierarchy) {
      for (Iterator it = json.keys(); it.hasNext();) {
        String prop = (String) it.next();
        Object value = json.opt(prop);
        filterState.pushProperty(prop);
        if (!filterState.isCurrentPropertyProcessed() && checkPropertyValueEqualToDefaultValue(t, prop, value, filterState)) {
          // Property value value is equal to the static default value -> remove the property
          it.remove();
        }
        filterState.popProperty(prop);
      }
    }
  }

  protected boolean checkPropertyValueEqualToDefaultValue(String objectType, String propertyName, Object propertyValue, FilterState filterState) {
    // Try to find a default value until one is found or there are no more parent types to check
    Map<String, Object> properties = m_defaults.get(objectType);
    if (properties == null) {
      return false;
    }
    if (properties.containsKey(propertyName)) {
      filterState.markCurrentPropertyAsProcessed();
      Object defaultValue = properties.get(propertyName);
      if (checkValueEqualToDefaultValue(propertyValue, defaultValue, filterState)) {
        return true;
      }
    }
    // Special case: Check if there is a "pseudo" default value, which will not
    // be removed itself, but might have sub-properties removed.
    else if (properties.containsKey("~" + propertyName)) {
      Object defaultValue = properties.get("~" + propertyName);
      checkValueEqualToDefaultValue(propertyValue, defaultValue, filterState);
    }
    return false;
  }

  protected boolean checkValueEqualToDefaultValue(Object value, Object defaultValue, FilterState filterState) {
    // Now compare the given value to the found default value
    if (value == null && defaultValue == null) {
      return true;
    }
    if (value == null || defaultValue == null) {
      return false;
    }
    if (defaultValue instanceof JSONObject) {
      if (value instanceof JSONObject) {
        JSONObject jsonValue = (JSONObject) value;
        JSONObject jsonDefaultValue = (JSONObject) defaultValue;
        // Special case: The property cannot be removed, but maybe  we can remove some of the objects attributes
        return filterDefaultObject(jsonValue, jsonDefaultValue, filterState);
      }
      if (value instanceof JSONArray) {
        JSONArray jsonValue = (JSONArray) value;
        JSONObject jsonDefaultValue = (JSONObject) defaultValue;
        // Special case: Apply default value object to each element in the array
        filterDefaultObject(jsonValue, jsonDefaultValue, filterState);
      }
      return false;
    }
    // Convert JSONArrays to collections to be able to compare them
    if (value instanceof JSONArray) {
      value = jsonArrayToCollection((JSONArray) value, true);
    }
    if (defaultValue instanceof JSONArray) {
      defaultValue = jsonArrayToCollection((JSONArray) defaultValue, false);
    }
    if (value instanceof List<?> && defaultValue instanceof List<?>) {
      return CollectionUtility.equalsCollection((List<?>) value, (List<?>) defaultValue); // same order
    }
    if (value instanceof Collection<?> && defaultValue instanceof Collection<?>) {
      return CollectionUtility.equalsCollection((Collection<?>) value, (Collection<?>) defaultValue); // any order
    }
    try {
      // Try to match types (to make Integer "1" equal to Double "1.0")
      value = TypeCastUtility.castValue(value, defaultValue.getClass());
    }
    catch (RuntimeException e) { // NOSONAR
      // Types do not match
      return false;
    }
    return ObjectUtility.equals(value, defaultValue);
  }

  /**
   * Removes all properties from "valueObject" where the value matches the corresponding value in "defaultValueObject".
   *
   * @return <code>true</code> of the two objects are completely equal and no properties remain in "valueObject". This
   *         means that the valueObject itself MAY be removed. Return value <code>false</code> means that not all
   *         properties are equal (but nevertheless, some properties may have been removed from valueObject).
   */
  protected boolean filterDefaultObject(JSONObject valueObject, JSONObject defaultValueObject, FilterState filterState) {
    boolean sameKeys = CollectionUtility.equalsCollection(valueObject.keySet(), defaultValueObject.keySet());
    for (Iterator it = valueObject.keys(); it.hasNext();) {
      String prop = (String) it.next();
      filterState.pushProperty(prop);
      if (!filterState.isCurrentPropertyProcessed()) {
        Object subValue = valueObject.opt(prop);
        if (defaultValueObject.has(prop)) {
          filterState.markCurrentPropertyAsProcessed();
          Object subDefaultValue = defaultValueObject.opt(prop);
          if (checkValueEqualToDefaultValue(subValue, subDefaultValue, filterState)) {
            // Property value value is equal to the static default value -> remove the property
            it.remove();
          }
        }
        else if (defaultValueObject.has("~" + prop)) {
          // Special case: Check if there is a "pseudo" default value, which will not
          // be removed itself, but might have sub-properties removed.
          Object subDefaultValue = defaultValueObject.opt("~" + prop);
          checkValueEqualToDefaultValue(subValue, subDefaultValue, filterState);
        }
      }
      filterState.popProperty(prop);
    }
    // Even more special case: If valueObject is now empty and it used to have the same keys as
    // the defaultValueObject, it is considered equal to the default value and MAY be removed.
    if (valueObject.length() == 0 && sameKeys) {
      return true;
    }
    return false;
  }

  /**
   * Filters the given default values from all {@link JSONObject}s in the "valueArray". If the array contains more
   * arrays, the method is called recursively on those arrays. Otherwise, nothing is filtered. If an element is
   * completely equal to the defaultValueObject it is <b>not</b> removed, i.e. an empty object remains at this position
   * in the array. Otherwise, we could not restore the object later.
   */
  protected void filterDefaultObject(JSONArray valueArray, JSONObject defaultValueObject, FilterState filterState) {
    for (int i = 0; i < valueArray.length(); i++) {
      Object value = valueArray.opt(i);
      // Can only filter
      if (value instanceof JSONObject) {
        JSONObject jsonValue = (JSONObject) value;
        // Filter, but ignore return value. Element in the array must never be removed,
        // otherwise we could not restore it later.
        filterDefaultObject(jsonValue, defaultValueObject, filterState);
      }
      else if (value instanceof JSONArray) {
        JSONArray jsonArray = (JSONArray) value;
        filterDefaultObject(jsonArray, defaultValueObject, filterState);
      }
    }
  }

  protected Collection<Object> jsonArrayToCollection(JSONArray array, boolean preserveOrder) {
    if (array == null) {
      return null;
    }
    Collection<Object> result = (preserveOrder ? new ArrayList<>() : new HashSet<>());
    for (int i = 0; i < array.length(); i++) {
      Object element = array.opt(i);
      if (element instanceof JSONArray) {
        result.add(jsonArrayToCollection((JSONArray) element, preserveOrder));
      }
      else {
        result.add(element);
      }
    }
    return result;
  }

  /**
   * Helper class for an object that is passed through all methods during a call to
   * {@link DefaultValuesFilter#filter(JSONObject)}.
   */
  protected class FilterState {

    /**
     * Stack of property names that are currently processed (may be nested when default values contain "pseudo" property
     * with "~" prefix)
     */
    private final Deque<String> m_propertyStack = new ArrayDeque<>();
    /**
     * Set of all property names that were already processed and don't have to be processed again going up in the object
     * hierarchy.
     * <p>
     * Example:
     * <ul>
     * <li>Both "FormField" and "Button" define a default value for the property "statusVisible".
     * <li>When filtering the JSON of a button, the property "statusVisible" can only be removed when its value is equal
     * to the value defined for "Button", not for "FormField".
     * <li>To achieve that, the property is marked as "processed" after comparing its value to the default value defined
     * for "Button" (no matter if the default value matches or not). This prevents any checks or accidental removals
     * when checking against the default values of the parent hierarchy.
     */
    private final Set<String> m_processedProperties = new HashSet<>();

    protected Deque<String> getPropertyStack() {
      return m_propertyStack;
    }

    protected Set<String> getCheckedProperties() {
      return m_processedProperties;
    }

    public void pushProperty(String propertyName) {
      m_propertyStack.push(propertyName);
    }

    public void popProperty(String propertyName) {
      if (ObjectUtility.notEquals(m_propertyStack.peek(), propertyName)) {
        throw new IllegalStateException("'" + propertyName + "' is not the last element in the stack: [" + CollectionUtility.format(m_propertyStack) + "]");
      }
      m_propertyStack.pop();
    }

    /**
     * @return a combined string from all property names on the stack (separated by ".")
     */
    public String getCurrentProperty() {
      StringBuilder sb = new StringBuilder();
      for (Iterator<String> it = m_propertyStack.descendingIterator(); it.hasNext();) {
        if (sb.length() > 0) {
          sb.append(".");
        }
        sb.append(it.next());
      }
      return sb.toString();
    }

    public boolean isCurrentPropertyProcessed() {
      return m_processedProperties.contains(getCurrentProperty());
    }

    public void markCurrentPropertyAsProcessed() {
      m_processedProperties.add(getCurrentProperty());
    }
  }
}
