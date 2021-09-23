/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.services.common.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

/**
 * Search model that contains a form data, verbose search texts and a valid status
 */
public class SearchFilter implements Serializable {
  private static final long serialVersionUID = 0L;

  private boolean m_completed;
  private AbstractFormData m_formData;
  private IDoEntity m_data;
  private List<String> m_displayTexts;

  public SearchFilter() {
    m_displayTexts = new ArrayList<>();
  }

  protected SearchFilter(SearchFilter other) {
    m_completed = other.m_completed;
    if (other.m_formData != null) {
      m_formData = other.m_formData.deepCopy();
    }
    if (other.m_data != null) {
      IDataObjectMapper mapper = BEANS.get(IDataObjectMapper.class);
      m_data = mapper.readValue(mapper.writeValue(other.m_data), other.m_data.getClass());
    }
    m_displayTexts = new ArrayList<>(other.m_displayTexts);
  }

  /**
   * @return live map of all binds changes to this map are immediately reflected inside the instance
   */
  public AbstractFormData getFormData() {
    return m_formData;
  }

  public void setFormData(AbstractFormData formData) {
    m_formData = formData;
  }

  public void addDisplayText(String s) {
    m_displayTexts.add(s);
  }

  public IDoEntity getData() {
    return m_data;
  }

  public void setData(IDoEntity data) {
    m_data = data;
  }

  /**
   * clear all elements in the filter
   */
  public void clear() {
    m_completed = false;
    m_displayTexts.clear();
    m_formData = null;
    m_data = null;
  }

  public String[] getDisplayTexts() {
    return m_displayTexts.toArray(new String[0]);
  }

  public void setDisplayTexts(String[] displayTexts) {
    m_displayTexts = CollectionUtility.arrayList(displayTexts);
  }

  public String getDisplayTextsPlain() {
    StringBuilder buf = new StringBuilder();
    char nl = '\n';
    for (String s : getDisplayTexts()) {
      if (s != null) {
        buf.append(s.trim());
        buf.append(nl);
      }
    }
    if (buf.length() > 0 && buf.charAt(buf.length() - 1) == nl) {
      buf.deleteCharAt(buf.length() - 1);
    }
    return buf.toString();
  }

  public boolean isCompleted() {
    return m_completed;
  }

  public void setCompleted(boolean b) {
    m_completed = b;
  }

  /**
   * Creates a copy of this instance. The copy is basically a deep copy, but immutable objects are shallow copied.
   */
  public SearchFilter copy() {
    return new SearchFilter(this);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(getClass().getSimpleName());
    buf.append('[');
    if (m_formData != null) {
      buf.append(m_formData.toString());
    }
    if (m_data != null) {
      buf.append(BEANS.get(IPrettyPrintDataObjectMapper.class).writeValue(m_data));
    }
    buf.append(']');
    return buf.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (m_completed ? 1231 : 1237);
    result = prime * result + ((m_displayTexts == null) ? 0 : m_displayTexts.hashCode());
    result = prime * result + ((m_formData == null) ? 0 : m_formData.hashCode());
    result = prime * result + ((m_data == null) ? 0 : m_data.hashCode());
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
    SearchFilter other = (SearchFilter) obj;
    if (m_completed != other.m_completed) {
      return false;
    }
    if (m_displayTexts == null) {
      if (other.m_displayTexts != null) {
        return false;
      }
    }
    else if (!m_displayTexts.equals(other.m_displayTexts)) {
      return false;
    }
    if (m_formData == null) {
      if (other.m_formData != null) {
        return false;
      }
    }
    else if (!m_formData.equals(other.m_formData)) {
      return false;
    }
    if (m_data == null) {
      if (other.m_data != null) {
        return false;
      }
    }
    else if (!m_data.equals(other.m_data)) {
      return false;
    }
    return true;
  }
}
