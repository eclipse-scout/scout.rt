/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.clientnotification;

import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry.ClientNotificationRegistryPlatformListener;

/**
 * Testing implementation of {@link ClientNotificationRegistry.ClientNotificationRegistryPlatformListener} suppressing start and stop of cleanup job.
 */
@Replace
public class TestingClientNotificationRegistryPlatformListener extends ClientNotificationRegistryPlatformListener {
  @Override
  public void stateChanged(PlatformEvent event) {
    // NOP do not init/shutdown ClientNotificationRegistry
  }
}
