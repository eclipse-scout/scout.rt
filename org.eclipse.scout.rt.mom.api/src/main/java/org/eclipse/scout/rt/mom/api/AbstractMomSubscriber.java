/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Keeps a list of {@link ISubscription}s that are registered during {@link #subscribe()}. All registered subscriptions
 * are disposed when {@link #dispose()} is called.
 *
 * @since 6.1
 */
@ApplicationScoped
public abstract class AbstractMomSubscriber {

  private final List<ISubscription> m_subscriptions = Collections.synchronizedList(new ArrayList<>());

  public abstract void subscribe();

  /**
   * Remembers the given <code>subscription</code>, so it can later be disposed by calling {@link #dispose()}.
   */
  protected void registerSubscription(ISubscription subscription) {
    m_subscriptions.add(subscription);
  }

  public void dispose() {
    synchronized (m_subscriptions) {
      for (ISubscription subscription : m_subscriptions) {
        subscription.dispose();
      }
    }
  }
}
