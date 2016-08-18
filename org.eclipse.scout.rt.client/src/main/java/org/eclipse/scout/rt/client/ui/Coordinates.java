package org.eclipse.scout.rt.client.ui;

import java.io.Serializable;

/**
 * Represents a geographical location. Format is decimal degrees.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Decimal_degrees">https://en.wikipedia.org/wiki/Decimal_degrees</a>
 * @since 6.1
 */
public class Coordinates implements Serializable {

  private static final long serialVersionUID = 1L;
  private String m_latitude;
  private String m_longitude;

  public Coordinates(String latitude, String longitude) {
    super();
    m_latitude = latitude;
    m_longitude = longitude;
  }

  public String getLatitude() {
    return m_latitude;
  }

  public String getLongitude() {
    return m_longitude;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_latitude == null) ? 0 : m_latitude.hashCode());
    result = prime * result + ((m_longitude == null) ? 0 : m_longitude.hashCode());
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
    Coordinates other = (Coordinates) obj;
    if (m_latitude == null) {
      if (other.m_latitude != null) {
        return false;
      }
    }
    else if (!m_latitude.equals(other.m_latitude)) {
      return false;
    }
    if (m_longitude == null) {
      if (other.m_longitude != null) {
        return false;
      }
    }
    else if (!m_longitude.equals(other.m_longitude)) {
      return false;
    }
    return true;
  }

}
