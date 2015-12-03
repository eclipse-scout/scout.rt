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
package org.eclipse.scout.rt.shared.services.common.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

/**
 * Search model that contains a form data, verbose search texts and a valid status
 */
public class SearchFilter implements Serializable, Cloneable {
  private static final long serialVersionUID = 0L;

  private boolean m_completed;
  private AbstractFormData m_formData;
  private ArrayList<String> m_displayTexts = new ArrayList<String>();

  public SearchFilter() {
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

  /**
   * clear all elements in the filter
   */
  public void clear() {
    m_completed = false;
    m_displayTexts.clear();
    m_formData = null;
  }

  public String[] getDisplayTexts() {
    return m_displayTexts.toArray(new String[0]);
  }

  public void setDisplayTexts(String[] displayTexts) {
    if (displayTexts == null) {
      displayTexts = new String[0];
    }
    m_displayTexts = new ArrayList<String>(Arrays.asList(displayTexts));
  }

  public String getDisplayTextsPlain() {
    StringBuffer buf = new StringBuffer();
    for (String s : getDisplayTexts()) {
      if (s != null) {
        buf.append(s.trim());
        buf.append("\n");
      }
    }
    return buf.toString().trim();
  }

  public boolean isCompleted() {
    return m_completed;
  }

  public void setCompleted(boolean b) {
    m_completed = b;
  }

  @Override
  public Object clone() {
    SearchFilter f;
    try {
      f = (SearchFilter) super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
    f.m_completed = m_completed;
    if (m_formData != null) {
      f.m_formData = (AbstractFormData) m_formData.clone();
    }
    f.m_displayTexts = new ArrayList<String>(m_displayTexts);
    return f;
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getClass().getSimpleName());
    buf.append("[");
    if (m_formData != null) {
      buf.append(m_formData.toString());
    }
    buf.append("]");
    return buf.toString();
  }
}
