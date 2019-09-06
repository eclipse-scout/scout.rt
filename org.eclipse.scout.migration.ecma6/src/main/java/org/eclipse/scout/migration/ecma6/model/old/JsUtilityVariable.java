/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6.model.old;

public class JsUtilityVariable extends AbstractJsElement {
  private final JsUtility m_jsUtility;
  private final String m_name;
  private final String m_valueOrFirstLine;
  private final boolean m_exported;
  private final boolean m_standalone;
  private final String m_tag;

  public JsUtilityVariable(JsUtility jsUtility, String name, String valueOrFirstLine, boolean exported, boolean standalone, String tag) {
    m_jsUtility = jsUtility;
    m_name = name;
    m_valueOrFirstLine = valueOrFirstLine;
    m_exported = exported;
    m_standalone = standalone;
    m_tag = tag;
  }

  public JsUtility getJsUtility() {
    return m_jsUtility;
  }

  public String getName() {
    return m_name;
  }

  public String getValueOrFirstLine() {
    return m_valueOrFirstLine;
  }

  public boolean isExported() {
    return m_exported;
  }

  public boolean isStandalone() {
    return m_standalone;
  }

  public String getTag() {
    return m_tag;
  }
}
