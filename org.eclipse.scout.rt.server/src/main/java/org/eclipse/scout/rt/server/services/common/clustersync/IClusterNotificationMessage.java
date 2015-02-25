/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.clustersync;

import java.io.Serializable;

/**
 * Message sent between cluster nodes
 */
public interface IClusterNotificationMessage extends Serializable {

  IClusterNotificationMessageProperties getProperties();

  IClusterNotification getNotification();

  /**
   * Merge notification with other notifications of the same type.<br>
   * Same type means notification1.getClass()==notification2.getClass()
   *
   * @return true if this notification was coalesced and therefore is consumed. This notification will be removed from
   *         the queue.
   */
  boolean coalesce(IClusterNotificationMessage newMessage);

}
