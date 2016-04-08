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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.handler.MessageContext;

import com.sun.xml.internal.ws.api.message.Packet;
import com.sun.xml.internal.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.internal.ws.developer.JAXWSProperties;
import com.sun.xml.internal.ws.transport.http.HttpAdapter;
import com.sun.xml.internal.ws.transport.http.WSHTTPConnection;

@SuppressWarnings("restriction")
public class ServletConnection extends WSHTTPConnection {

  private static final PropertyMap PROP_MODEL;

  private final HttpServletRequest m_request;
  private final HttpServletResponse m_response;
  private final ServletContext m_context;
  private final WebServiceContextDelegate m_contextDelegate;
  private int m_status = HttpServletResponse.SC_OK;

  private final Map<String, List<String>> m_responseHeaderMap;

  static {
    PROP_MODEL = parse(ServletConnection.class);
  }

  public ServletConnection(HttpAdapter adapter, ServletContext context, HttpServletRequest request, HttpServletResponse response) {
    m_context = context;
    m_request = request;
    m_response = response;
    m_contextDelegate = createContextDelegate(adapter, request);
    m_responseHeaderMap = new HashMap<String, List<String>>();
  }

  @Property(MessageContext.SERVLET_RESPONSE)
  public HttpServletResponse getResponse() {
    return m_response;
  }

  @Property(MessageContext.SERVLET_REQUEST)
  public HttpServletRequest getRequest() {
    return m_request;
  }

  @Property(JAXWSProperties.HTTP_REQUEST_URL)
  public String getRequestURL() {
    return m_request.getRequestURL().toString();
  }

  @Override
  public void setStatus(int status) {
    if (status > 0) {
      m_status = status;
    }
    else {
      throw new IllegalArgumentException("Invalid status code: " + status);
    }
  }

  @Override
  @Property(MessageContext.HTTP_RESPONSE_CODE)
  public int getStatus() {
    return m_status;
  }

  @Override
  public boolean isSecure() {
    return m_request.getScheme().equals("https");
  }

  @Override
  public InputStream getInput() throws IOException {
    return m_request.getInputStream();
  }

  @Override
  public String getProtocol() {
    return m_request.getProtocol();
  }

  @Override
  public OutputStream getOutput() throws IOException {
    m_response.setStatus(m_status);
    return m_response.getOutputStream();
  }

  @Override
  @Property(MessageContext.PATH_INFO)
  public String getPathInfo() {
    return m_request.getPathInfo();
  }

  @Override
  public String getBaseAddress() {
    return JaxWsHelper.getBaseAddress(m_request, true);
  }

  @Override
  @Property(MessageContext.QUERY_STRING)
  public String getQueryString() {
    return m_request.getQueryString();
  }

  @Override
  @Property(MessageContext.HTTP_REQUEST_METHOD)
  public String getRequestMethod() {
    return m_request.getMethod();
  }

  @Override
  public String getRequestHeader(String headerName) {
    return m_request.getHeader(headerName);
  }

  @Override
  @SuppressWarnings("deprecation")
  @Property({MessageContext.HTTP_REQUEST_HEADERS, Packet.INBOUND_TRANSPORT_HEADERS})
  public Map<String, List<String>> getRequestHeaders() {
    Map<String, List<String>> headerMap = new HashMap<String, List<String>>();

    Enumeration headerNames = m_request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = (String) headerNames.nextElement();
      String value = m_request.getHeader(name);

      if (headerMap.get(name) == null) {
        headerMap.put(name, new ArrayList<String>());
      }
      headerMap.get(name).add(value);
    }

    return headerMap;
  }

  @Override
  public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
    m_responseHeaderMap.clear();
    m_response.reset();
    m_response.setStatus(m_status);

    // update headers of @{link HttpServletResponse}
    if (responseHeaders == null || responseHeaders.size() == 0) {
      return;
    }
    for (Entry<String, List<String>> entry : responseHeaders.entrySet()) {
      String name = entry.getKey();
      List<String> values = entry.getValue();

      // According to JavaDoc, ignore 'Content-Type' and 'Content-Length'
      if (name.equalsIgnoreCase("Content-Type") || name.equalsIgnoreCase("Content-Length")) {
        continue;
      }
      for (String value : values) {
        m_response.addHeader(name, value);
      }
    }
  }

  @Override
  @Property({MessageContext.HTTP_RESPONSE_HEADERS, Packet.OUTBOUND_TRANSPORT_HEADERS})
  public Map<String, List<String>> getResponseHeaders() {
    return JaxWsHelper.cloneHeaderMap(m_responseHeaderMap); // clone according do JavaDoc
  }

  @Override
  public void setContentTypeResponseHeader(String value) {
    m_response.setContentType(value);
  }

  @Override
  public void setContentLengthResponseHeader(int value) {
    m_response.setContentLength(value);
  }

  @Override
  @Property(MessageContext.SERVLET_CONTEXT)
  public ServletContext getContext() {
    return m_context;
  }

  @Override
  public WebServiceContextDelegate getWebServiceContextDelegate() {
    return m_contextDelegate;
  }

  @Override
  protected PropertyMap getPropertyMap() {
    return PROP_MODEL;
  }

  protected WebServiceContextDelegate createContextDelegate(HttpAdapter adapter, HttpServletRequest request) {
    return new ServletContextDelegate(adapter, request);
  }

  @Override
  @SuppressWarnings("deprecation")
  @Deprecated
  public Set<String> getRequestHeaderNames() {
    Set<String> headerNameSet = new HashSet<String>();
    for (Enumeration<String> headerNames = m_request.getHeaderNames(); headerNames.hasMoreElements();) {
      headerNameSet.add(headerNames.nextElement());
    }
    return headerNameSet;
  }

  @Override
  public List<String> getRequestHeaderValues(String arg0) {
    List<String> headerList = new ArrayList<String>();
    for (Enumeration<String> headers = m_request.getHeaders(arg0); headers.hasMoreElements();) {
      headerList.add(headers.nextElement());
    }
    return headerList;
  }

  @Override
  public String getRequestScheme() {
    return m_request.getScheme();
  }

  @Override
  public String getRequestURI() {
    return m_request.getRequestURI();
  }

  @Override
  public String getServerName() {
    return m_request.getServerName();
  }

  @Override
  public int getServerPort() {
    return m_request.getServerPort();
  }

  @Override
  public void setResponseHeader(String name, List<String> values) {
    // According to JavaDoc, ignore 'Content-Type' and 'Content-Length'
    if (name.equalsIgnoreCase("Content-Type") || name.equalsIgnoreCase("Content-Length")) {
      return;
    }

    for (String value : values) {
      m_response.addHeader(name, value);
    }
  }
}
