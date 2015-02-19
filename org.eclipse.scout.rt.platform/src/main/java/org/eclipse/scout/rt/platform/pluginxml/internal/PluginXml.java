/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.pluginxml.internal;

import java.net.URL;

/**
 * The default implementation of {@link IPluginXml}. Classloading with {@link Class#forName(String)}.
 *
 * @since 5.1
 */
public class PluginXml implements IPluginXml {

  private final URL m_pluginXmlUrl;

  public PluginXml(URL pluginXmlUrl) {
    m_pluginXmlUrl = pluginXmlUrl;
  }

  @Override
  public URL getUrl() {
    return m_pluginXmlUrl;
  }

  @Override
  public Class<?> loadClass(String fullyQuallifiedName) throws ClassNotFoundException {
    return Class.forName(fullyQuallifiedName);
  }

  @Override
  public String toString() {
    return String.format("Xml file '%s'", getUrl());
  }
}
