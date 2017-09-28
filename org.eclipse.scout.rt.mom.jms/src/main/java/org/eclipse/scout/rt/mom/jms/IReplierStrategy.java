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

import org.eclipse.scout.rt.mom.api.IBiDestination;
import org.eclipse.scout.rt.mom.api.IRequestListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.SubscribeInput;

/**
 * Strategy to reply to requests in 'request-reply' messaging.
 *
 * @since 6.1
 */
public interface IReplierStrategy {

  /**
   * Subscribes for requests sent to the given destination.
   *
   * @param destination
   *          specifies the target of the subscription; must not be <code>null</code>.
   * @param listener
   *          specifies the listener to reply to requests; must not be <code>null</code>.
   * @param input
   *          specifies how to subscribe for messages.; must not be <code>null</code>.
   */
  <REQUEST, REPLY> ISubscription subscribe(IBiDestination<REQUEST, REPLY> destination, IRequestListener<REQUEST, REPLY> listener, SubscribeInput input) throws JMSException;
}
