package org.eclipse.scout.rt.mom.api;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * Lightweight object which describes a messaging destination with no physical resources allocated.
 *
 * @see IMom
 * @since 6.1
 */
class Destination<REQUEST, REPLY> implements IBiDestination<REQUEST, REPLY> {

  private final int m_type;
  private final String m_name;

  public Destination(final String name, final int type) {
    m_name = assertNotNull(name, "destination not specified");
    m_type = type;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public int getType() {
    return m_type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_name.hashCode();
    result = prime * result + m_type;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Destination other = (Destination) obj;
    if (!m_name.equals(other.m_name)) {
      return false;
    }
    if (m_type != other.m_type) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    switch (m_type) {
      case QUEUE:
        return new ToStringBuilder(this).attr("queue", m_name).toString();
      case TOPIC:
        return new ToStringBuilder(this).attr("topic", m_name).toString();
      case JNDI_LOOKUP:
        return new ToStringBuilder(this).attr("jndi", m_name).toString();
      default:
        return m_name;
    }
  }
}
