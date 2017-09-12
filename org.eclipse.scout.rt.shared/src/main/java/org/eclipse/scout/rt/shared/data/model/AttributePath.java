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
package org.eclipse.scout.rt.shared.data.model;

import java.util.List;

/**
 * This path represents the forward path from the data model root to an attribute
 * <p>
 * Since the data model is a graph (with potential cycles) the physical instantiation of the datamodel is performed by
 * creating all graph nodes once with single parent linkage. Therefore when holding a {@link IDataModelAttribute} or
 * {@link IDataModelEntity} the forward path to this object from the {@link IDataModel} root is ambiguous. This class
 * solves that problem by containing the complete path.
 * 
 * @since 3.8
 */
public final class AttributePath implements IDataModelPath {
  private final EntityPath m_entityPath;
  private final IDataModelAttribute m_attribute;

  public AttributePath(List<IDataModelEntity> entityPath, IDataModelAttribute attribute) {
    this(entityPath != null && !entityPath.isEmpty() ? new EntityPath(entityPath) : null, attribute);
  }

  public AttributePath(EntityPath entityPath, IDataModelAttribute attribute) {
    if (entityPath == null || entityPath.isEmpty()) {
      m_entityPath = EntityPath.EMPTY;
    }
    else {
      m_entityPath = entityPath;
    }
    m_attribute = attribute;
  }

  /**
   * @return the entity path of the attribute or {@link EntityPath#EMPTY}
   *         <p>
   *         Never returns null.
   */
  public EntityPath getEntityPath() {
    return m_entityPath;
  }

  public IDataModelAttribute getAttribute() {
    return m_attribute;
  }

  @Override
  public int hashCode() {
    return m_entityPath.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof AttributePath)) {
      return false;
    }
    AttributePath other = (AttributePath) obj;
    return this.m_entityPath.equals(other.m_entityPath) && this.m_attribute == other.m_attribute;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + m_entityPath.toString() + "#" + m_attribute.getClass().getSimpleName() + "(" + m_attribute.getText() + ")]";
  }
}
