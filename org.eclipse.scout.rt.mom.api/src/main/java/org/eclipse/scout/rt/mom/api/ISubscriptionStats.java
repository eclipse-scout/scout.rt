/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mom.api;

import java.util.Date;

/**
 * Allows to access some statistics of the subscription
 */
public interface ISubscriptionStats {

  /**
   * @return the number of messages received, including null messages
   */
  long receivedMessages();

  /**
   * @return the number of non-null messages received
   */
  long receivedNonNullMessages();

  /**
   * @return the number of message errors occurred
   */
  long receivedErrors();

  /**
   * @return timestamp of last received message, not including errors
   */
  Date lastMessageReceivedTimestamp();
}
