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
package org.eclipse.scout.jaxws216.handler;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Handler to log SOAP messages.
 */
public class LogHandler implements SOAPHandler<SOAPMessageContext> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LogHandler.class);

  private boolean m_sysout;

  public LogHandler() {
    this(false);
  }

  /**
   * @param sysout
   *          true to print log output to standard output (console)
   */
  public LogHandler(boolean sysout) {
    m_sysout = sysout;
  }

  @Override
  public final boolean handleMessage(SOAPMessageContext context) {
    handleLogMessageInternal(context);
    return true;
  }

  @Override
  public final boolean handleFault(SOAPMessageContext context) {
    handleLogMessageInternal(context);
    return true;
  }

  @Override
  public final Set<QName> getHeaders() {
    return null;
  }

  @Override
  public final void close(MessageContext messageContext) {
  }

  private void handleLogMessageInternal(SOAPMessageContext context) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      context.getMessage().writeTo(bos);
      bos.close();
      boolean outbound = TypeCastUtility.castValue(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY), boolean.class);
      handleLogMessage(outbound ? DirectionType.Out : DirectionType.In, bos.toString("UTF-8"), context);
    }
    catch (Exception e) {
      LOG.error("Error occured while logging SOAP message.", e);
    }
  }

  protected void handleLogMessage(DirectionType directionType, String soapMessage, SOAPMessageContext context) {
    String logMessage = "WS SOAP [service=" + context.get(SOAPMessageContext.WSDL_SERVICE) + ", port=" + context.get(SOAPMessageContext.WSDL_PORT) + ", operation=" + context.get(SOAPMessageContext.WSDL_OPERATION) + ", direction=" + directionType + ", message=" + soapMessage + "]";
    if (isSysout()) {
      System.out.println(logMessage);
    }
    else {
      LOG.debug(logMessage);
    }
  }

  public boolean isSysout() {
    return m_sysout;
  }

  public void setSysout(boolean sysout) {
    m_sysout = sysout;
  }

  public static enum DirectionType {
    In, Out;
  }
}
