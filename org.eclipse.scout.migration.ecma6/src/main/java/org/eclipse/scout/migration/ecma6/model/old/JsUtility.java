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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsUtility extends AbstractJsElement {
  private final JsFile m_jsFile;
  private final String m_namespace;
  private final String m_name;
  private final String m_startTag;
  private final List<JsUtilityFunction> m_functions = new ArrayList<>();
  private final List<JsUtilityVariable> m_variables = new ArrayList<>();

  public JsUtility(JsFile jsFile, String namespace, String name, String startTag) {
    m_jsFile = jsFile;
    m_namespace = namespace;
    m_name = name;
    m_startTag = startTag;
  }

  public String getNamespace() {
    return m_namespace;
  }

  public String getName() {
    return m_name;
  }

  public String getFullyQualifiedName() {
    return m_namespace == null ? m_name : m_namespace + "." + m_name;
  }

  public JsFile getJsFile() {
    return m_jsFile;
  }

  public String getStartTag() {
    return m_startTag;
  }

  public JsUtilityFunction addFunction(String name, boolean exported, String tag) {
    JsUtilityFunction f = new JsUtilityFunction(this, name, exported, tag);
    m_functions.add(f);
    return f;
  }

  public List<JsUtilityFunction> getFunctions() {
    return Collections.unmodifiableList(m_functions);
  }

  public JsUtilityFunction getFunction(String name) {
    return m_functions.stream().filter(fun -> name.equals(fun.getName())).findFirst().orElse(null);
  }

  public JsUtilityVariable addVariable(String name, String valueOrFirstLine, boolean exported, String tag) {
    JsUtilityVariable v = new JsUtilityVariable(this, name, valueOrFirstLine, exported, tag);
    m_variables.add(v);
    return v;
  }

  public List<JsUtilityVariable> getVariables() {
    return Collections.unmodifiableList(m_variables);
  }

  public JsUtilityVariable getVariable(String name) {
    return m_variables.stream().filter(v -> name.equals(v.getName())).findFirst().orElse(null);
  }
}
