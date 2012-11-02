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
package org.eclipse.scout.jaxws.internal.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import com.sun.xml.internal.ws.api.ResourceLoader;
import com.sun.xml.internal.ws.api.server.BoundEndpoint;
import com.sun.xml.internal.ws.api.server.Container;
import com.sun.xml.internal.ws.api.server.WebModule;

public class ServletContainer extends Container {

  private final WebModule m_module = new P_ServletModule();
  private final ResourceLoader m_resourceLoader;

  public ServletContainer(final ResourceLoader resourceLoader) {
    m_resourceLoader = resourceLoader;
  }

  @Override
  public <T> T getSPI(Class<T> spiType) {
    if (spiType.isAssignableFrom(m_module.getClass())) {
      return spiType.cast(m_module);
    }
    if (spiType == ResourceLoader.class) {
      return spiType.cast(m_resourceLoader);
    }
    return null;
  }

  private class P_ServletModule extends WebModule {

    private final List<BoundEndpoint> m_endpoints = new ArrayList<BoundEndpoint>();

    @Override
    public String getContextPath() {
      throw new WebServiceException("Container " + ServletContainer.class.getName() + " does not support getContextPath()");
    }

    @Override
    public List<BoundEndpoint> getBoundEndpoints() {
      return m_endpoints;
    }
  }
}
