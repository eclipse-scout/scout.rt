/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.logger;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;

/**
 * This listener should be run as late as possible, therefore a high order has been chosen and the lister just acts on
 * stopped (not stopping) event. Depending on the logger no more logging is possible after it has been shutdown.
 */
@Order(5950)
public class LoggerShutdownPlatformListener implements IPlatformListener {

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == State.PlatformStopped) {
      ILoggerSupport loggerSupport = BEANS.get(ILoggerSupport.class);
      if (loggerSupport != null) {
        loggerSupport.shutdown();
      }
    }
  }
}
