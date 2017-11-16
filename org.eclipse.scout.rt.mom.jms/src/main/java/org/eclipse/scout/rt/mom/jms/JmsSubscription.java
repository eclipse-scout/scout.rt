package org.eclipse.scout.rt.mom.jms;

import javax.jms.MessageConsumer;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Represents a {@link MessageConsumer} in the JMS messaging standard.
 *
 * @see IMom
 * @since 6.1
 */
public class JmsSubscription implements ISubscription {

  protected final IDestination<?> m_destination;
  protected final SubscribeInput m_subscribeInput;
  protected final IJmsSessionProvider m_sessionProvider;
  protected final IFuture<Void> m_jobMonitor;

  public JmsSubscription(IDestination<?> destination, SubscribeInput subscribeInput, IJmsSessionProvider sessionProvider, IFuture<Void> jobMonitor) {
    m_destination = destination;
    m_subscribeInput = subscribeInput;
    m_sessionProvider = sessionProvider;
    m_jobMonitor = jobMonitor;
  }

  @Override
  public IDestination<?> getDestination() {
    return m_destination;
  }

  @Override
  public void dispose() {
    m_sessionProvider.close();
    if (SubscribeInput.ACKNOWLEDGE_AUTO_SINGLE_THREADED == m_subscribeInput.getAcknowledgementMode()) {
      // Close did not throw an exception
      // In case of single threaded subscription we wait for the job to finish
      // This allows API clients to wait for any ongoing message processing
      m_jobMonitor.awaitDone();
    }
  }
}
