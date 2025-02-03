/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
