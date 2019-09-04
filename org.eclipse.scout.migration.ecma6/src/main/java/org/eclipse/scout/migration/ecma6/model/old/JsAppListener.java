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

import org.eclipse.scout.rt.platform.exception.VetoException;

public class JsAppListener extends AbstractJsElement {
  private final JsFile m_jsFile;
  private String m_body;
  private String m_instanceNamespace;
  private String m_instanceName;

  public JsAppListener(JsFile parent){
    m_jsFile = parent;
  }

  public JsFile getJsFile() {
    return m_jsFile;
  }

  public void setInstanceFqn(String instanceFqn) {
    String[] split = instanceFqn.split("\\.");
    if (split.length != 2) {
      throw new VetoException("Could not separate fqn('" + instanceFqn + "') in namespace and name!");
    }
    m_instanceNamespace = split[0];
    m_instanceName = split[1];
  }

  public String getInstanceName() {
    return m_instanceName;
  }

  public String getInstanceNamespace() {
    return m_instanceNamespace;
  }

  public void setBody(String body) {
    m_body = body;
  }

  public String getBody() {
    return m_body;
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent) {
    StringBuilder builder = new StringBuilder();
    builder
      .append("appListener [ instance = ")
    .append(getInstanceNamespace()).append(".").append(getInstanceName()).append("]");
    return builder.toString();
  }
}
