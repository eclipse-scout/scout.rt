/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms.internal;

import org.eclipse.scout.rt.mom.jms.JmsSubscription;

/**
 * Helper used in {@link JmsSubscription#awaitStarted(int, java.util.concurrent.TimeUnit)} and unit testing
 *
 * @since 6.1
 */
public interface ISubscriptionStats extends org.eclipse.scout.rt.mom.api.ISubscriptionStats {

  /**
   * @return true if the subscription is just before or inside a call to MessageConsumer#receive
   */
  boolean invokingReceive();
}
