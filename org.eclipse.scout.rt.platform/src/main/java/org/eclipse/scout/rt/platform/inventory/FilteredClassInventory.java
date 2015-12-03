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
package org.eclipse.scout.rt.platform.inventory;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class Inventory with filter for a specific type.
 */
public class FilteredClassInventory<T> {
  private static final Logger LOG = LoggerFactory.getLogger(FilteredClassInventory.class);

  private final IFilter<IClassInfo> m_filter;
  private final Class<?> m_inventoryType;

  public FilteredClassInventory(IFilter<IClassInfo> filter, Class<?> clazz) {
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
      if (m_filter.accept(ci)) {
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
