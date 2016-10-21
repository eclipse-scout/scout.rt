package org.eclipse.scout.rt.client.ui;

import java.io.Serializable;
import java.math.BigDecimal;

import org.eclipse.scout.rt.platform.util.NumberUtility;

/**
 * Represents a geographical location. Format is decimal degrees.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Decimal_degrees">https://en.wikipedia.org/wiki/Decimal_degrees</a>
 * @since 6.1
 */
public class Coordinates implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_latitude;
  private final String m_longitude;

  /**
   * @param latitude
   *          Latitude (φ, y coordinate)
   * @param longitude
   *          Longitude (λ, x coordinate)
   */
  public Coordinates(String latitude, String longitude) {
    m_latitude = latitude;
    m_longitude = longitude;
  }

  /**
   * @param latitude
   *          Latitude (φ, y coordinate)
   * @param longitude
   *          Longitude (λ, x coordinate)
   */
  public Coordinates(BigDecimal latitude, BigDecimal longitude) {
    m_latitude = (latitude == null ? null : latitude.toPlainString());
    m_longitude = (longitude == null ? null : longitude.toPlainString());
  }

  /**
   * Latitude (φ, y coordinate)
   * <p>
   * Example: Berne, Switzerland has latitude <code>7.45</code>
   * <p>
   * Format should be "decimal degree" (see {@link Coordinates})
   */
  public String getLatitude() {
    return m_latitude;
  }

  /**
   * Latitude (φ, y coordinate)
   * <p>
   * Example: Berne, Switzerland has latitude <code>7.45</code>
   */
  public BigDecimal getLatitudeAsBigDecimal() {
    return NumberUtility.getBigDecimalValue(getLatitude());
  }

  /**
   * Longitude (λ, x coordinate)
   * <p>
   * Example: Berne, Switzerland has longitude <code>46.95</code>
   * <p>
   * Format should be "decimal degree" (see {@link Coordinates})
   */
  public String getLongitude() {
    return m_longitude;
  }

  /**
   * Longitude (λ, x coordinate)
   * <p>
   * Example: Berne, Switzerland has longitude <code>46.95</code>
   */
  public BigDecimal getLongitudeAsBigDecimal() {
    return NumberUtility.getBigDecimalValue(getLongitude());
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

  @Override
  public String toString() {
    return "Coordinates [m_latitude=" + m_latitude + ", m_longitude=" + m_longitude + "]";
  }
}
