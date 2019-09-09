/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6.model.api;

import java.util.HashMap;
import java.util.Map;

public class Libraries extends NamedElement {

  Map<String /*fqn*/, INamedElement> m_allElements = new HashMap<>();

  public Libraries() {
    super(Type.AllLibraries, null, (INamedElement) null);
  }

  public void ensureParents() {
    setParents(this, null);
  }

  private void setParents(INamedElement element, INamedElement parent) {
    element.setParent(parent);
    element.getChildren().forEach(child -> setParents(child, element));
    m_allElements.put(element.getFullyQualifiedName(), element);
  }

  public INamedElement getElement(String fqn) {
    return m_allElements.get(fqn);
  }
}
