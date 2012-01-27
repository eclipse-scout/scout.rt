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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

import com.sun.xml.internal.ws.api.server.BoundEndpoint;
import com.sun.xml.internal.ws.api.server.Module;
import com.sun.xml.internal.ws.api.server.WSEndpoint;
import com.sun.xml.internal.ws.api.server.WebModule;
import com.sun.xml.internal.ws.transport.http.HttpAdapter;
import com.sun.xml.internal.ws.transport.http.HttpAdapterList;
import com.sun.xml.internal.ws.transport.http.WSHTTPConnection;

public class ServletAdapter extends HttpAdapter implements BoundEndpoint {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServletAdapter.class);

  private String m_alias;

  public ServletAdapter(WSEndpoint endpoint, String alias, String urlPattern, HttpAdapterList<? extends HttpAdapter> owner) {
    super(endpoint, owner, urlPattern);
    m_alias = alias;

    registerEndpoint();
  }

  public void handle(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws IOException {
    WSHTTPConnection connection = new ServletConnection(this, context, request, response);
    super.handle(connection);
  }

  @Override
  public URI getAddress() {
    WebModule webModule = endpoint.getContainer().getSPI(WebModule.class);
    if (webModule == null) {
      throw new WebServiceException("Container " + endpoint.getContainer().getClass().getName() + " does not support " + WebModule.class.getName());
    }
    return getAddress(webModule.getContextPath());
  }

  @Override
  public URI getAddress(String baseAddress) {
    String uri = StringUtility.join("", baseAddress, getValidPath());
    try {
      return new URI(uri);
    }
    catch (URISyntaxException e) {
      throw new WebServiceException("Invalid URI '" + uri + "'", e);
    }
  }

  public String getAlias() {
    return m_alias;
  }

  @Override
  public String toString() {
    return StringUtility.join(" ", super.toString(), "[endpoint=" + getAlias() + "]");
  }

  private void registerEndpoint() {
    Module module = getEndpoint().getContainer().getSPI(Module.class);
    if (module == null) {
      LOG.warn("Container " + endpoint.getContainer().getClass().getName() + " does not support " + Module.class.getName());
      return;
    }
    module.getBoundEndpoints().add(this);
  }
}
