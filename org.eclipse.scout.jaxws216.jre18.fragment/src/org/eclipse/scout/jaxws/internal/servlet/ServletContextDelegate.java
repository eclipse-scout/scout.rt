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

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.eclipse.scout.commons.StringUtility;

import com.sun.xml.internal.ws.api.message.Packet;
import com.sun.xml.internal.ws.api.server.PortAddressResolver;
import com.sun.xml.internal.ws.api.server.WSEndpoint;
import com.sun.xml.internal.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.internal.ws.transport.http.HttpAdapter;

@SuppressWarnings("restriction")
public class ServletContextDelegate implements WebServiceContextDelegate {

  private HttpAdapter m_servletAdapter;
  private HttpServletRequest m_request;

  public ServletContextDelegate(HttpAdapter servletAdapter, HttpServletRequest request) {
    m_servletAdapter = servletAdapter;
    m_request = request;
  }

  @Override
  public Principal getUserPrincipal(Packet packet) {
    return m_request.getUserPrincipal();
  }

  @Override
  public boolean isUserInRole(Packet packet, String role) {
    return m_request.isUserInRole(role);
  }

  @Override
  public String getEPRAddress(Packet packet, WSEndpoint endpoint) {
    String baseAddress = JaxWsHelper.getBaseAddress(m_request, true);
    PortAddressResolver resolver = m_servletAdapter.owner.createPortAddressResolver(baseAddress, endpoint.getClass());
    QName serviceQName = endpoint.getServiceName();
    QName portQName = endpoint.getPortName();

    String address = resolver.getAddressFor(serviceQName, portQName.getLocalPart());
    if (address == null) {
      throw new WebServiceException("Failed to find address for port '" + portQName + "'");
    }
    return address;
  }

  @Override
  public String getWSDLAddress(Packet packet, WSEndpoint endpoint) {
    if (m_servletAdapter.getEndpoint().getPort() != null) {
      return StringUtility.join("", getEPRAddress(packet, endpoint), "?wsdl");
    }
    return null;
  }
}
