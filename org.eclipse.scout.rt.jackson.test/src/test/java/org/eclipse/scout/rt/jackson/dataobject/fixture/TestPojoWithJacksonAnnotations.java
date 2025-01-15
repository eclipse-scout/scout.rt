/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Date;

import org.eclipse.scout.rt.dataobject.IValueFormatConstants;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test POJO used to test a set of jackson annotations together with data object {@link ObjectMapper}
 */
public class TestPojoWithJacksonAnnotations {

  private String m_id;
  private Date m_defaultDate;
  private Date m_formattedDate;
  private String m_renamedAttribute;
  private double m_ignoredAttribute;

  public String getId() {
    return m_id;
  }

  public void setId(String id) {
    m_id = id;
  }

  public Date getDefaultDate() {
    return m_defaultDate;
  }

  public void setDefaultDate(Date defaultDate) {
    m_defaultDate = defaultDate;
  }

  @JsonFormat(pattern = IValueFormatConstants.DATE_PATTERN)
  public Date getFormattedDate() {
    return m_formattedDate;
  }

  public void setFormattedDate(Date formattedDate) {
    m_formattedDate = formattedDate;
  }

  @JsonProperty("customRenamedAttribute")
  public String getRenamedAttribute() {
    return m_renamedAttribute;
  }

  public void setRenamedAttribute(String renamedAttribute) {
    m_renamedAttribute = renamedAttribute;
  }

  @JsonIgnore
  public double getIgnoredAttribute() {
    return m_ignoredAttribute;
  }

  public void setIgnoredAttribute(double ignoredAttribute) {
    m_ignoredAttribute = ignoredAttribute;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_defaultDate == null) ? 0 : m_defaultDate.hashCode());
    result = prime * result + ((m_formattedDate == null) ? 0 : m_formattedDate.hashCode());
    result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
    long temp;
    temp = Double.doubleToLongBits(m_ignoredAttribute);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((m_renamedAttribute == null) ? 0 : m_renamedAttribute.hashCode());
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
    TestPojoWithJacksonAnnotations other = (TestPojoWithJacksonAnnotations) obj;
    if (m_defaultDate == null) {
      if (other.m_defaultDate != null) {
        return false;
      }
    }
    else if (!m_defaultDate.equals(other.m_defaultDate)) {
      return false;
    }
    if (m_formattedDate == null) {
      if (other.m_formattedDate != null) {
        return false;
      }
    }
    else if (!m_formattedDate.equals(other.m_formattedDate)) {
      return false;
    }
    if (m_id == null) {
      if (other.m_id != null) {
        return false;
      }
    }
    else if (!m_id.equals(other.m_id)) {
      return false;
    }
    if (Double.doubleToLongBits(m_ignoredAttribute) != Double.doubleToLongBits(other.m_ignoredAttribute)) {
      return false;
    }
    if (m_renamedAttribute == null) {
      if (other.m_renamedAttribute != null) {
        return false;
      }
    }
    else if (!m_renamedAttribute.equals(other.m_renamedAttribute)) {
      return false;
    }
    return true;
  }
}
