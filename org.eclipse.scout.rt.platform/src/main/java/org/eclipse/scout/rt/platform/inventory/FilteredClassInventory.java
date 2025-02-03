/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.inventory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class Inventory with filter for a specific type.
 */
public class FilteredClassInventory<T> {
  private static final Logger LOG = LoggerFactory.getLogger(FilteredClassInventory.class);

  private final Predicate<IClassInfo> m_filter;
  private final Class<?> m_inventoryType;

  public FilteredClassInventory(Predicate<IClassInfo> filter, Class<?> clazz) {
    m_filter = filter;
    m_inventoryType = clazz;
  }

  /**
   * @return all classes in the jandex with the correct type accepting the filter.
   */
  public Set<Class<? extends T>> getClasses() {
    Set<IClassInfo> allClasses = findClasses();
    Set<Class<? extends T>> filteredClasses = new HashSet<>(allClasses.size());
    for (IClassInfo ci : allClasses) {
      if (m_filter.test(ci)) {
        try {
          @SuppressWarnings("unchecked")
          Class<? extends T> clazz = (Class<? extends T>) ci.resolveClass();
          filteredClasses.add(clazz);
        }
        catch (Exception e) {
          LOG.error("Error loading class", e);
        }
      }
    }
    return CollectionUtility.hashSet(filteredClasses);
  }

  /**
   * @return classes in jandex {@link ClassInventory} with the correct type.
   */
  protected Set<IClassInfo> findClasses() {
    return ClassInventory.get().getAllKnownSubClasses(getInventoryType());
  }

  protected Class<?> getInventoryType() {
    return m_inventoryType;
  }
}
