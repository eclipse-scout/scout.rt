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
package org.eclipse.scout.rt.server.jaxws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.security.auth.Subject;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * Utility methods to work with JAX-WS {@link MessageContext}.
 *
 * @since 5.1
 */
public final class MessageContexts {

  public static final String PROP_RUN_CONTEXT = MessageContexts.class.getName() + ".RunContext";

  private MessageContexts() {
  }

  /**
   * Returns the current SOAP message from the given {@link SOAPMessageContext}.
   */
  public static String getSoapMessage(final SOAPMessageContext context) throws SOAPException, IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      context.getMessage().writeTo(bos);
      return bos.toString("UTF-8");
    }
  }

  /**
   * @return <code>true</code> if the current message is an outbound-message, meaning that the request was already
   *         processed.
   */
  public static boolean isOutboundMessage(final MessageContext context) {
    return TypeCastUtility.castValue(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY), boolean.class);
  }

  /**
   * @return <code>true</code> if the current message is an inbound-message, meaning that the request is not processed
   *         yet.
   */
  public static boolean isInboundMessage(final MessageContext context) {
    return !MessageContexts.isOutboundMessage(context);
  }

  /**
   * Registers the given {@link RunContext} within {@link MessageContext} with scope 'application'.
   */
  public static void setRunContext(final MessageContext context, final RunContext runContext) {
    if (runContext == null) {
      context.remove(PROP_RUN_CONTEXT);
    }
    else {
      context.put(PROP_RUN_CONTEXT, runContext);
      context.setScope(PROP_RUN_CONTEXT, Scope.APPLICATION); // APPLICATION-SCOPE to be accessible in port type.
    }
  }

  /**
   * Returns the {@link RunContext} contained in {@link MessageContext}, or <code>null</code> if not found.
   */
  public static RunContext getRunContext(final MessageContext messageContext) {
    final Object runContext = messageContext.get(MessageContexts.PROP_RUN_CONTEXT);
    if (runContext instanceof RunContext) {
      return (RunContext) runContext;
    }
    else if (runContext == null) {
      return null;
    }
    else {
      throw new WebServiceException(String.format("Invalid 'RunContext' on '%s' [actual=%s, expected=%s]", MessageContext.class.getName(), runContext.getClass().getName(), RunContext.class.getName()));
    }
  }

  /**
   * Returns the subject as declared in {@link RunContext} contained in {@link MessageContext}, or the default subject
   * if not found.
   */
  public static Subject getSubject(final MessageContext messageContext, final Subject defaultSubject) {
    final RunContext runContext = MessageContexts.getRunContext(messageContext);
    if (runContext != null && runContext.getSubject() != null) {
      return runContext.getSubject();
    }
    else {
      return defaultSubject;
    }
  }
}
