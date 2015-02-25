/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.osgi;

import java.io.Serializable;

/**
 * Convenience holder with a bundle symbolic name and a classname.
 */
public class BundleClassDescriptor implements Serializable {
  private static final long serialVersionUID = 1L;

  private String m_bundleSymbolicName;
  private String m_className;

  public BundleClassDescriptor(String bundleSymbolicName, String className) {
    if (bundleSymbolicName == null) {
      throw new IllegalArgumentException("bundleSymbolicName must not be null");
    }
    if (className == null) {
      throw new IllegalArgumentException("className must not be null");
    }
    m_bundleSymbolicName = bundleSymbolicName;
    m_className = className;
  }

  /**
   * @return the bundleSymbolicName
   */
  public String getBundleSymbolicName() {
    return m_bundleSymbolicName;
  }

  /**
   * @return the className
   */
  public String getClassName() {
    return m_className;
  }

  /**
   * @return the class simple name
   */
  public String getSimpleClassName() {
    int i = m_className.lastIndexOf('.');
    if (i >= 0) {
      return m_className.substring(i + 1);
    }
    else {
      return m_className;
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return m_bundleSymbolicName.hashCode() ^ m_className.hashCode();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BundleClassDescriptor) {
      BundleClassDescriptor o = (BundleClassDescriptor) obj;
      return o.m_bundleSymbolicName.equals(this.m_bundleSymbolicName) && o.m_className.equals(this.m_className);
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + m_className + "@" + m_bundleSymbolicName + "]";
  }
}
