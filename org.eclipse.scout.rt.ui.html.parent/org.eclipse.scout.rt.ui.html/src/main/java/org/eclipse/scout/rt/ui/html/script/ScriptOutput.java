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
package org.eclipse.scout.rt.ui.html.script;

public class ScriptOutput {
  private final String m_pathInfo;
  private final byte[] m_content;
  private final long m_lastModified;

  public ScriptOutput(String pathInfo, byte[] content, long lastModified) {
    m_pathInfo = pathInfo;
    m_content = content;
    m_lastModified = lastModified;
  }

  public String getPathInfo() {
    return m_pathInfo;
  }

  public byte[] getContent() {
    return m_content;
  }

  public long getLastModified() {
    return m_lastModified;
  }
}
