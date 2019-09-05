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

public class JsTopLevelEnum extends AbstractJsElement {

  private final JsFile m_jsFile;
  private String m_namespace;
  private String m_name;

  public JsTopLevelEnum(String namespace, String name, JsFile jsFile) {
    m_namespace = namespace;
    m_name = name;
    m_jsFile = jsFile;
  }

  public String getNamespace() {
    return m_namespace;
  }

  public String getName() {
    return m_name;
  }

  public String getFqn() {
    return m_namespace + "." + m_name;
  }


  public JsFile getJsFile() {
    return m_jsFile;
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent) {
    StringBuilder builder = new StringBuilder();
    builder.append(getName());
    return builder.toString();
  }
}
