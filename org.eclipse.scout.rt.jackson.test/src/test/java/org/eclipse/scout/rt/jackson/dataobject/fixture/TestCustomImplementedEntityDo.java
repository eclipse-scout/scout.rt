/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoNode;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.ValueFormat;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Custom test implementation of {@link IDoEntity}, without extending {@link DoEntity}.
 */
@TypeName("TestCustomImplementedEntity")
public class TestCustomImplementedEntityDo implements IDoEntity {

  private final Map<String, DoNode<?>> m_attributes = new LinkedHashMap<>();
  private List<IDoEntityContribution> m_contributions;

  // attributes
  private static final String DATE_ATTRIBUTE = "dateAttribute";

  @SuppressWarnings("unchecked")
  @ValueFormat(pattern = IValueFormatConstants.DATE_PATTERN)
  public DoValue<Date> dateAttribute() {
    if (!has(DATE_ATTRIBUTE)) {
      put(DATE_ATTRIBUTE, null);
    }
    return (DoValue<Date>) getNode(DATE_ATTRIBUTE);
  }

  // interface methods

  @Override
  public DoNode<?> getNode(String attributeName) {
    return m_attributes.get(attributeName);
  }

  @Override
  public boolean has(String attributeName) {
    return m_attributes.containsKey(attributeName);
  }

  @Override
  public void putNode(String attributeName, DoNode<?> attribute) {
    IDoEntity.super.putNode(attributeName, attribute);
    m_attributes.put(attributeName, attribute);
  }

  @Override
  public void put(String attributeName, Object value) {
    putNode(attributeName, DoValue.of(value));
  }

  @Override
  public <V> void putList(String attributeName, List<V> value) {
    DoList<V> doList = new DoList<>();
    doList.set(value);
    putNode(attributeName, doList);
  }

  @Override
  public <V> void putSet(String attributeName, Set<V> value) {
    DoSet<V> doSet = new DoSet<>();
    doSet.set(value);
    putNode(attributeName, doSet);
  }

  @Override
  public <V> void putCollection(String attributeName, Collection<V> value) {
    DoCollection<V> doCollection = new DoCollection<>();
    doCollection.set(value);
    putNode(attributeName, doCollection);
  }

  @Override
  public boolean remove(String attributeName) {
    return m_attributes.remove(attributeName) != null;
  }

  @Override
  public boolean removeIf(Predicate<? super DoNode<?>> filter) {
    return m_attributes.values().removeIf(filter);
  }

  @Override
  public Map<String, DoNode<?>> allNodes() {
    return Collections.unmodifiableMap(m_attributes);
  }

  @Override
  public Map<String, ?> all() {
    return allNodes().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().get()));
  }

  @Override
  public boolean hasContributions() {
    return !CollectionUtility.isEmpty(m_contributions);
  }

  @Override
  public Collection<IDoEntityContribution> getContributions() {
    if (m_contributions == null) {
      m_contributions = new ArrayList<>();
    }
    return m_contributions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TestCustomImplementedEntityDo that = (TestCustomImplementedEntityDo) o;
    if (m_attributes != null ? !m_attributes.equals(that.m_attributes) : that.m_attributes != null) {
      return false;
    }
    if (!CollectionUtility.equalsCollection(m_contributions, that.m_contributions, false)) { // element order is not relevant
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = m_attributes != null ? m_attributes.hashCode() : 0;
    result = 31 * result + CollectionUtility.hashCodeCollection(m_contributions); // element order is not relevant
    return result;
  }

  @Override
  public String toString() {
    return TestCustomImplementedEntityDo.class.getSimpleName() + " [m_attributes=" + m_attributes + "]";
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @SuppressWarnings("unchecked")
  @Generated("DoConvenienceMethodsGenerator")
  public TestCustomImplementedEntityDo withDateAttribute(Date dateAttribute) {
    dateAttribute().set(dateAttribute);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Generated("DoConvenienceMethodsGenerator")
  public Date getDateAttribute() {
    return dateAttribute().get();
  }
}
