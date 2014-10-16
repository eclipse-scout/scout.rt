package org.eclipse.scout.rt.server.jms.clustersync;

import java.util.List;

import javax.jms.Session;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.jms.AbstractSimpleJmsService;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.IPublishSubscribeMessageListener;
import org.eclipse.scout.rt.server.services.common.clustersync.IPublishSubscribeMessageService;

/**
 * This publish subscribe message service uses a JMS topic to deliver messages.
 * <p>
 * This services must not be registered with a session based service factory.
 */
@SuppressWarnings("restriction")
public class JmsPublishSubscribeMessageService extends AbstractSimpleJmsService<IClusterNotificationMessage> implements IPublishSubscribeMessageService {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(JmsPublishSubscribeMessageService.class);

  private volatile IPublishSubscribeMessageListener m_listener;

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
      LOG.info("Cluster synchronization is not enabled: missing connectionFactory or topic configuration properties.");
      return;
    }
    setupConnection();
    startMessageConsumerJob();
    LOG.info("Cluster synchronization is enabled and JMS message listener is registered");
  }

  @Override
  public void unsubsribe() throws ProcessingException {
    if (!isEnabled()) {
      LOG.trace("Cluster synchronization is not enabled");
      return;
    }
    try {
      stopMessageConsumerJob();
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
  protected void execOnMessage(IClusterNotificationMessage message, Session session, IProgressMonitor monitor) {
    IPublishSubscribeMessageListener listener = getListener();
    if (listener != null) {
      listener.onMessage(message);
    }
  }
}
