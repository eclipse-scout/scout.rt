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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.BEANS;

/**
 * Helper class to visit {@link IDataObject}s. By intention no methods are public and sub classes must provide public
 * methods. Usually {@link #visit(Object)} is called.
 * <p>
 * {@link DataObjectVisitors} will be in most cases sufficient.
 */
public abstract class AbstractDataObjectVisitor {

  protected final DataObjectInventory m_inventory = BEANS.get(DataObjectInventory.class);

  protected void visit(Object o) {
    if (o == null) {
      return;
    }

    if (o instanceof Collection) {
      caseNode((Collection<?>) o, this::caseCollection);
    }
    else if (o instanceof Map) {
      caseNode((Map<?, ?>) o, this::caseMap);
    }
    else if (o instanceof IDoEntity) {
      caseNode((IDoEntity) o, this::caseDoEntity);
    }
    else if (o instanceof DoList) {
      caseNode((DoList<?>) o, this::caseDoList);
    }
    else if (o instanceof DoSet) {
      caseNode((DoSet<?>) o, this::caseDoSet);
    }
    else if (o instanceof DoCollection) {
      caseNode((DoCollection<?>) o, this::caseDoCollection);
    }
    else {
      caseNode(o, this::caseObjectWithExtension);
    }
  }

  protected <T> void caseNode(T node, Consumer<T> chain) {
    chain.accept(node);
  }

  protected void caseCollection(Collection<?> collection) {
    for (Object o : collection) {
      visit(o);
    }
  }

  protected void caseMap(Map<?, ?> map) {
    for (Entry<?, ?> entry : map.entrySet()) {
      visit(entry.getKey());
      visit(entry.getValue());
    }
  }

  protected void caseDoEntity(IDoEntity entity) {
    applyVisitorExtension(entity);
    caseDoEntityNodes(entity.allNodes().values());
    caseDoEntityContributions(entity.getContributions());
  }

  protected void caseDoEntityNodes(Collection<DoNode<?>> nodes) {
    for (DoNode<?> node : nodes) {
      caseDoEntityNode(node);
    }
  }

  protected void caseDoEntityNode(DoNode<?> node) {
    if (node instanceof DoList) {
      caseDoList((DoList<?>) node);
    }
    else if (node instanceof DoSet) {
      caseDoSet((DoSet<?>) node);
    }
    else if (node instanceof DoCollection) {
      caseDoCollection((DoCollection<?>) node);
    }
    else {
      // DoValue
      visit(node.get());
    }
  }

  protected void caseDoEntityContributions(Collection<IDoEntityContribution> contributions) {
    for (IDoEntityContribution contribution : contributions) {
      visit(contribution);
    }
  }

  protected void caseDoList(DoList<?> doList) {
    for (Object o : doList) {
      visit(o);
    }
  }

  protected void caseDoSet(DoSet<?> doSet) {
    for (Object o : doSet) {
      visit(o);
    }
  }

  protected void caseDoCollection(DoCollection<?> doCollection) {
    for (Object o : doCollection) {
      visit(o);
    }
  }

  protected void caseObjectWithExtension(Object o) {
    applyVisitorExtension(o);
    caseObject(o);
  }

  protected void applyVisitorExtension(Object o) {
    if (o == null) {
      return; // should never be null, but just in case, e.g. if called from subclasses with a null value
    }

    //noinspection unchecked
    IDataObjectVisitorExtension<Object> visitorExtension = m_inventory.getVisitorExtension((Class<Object>) o.getClass());
    if (visitorExtension != null) {
      // Call visitor extension if there is a custom implementation for the given class
      visitorExtension.visit(o, this::visit);
    }
  }

  protected void caseObject(Object o) {
    // method hook
  }
}
