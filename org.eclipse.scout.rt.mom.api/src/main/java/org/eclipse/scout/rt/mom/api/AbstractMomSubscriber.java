/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
