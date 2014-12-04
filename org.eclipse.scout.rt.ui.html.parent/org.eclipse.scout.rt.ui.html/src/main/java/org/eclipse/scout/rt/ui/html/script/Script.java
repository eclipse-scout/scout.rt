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

import java.net.URL;

public class Script {
  private final String m_path;
  private final URL m_url;
  private final IWebContentResourceLocator m_loc;

  public Script(String path, URL url, IWebContentResourceLocator loc) {
    m_path = path;
    m_url = url;
    m_loc = loc;
  }

  public String getPath() {
    return m_path;
  }

  public URL getURL() {
    return m_url;
  }

  public IWebContentResourceLocator getScriptLocator() {
    return m_loc;
  }
}
