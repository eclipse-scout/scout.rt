/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

import org.eclipse.scout.rt.platform.classid.ClassIdentifier;

public class ExtensionRegistryMoveItem extends AbstractExtensionRegistryItem {

  private final ClassIdentifier m_modelClassIdentifier;
  private final ClassIdentifier m_newModelContainerClassIdentifier;

  public ExtensionRegistryMoveItem(ClassIdentifier modelClass, ClassIdentifier newModelContainerClassIdentifier, Double newModelOrder, long order) {
    super(order, newModelOrder);
    m_modelClassIdentifier = modelClass;
    m_newModelContainerClassIdentifier = newModelContainerClassIdentifier;
  }

  public ClassIdentifier getNewModelContainerClassIdentifier() {
    return m_newModelContainerClassIdentifier;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_modelClassIdentifier == null) ? 0 : m_modelClassIdentifier.hashCode());
    result = prime * result + ((m_newModelContainerClassIdentifier == null) ? 0 : m_newModelContainerClassIdentifier.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof ExtensionRegistryMoveItem)) {
      return false;
    }
    ExtensionRegistryMoveItem other = (ExtensionRegistryMoveItem) obj;
    if (m_modelClassIdentifier == null) {
      if (other.m_modelClassIdentifier != null) {
        return false;
      }
    }
    else if (!m_modelClassIdentifier.equals(other.m_modelClassIdentifier)) {
      return false;
    }
    if (m_newModelContainerClassIdentifier == null) {
      if (other.m_newModelContainerClassIdentifier != null) {
        return false;
      }
    }
    else if (!m_newModelContainerClassIdentifier.equals(other.m_newModelContainerClassIdentifier)) {
      return false;
    }
    return true;
  }
}
