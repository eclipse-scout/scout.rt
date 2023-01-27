/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.util.IDisposable;

/**
 * Represents a subscription to a destination.
 * <p>
 * Use this object to unsubscribe from the destination.
 *
 * @see IMom
 * @since 6.1
 */
public interface ISubscription extends IDisposable {

  /**
   * @return the destination this subscription belongs to
   */
  IDestination<?> getDestination();

  /**
   * @return the listener called upon message received or null if {@link #getRequestListener()} is set
   */
  IMessageListener<?> getMessageListener();

  /**
   * @return the listener called upon message received or null if {@link #getMessageListener()} is set
   */
  IRequestListener<?, ?> getRequestListener();

  /**
   * @return input used during subscription
   */
  SubscribeInput getSubscribeInput();

  /**
   * {@inheritDoc}
   * <p>
   * In case of single threaded subscription, the call to this method blocks until any ongoing processing of this
   * subscription is finished.
   */
  @Override
  void dispose();

  /**
   * @return true if subscription is disposing or was disposed
   */
  boolean isDisposed();

  /**
   * Returns an object with some stats of this subscription since the session was started. May be null when no session
   * or connection was opened or in case of null transport.
   *
   * @return stats of this subscription since the session was started or null
   */
  ISubscriptionStats getStats();

}
