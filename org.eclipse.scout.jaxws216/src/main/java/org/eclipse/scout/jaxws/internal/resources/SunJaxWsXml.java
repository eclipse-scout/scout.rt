/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.internal.resources;

import java.net.URL;

import org.osgi.framework.Bundle;

public class SunJaxWsXml {
  private final Bundle m_bundle;
  private final URL m_resource;

  public SunJaxWsXml(Bundle bundle, URL resource) {
    m_bundle = bundle;
    m_resource = resource;
  }

  public Bundle getBundle() {
    return m_bundle;
  }

  public URL getResource() {
    return m_resource;
  }
}
