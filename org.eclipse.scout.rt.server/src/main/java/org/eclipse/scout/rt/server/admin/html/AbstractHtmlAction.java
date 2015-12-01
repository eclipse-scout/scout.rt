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
package org.eclipse.scout.rt.server.admin.html;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

public abstract class AbstractHtmlAction implements Runnable, Serializable {
  private static final long serialVersionUID = -1352934517273013173L;
  private Map<String, String> m_formParameters = new HashMap<String, String>();
  private String m_plainText;
  private String m_htmlText;
  private Throwable m_exception;
  private String m_uid;

  public AbstractHtmlAction(String uid) {
    super();
    m_uid = uid;
  }

  public String getUid() {
    return m_uid;
  }

  public void setFormParameters(Map<String, String> m) {
    m_formParameters = CollectionUtility.copyMap(m);
  }

  public Map getFormParameters() {
    return m_formParameters;
  }

  public void setPlainText(String s) {
    m_plainText = s;
  }

  public String getPlainText() {
    return m_plainText;
  }

  public void setHtmlText(String s) {
    m_htmlText = s;
  }

  public String getHtmlText() {
    return m_htmlText;
  }

  public void setException(Throwable e) {
    m_exception = e;
  }

  public Throwable getException() {
    return m_exception;
  }

  public String getFormParameter(String name, String defaultValue) {
    String s = m_formParameters.get(name);
    if (s == null) {
      s = defaultValue;
    }
    return s;
  }
}
