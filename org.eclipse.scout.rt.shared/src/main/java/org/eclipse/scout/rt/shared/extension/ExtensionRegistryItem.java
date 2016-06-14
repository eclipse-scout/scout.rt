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
package org.eclipse.scout.rt.shared.extension;

import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.platform.util.BeanUtility;

public class ExtensionRegistryItem extends AbstractExtensionRegistryItem {

  private final ClassIdentifier m_ownerClass;
  private final Class<?> m_declaringClass;
  private final Class<?> m_extensionClass;

  public ExtensionRegistryItem(ClassIdentifier originalClass, Class<?> extensionClass, Double modelOrder, long order) {
    this(originalClass, null, extensionClass, modelOrder, order);
  }

  public ExtensionRegistryItem(ClassIdentifier originalClass, Class<?> declaringClass, Class<?> extensionClass, Double modelOrder, long order) {
    super(order, modelOrder);
    m_ownerClass = originalClass;
    m_declaringClass = declaringClass;
    m_extensionClass = extensionClass;
  }

  public <T> T createInstance(Object owner, ExtensionStack extensionStack) {
    Object declaringObject = null;
    if (getDeclaringClass() != null) {
      // find declaring class instance on extension stack
      declaringObject = extensionStack.findContextObjectByClass(getDeclaringClass());
    }
    return createInstance(owner, declaringObject);
  }

  @SuppressWarnings("unchecked")
  public <T> T createInstance(Object owner, Object declaringObject) {
    T resultingInstance = null;
    RuntimeException ex = null;
    try {
      boolean isExtension = IExtension.class.isAssignableFrom(m_extensionClass);
      if (isExtension) {
        if (getDeclaringClass() == null) {
          resultingInstance = (T) BeanUtility.createInstance(m_extensionClass, owner);
        }
        else {
          resultingInstance = (T) BeanUtility.createInstance(m_extensionClass, declaringObject, owner);
        }
      }
      else {
        if (getDeclaringClass() == null) {
          resultingInstance = (T) BeanUtility.createInstance(m_extensionClass);
        }
        else {
          resultingInstance = (T) BeanUtility.createInstance(m_extensionClass, declaringObject);
        }
      }
    }
    catch (RuntimeException e) {
      ex = e;
    }
    if (resultingInstance == null) {
      throw new IllegalExtensionException("Cannot create instance of class '" + m_extensionClass.getName() + "'. No constructor found that matches available input parameters.", ex);
    }

    // apply order if provided and supported
    Double order = getNewModelOrder();
    if (order != null && resultingInstance instanceof IOrdered) {
      ((IOrdered) resultingInstance).setOrder(order.doubleValue());
    }
    return resultingInstance;
  }

  public Class<?> getOriginalClass() {
    return m_ownerClass.getLastSegment();
  }

  public ClassIdentifier getOriginalClassIdentifier() {
    return m_ownerClass;
  }

  public Class<?> getDeclaringClass() {
    return m_declaringClass;
  }

  public Class<?> getExtensionClass() {
    return m_extensionClass;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_declaringClass == null) ? 0 : m_declaringClass.hashCode());
    result = prime * result + ((m_extensionClass == null) ? 0 : m_extensionClass.hashCode());
    result = prime * result + ((m_ownerClass == null) ? 0 : m_ownerClass.hashCode());
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
    if (!(obj instanceof ExtensionRegistryItem)) {
      return false;
    }
    ExtensionRegistryItem other = (ExtensionRegistryItem) obj;
    if (m_declaringClass == null) {
      if (other.m_declaringClass != null) {
        return false;
      }
    }
    else if (!m_declaringClass.equals(other.m_declaringClass)) {
      return false;
    }
    if (m_extensionClass == null) {
      if (other.m_extensionClass != null) {
        return false;
      }
    }
    else if (!m_extensionClass.equals(other.m_extensionClass)) {
      return false;
    }
    if (m_ownerClass == null) {
      if (other.m_ownerClass != null) {
        return false;
      }
    }
    else if (!m_ownerClass.equals(other.m_ownerClass)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Extension '").append(m_extensionClass.getName()).append("' for '").append(m_ownerClass).append("'.");
    return sb.toString();
  }
}
