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
package org.eclipse.scout.rt.shared.clientnotification;

import java.io.Serializable;
import java.util.Set;

/**
 * Address of a client notification that can be used for dispatching.
 */
public interface IClientNotificationAddress extends Serializable {

  /**
   * @return the sessions for which this client notification should be dispatched.
   */
  Set<String> getSessionIds();

  /**
   * @return the userIds for which this client notification should be dispatched.
   */
  Set<String> getUserIds();

  /**
   * @return <code>true</code>, if a notification should be dispatched once for every available session,
   *         <code>false</code> otherwise.
   */
  boolean isNotifyAllSessions();

  /**
   * @return <code>true</code>, if a notification should be dispatched once per UI server node, <code>false</code>
   *         otherwise.
   */
  boolean isNotifyAllNodes();

}
