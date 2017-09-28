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
package org.eclipse.scout.rt.platform.config;

/**
 * <h3>{@link ConfigPropertyChangeEvent}</h3><br>
 * Describes the change of a {@link IConfigProperty}.
 */
public class ConfigPropertyChangeEvent {

  /**
   * Event describing a manual change of the property by using {@link IConfigProperty#setValue(Object)};
   */
  public static final int TYPE_VALUE_CHANGED = 1;

  /**
   * Event describing that the property value was invalidated. After the next access to the property value an event of
   * type {@link #TYPE_VALUE_INITIALIZED} will be fired.<br>
   * This event type has no namespace and no old or new value defined because it means all values of a property (all
   * namespaces) are invalidated.
   */
  public static final int TYPE_INVALIDATE = 2;

  /**
   * Event describing that the property initialized its value because it was accessed and has not yet initialized its
   * value or has been invalidated before ({@link IConfigProperty#invalidate()}).
   */
  public static final int TYPE_VALUE_INITIALIZED = 3;

  private final IConfigProperty<?> m_configProperty;
  private final String m_namespace;
  private final Object m_oldValue;
  private final Object m_newValue;
  private final int m_type;

  public ConfigPropertyChangeEvent(IConfigProperty<?> configProperty, Object oldValue, Object newValue, String namespace, int type) {
    m_configProperty = configProperty;
    m_oldValue = oldValue;
    m_newValue = newValue;
    m_type = type;
    m_namespace = namespace;
  }

  /**
   * @return The {@link IConfigProperty} instance that changed.
   */
  public IConfigProperty<?> getConfigProperty() {
    return m_configProperty;
  }

  /**
   * @return The old value of the property before the change.
   */
  public Object getOldValue() {
    return m_oldValue;
  }

  /**
   * @return The new (and now current) value of the property.
   */
  public Object getNewValue() {
    return m_newValue;
  }

  /**
   * @return The type of event. One of {@link #TYPE_VALUE_CHANGED}, {@link #TYPE_INVALIDATE},
   *         {@link #TYPE_VALUE_INITIALIZED}.
   */
  public int getType() {
    return m_type;
  }

  /**
   * @return The namespace of the changed event
   */
  public String getNamespace() {
    return m_namespace;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_configProperty == null) ? 0 : m_configProperty.hashCode());
    result = prime * result + ((m_namespace == null) ? 0 : m_namespace.hashCode());
    result = prime * result + ((m_newValue == null) ? 0 : m_newValue.hashCode());
    result = prime * result + ((m_oldValue == null) ? 0 : m_oldValue.hashCode());
    result = prime * result + m_type;
    return result;
  }

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
    ConfigPropertyChangeEvent other = (ConfigPropertyChangeEvent) obj;
    if (m_configProperty == null) {
      if (other.m_configProperty != null) {
        return false;
      }
    }
    else if (!m_configProperty.equals(other.m_configProperty)) {
      return false;
    }
    if (m_namespace == null) {
      if (other.m_namespace != null) {
        return false;
      }
    }
    else if (!m_namespace.equals(other.m_namespace)) {
      return false;
    }
    if (m_newValue == null) {
      if (other.m_newValue != null) {
        return false;
      }
    }
    else if (!m_newValue.equals(other.m_newValue)) {
      return false;
    }
    if (m_oldValue == null) {
      if (other.m_oldValue != null) {
        return false;
      }
    }
    else if (!m_oldValue.equals(other.m_oldValue)) {
      return false;
    }
    if (m_type != other.m_type) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ConfigPropertyChangeEvent [");
    builder.append("property=").append(m_configProperty.getKey()).append(", ");
    if (m_namespace != null) {
      builder.append("namespace=").append(m_namespace).append(", ");
    }
    if (m_oldValue != null) {
      builder.append("oldValue=").append(m_oldValue).append(", ");
    }
    if (m_newValue != null) {
      builder.append("newValue=").append(m_newValue).append(", ");
    }
    builder.append("type=").append(m_type).append("]");
    return builder.toString();
  }
}
