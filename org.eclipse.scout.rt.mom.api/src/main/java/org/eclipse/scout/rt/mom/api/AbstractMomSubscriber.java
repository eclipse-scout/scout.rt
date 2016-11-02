package org.eclipse.scout.rt.mom.api;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Keeps a list of {@link ISubscription}s that are registered during {@link #subscribe()}. All registered subscriptions
 * are disposed when {@link #dispose()} is called.
 * <p>
 * This class is not thread-safe.
 *
 * @since 6.1
 */
@Bean
public abstract class AbstractMomSubscriber {

  private final List<ISubscription> m_subscriptions = new ArrayList<>();

  public abstract void subscribe();

  /**
   * Remembers the given <code>subscription</code>, so it can later be disposed by calling {@link #dispose()}.
   */
  protected void registerSubscription(ISubscription subscription) {
    m_subscriptions.add(subscription);
  }

  public void dispose() {
    for (ISubscription subscription : m_subscriptions) {
      subscription.dispose();
    }
  }
}
