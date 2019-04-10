/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms.internal;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.jms.IJmsSessionProvider;

/**
 * Support for the new additional method {@link IJmsSessionProvider2#receive(SubscribeInput)}. This static helper is
 * used in order not to break the existing API of {@link IJmsSessionProvider}.
 * <p>
 * TODO [10.0] imo
 * <p>
 * Future code will move this static method to {@link IJmsSessionProvider2} and merge {@link IJmsSessionProvider2}
 * with {@link IJmsSessionProvider}. However this will change the API.
 *
 * @since 6.1
 */
public final class JmsSessionProviderMigration {
  private JmsSessionProviderMigration() {
  }

  public static Message receive(IJmsSessionProvider sessionProvider, SubscribeInput input, long receiveTimeoutMillis) throws JMSException {
    if (sessionProvider instanceof IJmsSessionProvider2) {
      return ((IJmsSessionProvider2) sessionProvider).receive(input, receiveTimeoutMillis);
    }
    MessageConsumer consumer = sessionProvider.getConsumer(input);
    return receiveTimeoutMillis == 0L ? consumer.receive() : consumer.receive(receiveTimeoutMillis);
  }
}
