/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Helper class to replace objects within a {@link IDataObject}. By intention no methods are public and sub classes must
 * provide public methods. Usually {@link #visit(Object)} is called.
 * <p>
 * {@link DataObjectVisitors} will be in most cases sufficient.
 */
public abstract class AbstractReplacingDataObjectVisitor extends AbstractDataObjectVisitor {

  @Override
  protected void caseCollection(Collection<?> collection) {
    if (collection instanceof List) {
      // order is important
      updateList((List<?>) collection);
    }
    else {
      // order can be ignored
      updateCollection(collection);
    }
  }

  @Override
  protected void caseMap(Map<?, ?> map) {
    updateMap(map);
  }

  @Override
  protected void caseDoEntityNode(DoNode<?> node) {
    updateDoNode(node);
  }

  @Override
  protected void caseDoEntityContributions(Collection<IDoEntityContribution> contributions) {
    updateCollection(contributions);
  }

  @Override
  protected void caseDoList(DoList<?> doList) {
    updateList(doList.get());
  }

  @Override
  protected void caseDoSet(DoSet<?> doSet) {
    updateSet(doSet.get());
  }

  @Override
  protected void caseDoCollection(DoCollection<?> doCollection) {
    updateCollection(doCollection.get());
  }

  protected <LT> void updateList(List<LT> list) {
    ListIterator<LT> it = list.listIterator();
    while (it.hasNext()) {
      LT value = it.next();
      LT newValue = replaceOrVisit(value);
      if (value != newValue) {
        it.remove();
        it.add(newValue);
      }
    }
  }

  protected <CT> void updateCollection(Collection<CT> collection) {
    List<CT> newValues = null;
    Iterator<CT> it = collection.iterator();
    while (it.hasNext()) {
      CT value = it.next();
      CT newValue = replaceOrVisit(value);
      if (value != newValue) {
        it.remove();
        if (newValues == null) {
          newValues = new ArrayList<>();
        }
        newValues.add(newValue);
      }
    }
    if (newValues != null) {
      collection.addAll(newValues);
    }
  }

  protected <SET> void updateSet(Set<SET> set) {
    Set<SET> newValues = null;
    Iterator<SET> it = set.iterator();
    while (it.hasNext()) {
      SET value = it.next();
      SET newValue = replaceOrVisit(value);
      if (value != newValue) {
        it.remove();
        if (newValues == null) {
          newValues = new LinkedHashSet<>();
        }
        newValues.add(newValue);
      }
    }
    if (newValues != null) {
      set.addAll(newValues);
    }
  }

  protected <K, V> void updateMap(Map<K, V> map) {
    Map<K, V> newEntries = null;
    Iterator<Entry<K, V>> it = map.entrySet().iterator();
    while (it.hasNext()) {
      Entry<K, V> entry = it.next();
      K key = entry.getKey();
      K newKey = replaceOrVisit(key);
      V value = entry.getValue();
      V newValue = replaceOrVisit(value);

      if (newKey != key || newValue != value) {
        it.remove();
        if (newEntries == null) {
          newEntries = new HashMap<>();
        }
        newEntries.put(newKey, newValue);
      }
    }
    if (newEntries != null) {
      map.putAll(newEntries);
    }
  }

  protected <NT> void updateDoNode(DoNode<NT> node) {
    node.set(replaceOrVisit(node.get()));
  }

  /**
   * Default implementation only visits and doesn't replace <code>o</code> by a new value.
   * <p>
   * Subclasses must make sure to execute the super call if object isn't replaced because implementation will take care
   * of handling visitor extensions too.
   */
  @SuppressWarnings("unchecked")
  protected <OT> OT replaceOrVisit(OT o) {
    if (o != null) {
      //noinspection unchecked
      IDataObjectVisitorExtension<Object> visitorExtension = m_inventory.getVisitorExtension((Class<Object>) o.getClass());
      if (visitorExtension != null) {
        // Call visitor extension if there is a custom implementation for the given class
        return (OT) visitorExtension.replaceOrVisit(o, this::replaceOrVisit);
      }
    }

    visit(o);
    return o;
  }
}
