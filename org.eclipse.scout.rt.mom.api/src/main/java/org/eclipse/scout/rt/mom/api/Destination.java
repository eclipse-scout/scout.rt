/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.api;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNullOrEmpty;

import java.util.Collections;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * Lightweight object which describes a messaging destination with no physical resources allocated.
 * <p>
 * Two destination with the same <i>name</i> are considered 'equals'.
 *
 * @see IMom
 * @since 6.1
 */
class Destination<REQUEST, REPLY> implements IBiDestination<REQUEST, REPLY> {

  private final String m_name;
  private final IDestinationType m_type;
  private final IResolveMethod m_resolveMethod;
  private final Map<String, String> m_properties;

  /**
   * @param name
   *          the symbolic name for the destination
   * @param destinationType
   *          the type of the resource that this destination represents, e.g. {@link DestinationType#QUEUE}
   * @param resolveMethod
   *          the method how to resolve the actual destination, e.g. {@link ResolveMethod#JNDI}
   * @param properties
   *          optional map of additional properties used to resolve the destination (may be set to <code>null</code> if
   *          no properties are required)
   * @throws AssertionException
   *           if one of <code>name</code>, <code>type</code> or <code>resolveMethod</code> is <code>null</code> or
   *           empty
   */
  public Destination(final String name, final IDestinationType type, IResolveMethod resolveMethod, Map<String, String> properties) {
    m_name = assertNotNullOrEmpty(name, "destination name not specified");
    m_type = assertNotNull(type, "destination type not specified");
    m_resolveMethod = assertNotNull(resolveMethod, "resolve method not specified");
    m_properties = (properties == null ? Collections.emptyMap() : Collections.unmodifiableMap(properties));
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public IDestinationType getType() {
    return m_type;
  }

  @Override
  public IResolveMethod getResolveMethod() {
    return m_resolveMethod;
  }

  /**
   * @return an unmodifiable map of additional properties (never <code>null</code>).
   */
  public Map<String, String> getProperties() {
    return m_properties;
  }

  /**
   * Two destination with the same <i>name</i> are considered 'equals'.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    return result;
  }

  /**
   * Two destination with the same <i>name</i> are considered 'equals'.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Destination other = (Destination) obj;
    if (m_name == null) {
      if (other.m_name != null) {
        return false;
      }
    }
    else if (!m_name.equals(other.m_name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attr("name", m_name)
        .attr("type", m_type)
        .attr("resolveMethod", m_resolveMethod)
        .attr("properties", m_properties)
        .toString();
  }
}
