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

public class JsUtilityFunction extends AbstractJsElement {
  private final JsUtility m_jsUtility;
  private final String m_name;
  private final boolean m_exported;
  private final String m_tag;

  public JsUtilityFunction(JsUtility jsUtility, String name, boolean exported, String tag) {
    m_jsUtility = jsUtility;
    m_name = name;
    m_exported = exported;
    m_tag = tag;
  }

  public JsUtility getJsUtility() {
    return m_jsUtility;
  }

  public String getName() {
    return m_name;
  }

  public boolean isExported() {
    return m_exported;
  }

  public String getTag() {
    return m_tag;
  }
}
