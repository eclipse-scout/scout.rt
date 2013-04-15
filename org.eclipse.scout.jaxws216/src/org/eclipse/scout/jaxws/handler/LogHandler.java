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
package org.eclipse.scout.jaxws.handler;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Handler to log SOAP messages.
 */
public class LogHandler implements SOAPHandler<SOAPMessageContext> {

  protected static final IScoutLogger LOG = ScoutLogManager.getLogger(LogHandler.class);

  private int m_logLevel;

  public LogHandler() {
    initConfig();
  }

  protected void initConfig() {
    setLogLevel(getConfiguredLogLevel());
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
    if (!LOG.isLoggable(getLogLevel())) {
      return;
    }
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

  /**
   * To be overwritten to implement specific log behavior, e.g. to write the log entry into a database.
   * 
   * @param directionType
   *          indicates the direction of the message flow; {@link DirectionType#In} for inbound and
   *          {@link DirectionType#Out} for outbound traffic, respectively.
   * @param soapMessage
   *          the SOAP message formatted as {@link String}.
   * @param context
   *          {@link SOAPMessageContext}
   */
  protected void handleLogMessage(DirectionType directionType, String soapMessage, SOAPMessageContext context) {
    String logMessage = "WS SOAP [service=" + context.get(SOAPMessageContext.WSDL_SERVICE) + ", port=" + context.get(SOAPMessageContext.WSDL_PORT) + ", operation=" + context.get(SOAPMessageContext.WSDL_OPERATION) + ", direction=" + directionType + ", message=" + soapMessage + "]";

    switch (getLogLevel()) {
      case IScoutLogger.LEVEL_WARN:
        LOG.warn(logMessage);
        break;
      case IScoutLogger.LEVEL_DEBUG:
        LOG.debug(logMessage);
        break;
      case IScoutLogger.LEVEL_TRACE:
        LOG.trace(logMessage);
        break;
      default:
        LOG.info(logMessage);
        break;
    }
  }

  public int getLogLevel() {
    return m_logLevel;
  }

  public void setLogLevel(int logLevel) {
    m_logLevel = logLevel;
  }

  /**
   * To configure the log level the SOAP-message is logged with.
   * 
   * @return the log level
   * @see IScoutLogger for the available log levels.
   */
  @Order(10.0)
  @ConfigProperty(ConfigProperty.INTEGER)
  protected int getConfiguredLogLevel() {
    return IScoutLogger.LEVEL_INFO;
  }

  /**
   * The direction of the message flow.
   */
  public static enum DirectionType {
    In, Out;
  }
}
