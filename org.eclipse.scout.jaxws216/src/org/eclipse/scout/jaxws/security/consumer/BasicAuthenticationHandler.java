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
package org.eclipse.scout.jaxws.security.consumer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.annotation.ScoutTransaction;
import org.eclipse.scout.jaxws.service.AbstractWebServiceClient;

/**
 * <p>
 * Handler to include user's credentials in webservice requests by using Basic Access Authentication. This requires
 * requests to provide a valid user name and password to access content. User's credentials are transported in HTTP
 * headers. Basic authentication also works across firewalls and proxy servers.
 * </p>
 * <p>
 * User's credentials are configured in config.ini by specifying the properties <code>#username</code> and
 * <code>#password</code> of the respective {@link AbstractWebServiceClient}.
 * </p>
 * <p>
 * However, the disadvantage of Basic authentication is that it transmits unencrypted base64-encoded passwords across
 * the network. Therefore, you only should use this authentication when you know that the connection between the client
 * and the server is secure. The connection should be established either over a dedicated line or by using Secure
 * Sockets Layer (SSL) encryption and Transport Layer Security (TLS).
 * </p>
 */
@ScoutTransaction
public class BasicAuthenticationHandler implements IAuthenticationHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BasicAuthenticationHandler.class);

  protected String m_username;
  protected String m_password;

  public BasicAuthenticationHandler() {
  }

  @Override
  public final boolean handleMessage(SOAPMessageContext context) {
    boolean outbound = TypeCastUtility.castValue(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY), boolean.class);
    if (!outbound) {
      return true; // only outbound messages are of interest
    }

    if (m_username == null || m_password == null) {
      throw new WebServiceException("Invalid credentials configured.");
    }

    try {
      Map<String, List<String>> httpRequestHeaders = getHttpRequestHeaders(context);
      if (httpRequestHeaders == null) {
        httpRequestHeaders = new HashMap<String, List<String>>();
      }
      installAuthHeader(httpRequestHeaders);
      context.put(MessageContext.HTTP_REQUEST_HEADERS, httpRequestHeaders);
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to install Basic Authentication headers", e);
      return false;
    }
  }

  protected void installAuthHeader(Map<String, List<String>> httpRequestHeaders) throws Exception {
    String credentials = StringUtility.join(":", m_username, m_password);
    String credentialsEncoded = Base64Utility.encode(credentials.getBytes());

    List<String> basicAuthToken = new LinkedList<String>();
    basicAuthToken.add("Basic " + credentialsEncoded);
    httpRequestHeaders.put("Authorization", basicAuthToken);
  }

  @Override
  public Set<QName> getHeaders() {
    return new HashSet<QName>();
  }

  @Override
  public void close(MessageContext context) {
  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    return false;
  }

  @SuppressWarnings("unchecked")
  private Map<String, List<String>> getHttpRequestHeaders(SOAPMessageContext context) {
    return (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);
  }

  @Override
  public void setPassword(String password) {
    m_password = password;
  }

  @Override
  public void setUsername(String username) {
    m_username = username;
  }
}
