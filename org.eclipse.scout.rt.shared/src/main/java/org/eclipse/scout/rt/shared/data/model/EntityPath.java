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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * This path represents the forward path from the data model root to an entity
 * <p>
 * Since the data model is a graph (with potential cycles) the physical instantiation of the datamodel is performed by
 * creating all graph nodes once with single parent linkage. Therefore when holding a {@link IDataModelAttribute} or
 * {@link IDataModelEntity} the forward path to this object from the {@link IDataModel} root is ambiguous. This class
 * solves that problem by containing the complete path.
 *
 * @since 3.8
 */
public final class EntityPath implements IDataModelPath {
  public static final EntityPath EMPTY = new EntityPath();

  private final List<IDataModelEntity> m_entityPath;
  private final int m_hashCode;

  public EntityPath() {
    this(null);
  }

  public EntityPath(List<IDataModelEntity> entityPath) {
    this(entityPath, true);
  }

  /**
   * @param entityPath
   * @param useCopyOfList
   *          if true then the entityPath list is assumed to be mutable and a copy of it is taken.
   */
  EntityPath(List<IDataModelEntity> entityPath, boolean useCopyOfList) {
    if (entityPath == null || entityPath.isEmpty()) {
      m_entityPath = CollectionUtility.emptyArrayList();
    }
    else {
      if (useCopyOfList) {
        m_entityPath = CollectionUtility.arrayList(entityPath);
      }
      else {
        m_entityPath = entityPath;
      }
    }
    m_hashCode = m_entityPath.hashCode();
  }

  /**
   * @return the path of elements from root to last
   */
  public List<IDataModelEntity> elements() {
    return CollectionUtility.arrayList(m_entityPath);
  }

  /**
   * convenience to get first element
   */
  public IDataModelEntity firstElement() {
    return !m_entityPath.isEmpty() ? m_entityPath.get(0) : null;
  }

  /**
   * convenience to get last element
   */
  public IDataModelEntity lastElement() {
    return !m_entityPath.isEmpty() ? m_entityPath.get(m_entityPath.size() - 1) : null;
  }

  public int size() {
    return m_entityPath.size();
  }

  public IDataModelEntity get(int index) {
    return m_entityPath.get(index);
  }

  /**
   * @return true if {@link #size()} is 0
   */
  public boolean isEmpty() {
    return this == EMPTY || size() == 0;
  }

  /**
   * @return a new path without the last component
   */
  public EntityPath parent() {
    if (size() <= 1) {
      return EMPTY;
    }
    return new EntityPath(m_entityPath.subList(0, m_entityPath.size() - 1), false);
  }

  /**
   * @return a new path containing the elements between from (inclusive) and to (exclusive)
   */
  public EntityPath subPath(int fromIndex, int toIndex) {
    if (size() <= 0) {
      return EMPTY;
    }
    return new EntityPath(m_entityPath.subList(fromIndex, toIndex), false);
  }

  /**
   * @return a new path by inserting the component at the front
   */
  public EntityPath addToFront(IDataModelEntity e) {
    if (e == null) {
      return this;
    }
    ArrayList<IDataModelEntity> newList = new ArrayList<>(size() + 1);
    newList.add(e);
    newList.addAll(m_entityPath);
    return new EntityPath(newList, false);
  }

  /**
   * @return a new path by appending the component at the end
   */
  public EntityPath addToEnd(IDataModelEntity e) {
    if (e == null) {
      return this;
    }
    ArrayList<IDataModelEntity> newList = new ArrayList<>(size() + 1);
    newList.addAll(m_entityPath);
    newList.add(e);
    return new EntityPath(newList, false);
  }

  /**
   * @return a new path by inserting the component at the front
   */
  public EntityPath addToFront(EntityPath p) {
    if (p == null || p.size() == 0) {
      return this;
    }
    ArrayList<IDataModelEntity> newList = new ArrayList<>(size() + 1);
    newList.addAll(p.m_entityPath);
    newList.addAll(this.m_entityPath);
    return new EntityPath(newList, false);
  }

  /**
   * @return a new path by appending the component at the end
   */
  public EntityPath addToEnd(EntityPath p) {
    if (p == null || p.size() == 0) {
      return this;
    }
    ArrayList<IDataModelEntity> newList = new ArrayList<>(size() + 1);
    newList.addAll(this.m_entityPath);
    newList.addAll(p.m_entityPath);
    return new EntityPath(newList, false);
  }

  /**
   * @return a new path by appending the component at the end
   */
  public AttributePath addToEnd(IDataModelAttribute a) {
    if (a == null) {
      throw new IllegalArgumentException("attribute is null");
    }
    return new AttributePath(this, a);
  }

  @Override
  public int hashCode() {
    return m_hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof EntityPath)) {
      return false;
    }
    EntityPath other = (EntityPath) obj;
    return this.m_entityPath.equals(other.m_entityPath);
  }

  @Override
  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(getClass().getSimpleName());
    buf.append("[");
    if (isEmpty()) {
      buf.append("<EMPTY>");
    }
    else {
      int index = 0;
      for (IDataModelEntity e : m_entityPath) {
        if (index > 0) {
          buf.append(".");
        }
        buf.append(e.getClass().getSimpleName());
        buf.append("(").append(e.getText()).append(")");
        index++;
      }
    }
    buf.append("]");
    return buf.toString();
  }

}
