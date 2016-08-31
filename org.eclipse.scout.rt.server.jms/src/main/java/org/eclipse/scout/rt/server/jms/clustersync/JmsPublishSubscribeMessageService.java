/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jms.clustersync;

import java.util.Hashtable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.server.jms.AbstractSimpleJmsService;
import org.eclipse.scout.rt.server.jms.clustersync.JmsPublishSubscribeMessageProperties.JndiConnectionFactory;
import org.eclipse.scout.rt.server.jms.clustersync.JmsPublishSubscribeMessageProperties.JndiInitialContextFactory;
import org.eclipse.scout.rt.server.jms.clustersync.JmsPublishSubscribeMessageProperties.JndiPassword;
import org.eclipse.scout.rt.server.jms.clustersync.JmsPublishSubscribeMessageProperties.JndiProviderUrl;
import org.eclipse.scout.rt.server.jms.clustersync.JmsPublishSubscribeMessageProperties.JndiUsername;
import org.eclipse.scout.rt.server.jms.clustersync.JmsPublishSubscribeMessageProperties.PublishSubscribeTopic;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.IPublishSubscribeMessageListener;
import org.eclipse.scout.rt.server.services.common.clustersync.IPublishSubscribeMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This publish subscribe message service uses a JMS topic to deliver messages.
 * <p>
 * This services must not be registered with a session based service factory.
 *
 * @deprecated will be removed in 7.1.x; use {@link MOM} instead.
 */
@Bean
@ApplicationScoped
@Deprecated
@SuppressWarnings("deprecation")
public class JmsPublishSubscribeMessageService extends AbstractSimpleJmsService<IClusterNotificationMessage> implements IPublishSubscribeMessageService {
  private static Logger LOG = LoggerFactory.getLogger(JmsPublishSubscribeMessageService.class);

  private volatile IPublishSubscribeMessageListener m_listener;
  private ConnectionFactory m_connectionFactory;
  private Destination m_destination;

  @PostConstruct
  protected void initializeService() {
    try {
      Hashtable<Object, Object> env = new Hashtable<>();
      setupInitialContextEnvironment(env);
      InitialContext context = new InitialContext(env.isEmpty() ? null : env);

      m_connectionFactory = (ConnectionFactory) context.lookup(CONFIG.getPropertyValue(JndiConnectionFactory.class));
      m_destination = (Destination) context.lookup(CONFIG.getPropertyValue(PublishSubscribeTopic.class));
    }
    catch (NamingException e) {
      throw new PlatformException("cannot setup jms", e);
    }
  }

  /**
   * Sets-up the environment used for creating an {@link InitialContext}.
   */
  protected void setupInitialContextEnvironment(Hashtable<Object, Object> env) {
    addConfigPropertyToEnvironment(env, JndiInitialContextFactory.class, Context.INITIAL_CONTEXT_FACTORY);
    addConfigPropertyToEnvironment(env, JndiProviderUrl.class, Context.PROVIDER_URL);
    addConfigPropertyToEnvironment(env, JndiUsername.class, Context.SECURITY_PRINCIPAL);
    addConfigPropertyToEnvironment(env, JndiPassword.class, Context.SECURITY_CREDENTIALS);
  }

  @Override
  protected ConnectionFactory getConnectionFactory() {
    return m_connectionFactory;
  }

  @Override
  protected Destination getDestination() {
    return m_destination;
  }

  @Override
  public void setListener(IPublishSubscribeMessageListener listener) {
    m_listener = listener;
  }

  @Override
  public IPublishSubscribeMessageListener getListener() {
    return m_listener;
  }

  @Override
  public void subscribe() {
    if (!isEnabled()) {
      throw new ProcessingException("Cluster synchronization is not enabled: missing connectionFactory or topic configuration properties.");
    }
    setupConnection();
    startMessageConsumer();
    LOG.info("Cluster synchronization is enabled and JMS message listener is registered");
  }

  @Override
  public void unsubsribe() {
    if (!isEnabled()) {
      LOG.info("Cluster synchronization is not enabled");
      return;
    }
    try {
      stopMessageConsumer();
    }
    finally {
      closeConnection();
    }
    LOG.info("Cluster synchronization is stopped");
  }

  @Override
  public void publishNotifications(List<IClusterNotificationMessage> notificationMessages) {
    send(notificationMessages);
  }

  @Override
  protected void execOnMessage(IClusterNotificationMessage message, Session session) {
    IPublishSubscribeMessageListener listener = getListener();
    if (listener != null) {
      listener.onMessage(message);
    }
  }
}
