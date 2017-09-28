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
package org.eclipse.scout.rt.mom.jms;

import javax.jms.JMSException;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.SubscribeInput;

/**
 * Strategy to subscribe for messages.
 *
 * @since 6.1
 */
public interface ISubscriptionStrategy {

  /**
   * Subscribes for messages sent to the given destination.
   *
   * @param destination
   *          specifies the target of the subscription; must not be <code>null</code>.
   * @param listener
   *          specifies the listener to receive messages; must not be <code>null</code>.
   * @param input
   *          specifies how to subscribe for messages.; must not be <code>null</code>.
   */
  <DTO> ISubscription subscribe(IDestination<DTO> destination, IMessageListener<DTO> listener, SubscribeInput input) throws JMSException;
}
