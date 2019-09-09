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

public class JsClassVariable extends AbstractJsElement {
  private final JsClass m_jsClass;
  private final String m_name;
  private final boolean m_const;
  private final boolean m_exported;

  public JsClassVariable(JsClass jsClass, String name, boolean aConst) {
    m_jsClass = jsClass;
    m_name = name;
    m_const = aConst;
    m_exported = !name.startsWith("_");
  }

  public JsClass getJsClass() {
    return m_jsClass;
  }

  public String getName() {
    return m_name;
  }

  public String getFqn(){
    return getJsClass().getFullyQualifiedName() + "." + getName();
  }

  public boolean isConst() {
    return m_const;
  }

  public boolean isExported() {
    return m_exported;
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent){
    StringBuilder builder = new StringBuilder();
    builder.append(getName());
    return builder.toString();
  }
}
