package org.eclipse.scout.rt.server.jms.clustersync;

import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.PlatformException;
import org.eclipse.scout.rt.server.jms.AbstractSimpleJmsService;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.IPublishSubscribeMessageListener;
import org.eclipse.scout.rt.server.services.common.clustersync.IPublishSubscribeMessageService;

/**
 * This publish subscribe message service uses a JMS topic to deliver messages.
 * <p>
 * This services must not be registered with a session based service factory.
 */
@Bean
@ApplicationScoped
public class JmsPublishSubscribeMessageService extends AbstractSimpleJmsService<IClusterNotificationMessage> implements IPublishSubscribeMessageService {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(JmsPublishSubscribeMessageService.class);

  private volatile IPublishSubscribeMessageListener m_listener;
  private ConnectionFactory m_connectionFactory;
  private Destination m_destination;

  public JmsPublishSubscribeMessageService() {
  }

  @Override
  protected void initializeService() {
    try {
      m_connectionFactory = InitialContext.doLookup("BSIConnectionFactory");
      m_destination = InitialContext.doLookup("BSIJMSDistTopic");
    }
    catch (NamingException e) {
      throw new PlatformException("cannot setup jms", e);
    }
    super.initializeService();
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
  public void subscribe() throws ProcessingException {
    if (!isEnabled()) {
      throw new ProcessingException("Cluster synchronization is not enabled: missing connectionFactory or topic configuration properties.");
    }
    setupConnection();
    startMessageConsumer();
    LOG.info("Cluster synchronization is enabled and JMS message listener is registered");
  }

  @Override
  public void unsubsribe() throws ProcessingException {
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
  protected void execOnMessage(IClusterNotificationMessage message, Session session) throws ProcessingException {
    IPublishSubscribeMessageListener listener = getListener();
    if (listener != null) {
      listener.onMessage(message);
    }
  }
}
