/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.scout.jaxws.internal;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;

import com.sun.xml.internal.ws.client.BindingProviderProperties;

/**
 * Helper class for JAX-WS Scout RT.
 */
@SuppressWarnings("restriction")
public final class JaxWsHelper {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JaxWsHelper.class);

  private static final String PROP_SESSION_CONTEXT = "org.eclipse.scout.jaxws.internal.session";

  private JaxWsHelper() {
  }

  /**
   * To get the base address of the application
   *
   * @param request
   * @return
   */
  public static String getBaseAddress(HttpServletRequest request, boolean includeContextPath) {
    StringBuilder builder = new StringBuilder();
    builder.append(request.getScheme());
    builder.append("://");
    builder.append(request.getServerName());
    builder.append(':');
    builder.append(request.getServerPort());
    if (includeContextPath) {
      builder.append(request.getContextPath());
    }
    return builder.toString();
  }

  /**
   * Clones the given header map
   *
   * @param headers
   * @return
   */
  public static Map<String, List<String>> cloneHeaderMap(Map<String, List<String>> headers) {
    Map<String, List<String>> clone = new HashMap<String, List<String>>();

    for (Entry<String, List<String>> headerEntry : headers.entrySet()) {
      String name = headerEntry.getKey();
      List<String> values = headerEntry.getValue();

      clone.put(name, new ArrayList<String>(values));
    }
    return clone;
  }

  /**
   * @return <code>true</code> if the current message is an outbound-message, meaning that the request was already
   *         processed.
   */
  public static boolean isOutboundMessage(final MessageContext context) {
    return TypeCastUtility.castValue(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY), boolean.class);
  }

  /**
   * Invoke to reject a request because of an internal server error.
   */
  public static boolean reject500(final MessageContext context, final Exception exception) {
    context.put(MessageContext.HTTP_RESPONSE_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    LOG.error("Internal server error  (Authentication)", exception);

    if (exception instanceof WebServiceException) {
      throw (WebServiceException) exception;
    }
    else {
      throw new WebServiceException("Internal server error");
    }
  }

  /**
   * Invoke to reject a request because unauthorized.
   */
  public static boolean reject401(final MessageContext context) {
    context.put(MessageContext.HTTP_RESPONSE_CODE, HttpServletResponse.SC_UNAUTHORIZED);

    final boolean oneway = BooleanUtility.nvl((Boolean) context.get(BindingProviderProperties.ONE_WAY_OPERATION), false);
    if (oneway) {
      // do not just return false as in one-way communication, the chain is continued regardless of the status.
      throw new WebServiceException("Unauthorized");
    }
    return false;
  }

  /**
   * Returns the session contained in the calling {@link MessageContext}, or <code>null</code> if not available.
   */
  public static IServerSession getContextSession(MessageContext context) {
    Object serverSession = context.get(PROP_SESSION_CONTEXT);
    return (IServerSession) (serverSession instanceof IServerSession ? serverSession : null);
  }

  /**
   * To put the session onto the calling {@link MessageContext}.
   */
  public static void setContextSession(MessageContext context, IServerSession session) {
    if (session == null) {
      context.remove(PROP_SESSION_CONTEXT);
    }
    else {
      context.put(PROP_SESSION_CONTEXT, session);
      context.setScope(PROP_SESSION_CONTEXT, Scope.APPLICATION); // APPLICATION-SCOPE to be accessible in @{link ScoutInstanceResolver}
    }
  }

  /**
   * Checks, whether the current call runs in a 'doAs' context with one principal set at minimum.
   */
  public static boolean isAuthenticated() {
    Subject subject = Subject.getSubject(AccessController.getContext());
    return (subject != null && subject.getPrincipals().size() > 0);
  }

  /**
   * Asserts the given Subject not to be <code>null</code> nor read-only.
   */
  public static Subject assertValidAuthSubject(Subject subject) {
    if (subject == null) {
      throw new WebServiceException("Unexpected: Webservice request rejected beause not running on behalf of a Subject.");
    }
    else if (subject.isReadOnly()) {
      throw new WebServiceException(String.format("Unexpected: Subject must not be readonly [subject=%s]", subject));
    }
    return subject;
  }
}
