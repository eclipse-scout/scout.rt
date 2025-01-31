/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.clientnotification;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

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
   * <code>false</code> otherwise.
   */
  boolean isNotifyAllSessions();

  /**
   * @return <code>true</code>, if a notification should be dispatched once per UI server node, <code>false</code>
   * otherwise.
   */
  boolean isNotifyAllNodes();

  default String prettyPrint() {
    if (isNotifyAllNodes()) {
      return "all nodes";
    }
    if (isNotifyAllSessions()) {
      return "all sessions";
    }
    if (CollectionUtility.hasElements(getUserIds())) {
      return getUserIds().stream().sorted().collect(Collectors.joining(", ", "users [", "]"));
    }
    if (CollectionUtility.hasElements(getSessionIds())) {
      return getSessionIds().stream().sorted().collect(Collectors.joining(", ", "sessions [", "]"));
    }
    return "unknown";
  }
}
