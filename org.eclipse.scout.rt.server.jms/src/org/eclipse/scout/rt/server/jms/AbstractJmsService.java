/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jms;

import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.eclipse.scout.service.SERVICES;

/**
 * Base class for a JNDI configured scout service for JMS. See {@link AbstractJndiService} for configuring example.
 * <p>
 * This class is thread save.
 * <p>
 * Some notes about JMS objects:
 * <ul>
 * <li>A {@link Connection} is a is a relatively heavyweight object. It supports concurrent use. Therefore it should be
 * shared.
 * <li> {@link Session}, {@link MessageConsumer} and {@link MessageProducer} are lightweight objects and should not be
 * used by different threads.
 * <li>To receive new messages one should not use the method
 * {@link MessageConsumer#setMessageListener(javax.jms.MessageListener)} as this is a J2EE container private method.
 * Instead one should use the synchronous methods like {@link MessageConsumer#receive(long)}
 * </ul>
 * 
 * @param <T>
 *          the type of message that should be sent and received
 */
public abstract class AbstractJmsService<T> extends AbstractJndiService {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJmsService.class);

  private String m_connectionFactory;
  private String m_destination;

  private Connection m_connection;

  @ConfigProperty(ConfigProperty.STRING)
  @Order(10)
  protected String getConfiguredConnectionFactory() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(20)
  protected String getConfiguredDestination() {
    return null;
  }

  public String getConnectionFactory() {
    return m_connectionFactory;
  }

  public void setConnectionFactory(String connectionFactory) {
    m_connectionFactory = connectionFactory;
  }

  public String getDestination() {
    return m_destination;
  }

  public void setDestination(String destination) {
    m_destination = destination;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setConnectionFactory(getConfiguredConnectionFactory());
    setDestination(getConfiguredDestination());
  }

  protected ConnectionFactory lookupConnectionFactory() throws ProcessingException {
    return lookup(getConnectionFactory(), ConnectionFactory.class);
  }

  protected Destination lookupDestination() throws ProcessingException {
    return lookup(getDestination(), Destination.class);
  }

  protected boolean isEnabled() {
    return getConnectionFactory() != null && getDestination() != null;
  }

  @SuppressWarnings("unchecked")
  protected Class<T> getMessageType() {
    return TypeCastUtility.getGenericsParameterClass(this.getClass(), AbstractJmsService.class);
  }

  protected JmsMessageSerializer<T> createMessageSerializer() {
    return new JmsMessageSerializer<T>(getMessageType());
  }

  protected synchronized Connection getConnection() {
    return m_connection;
  }

  protected synchronized void setupConnection() throws ProcessingException {
    closeConnection();
    ConnectionFactory connectionFactory = lookupConnectionFactory();
    Connection con;
    try {
      con = connectionFactory.createConnection();
    }
    catch (JMSException e) {
      throw new ProcessingException("Failed creating JMS connection", e);
    }
    String clientId = null;
    try {
      // try to set clientId; might fail, ignore if happens
      clientId = createClientId();
      con.setClientID(clientId);
    }
    catch (Exception e) {
      LOG.info("WARNING - Failed to set clientID '{0}' for consumer connection, possibly because of running in application container: {1}", clientId, e.getMessage());
      LOG.trace("Full Exception:", e);
    }
    m_connection = con;
  }

  protected synchronized void closeConnection() throws ProcessingException {
    Connection connection = m_connection;
    if (connection != null) {
      m_connection = null;
      try {
        connection.close();
      }
      catch (JMSException e) {
        throw new ProcessingException("Failed closing JMS connection", e);
      }
    }
  }

  protected String createClientId() {
    String serverVersion = null;
    try {
      serverVersion = Platform.getProduct().getDefiningBundle().getVersion().toString();
    }
    catch (Exception e) {
      LOG.warn("Cannot determine server version", e);
    }
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName()).append(" ");
    if (serverVersion != null) {
      sb.append(serverVersion).append(" ");
    }
    sb.append("nodeId=").append(SERVICES.getService(IClusterSynchronizationService.class).getNodeId()).append(" ");
    sb.append("registered at ");
    sb.append(DateUtility.format(new Date(), "yyyy-MM-dd HH:mm:ss,SSS"));
    return sb.toString();
  }
}
