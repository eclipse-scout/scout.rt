/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

/**
 * Helper class to visit {@link IDataObject}s. By intention no methods are public and sub classes must provide public
 * methods. Usually {@link #visit(Object)} is called.
 * <p>
 * {@link DataObjectVisitors} will be in most cases sufficient.
 */
public abstract class AbstractDataObjectVisitor {

  protected void visit(Object o) {
    if (o == null) {
      return;
    }
    else if (o instanceof Collection) {
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
    else {
      caseNode(o, this::caseObject);
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
    for (DoNode<?> node : entity.allNodes().values()) {
      visit(node.get());
    }
  }

  protected void caseDoList(DoList<?> doList) {
    for (Object o : doList) {
      visit(o);
    }
  }

  protected void caseObject(Object o) {
    // method hook
  }
}
