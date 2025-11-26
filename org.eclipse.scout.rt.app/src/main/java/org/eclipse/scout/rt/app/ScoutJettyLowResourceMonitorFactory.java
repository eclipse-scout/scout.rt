/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.app;

import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.Server;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Factory to create a {@link LowResourceMonitor} to monitor the Jetty server for low resources.
 */
@ApplicationScoped
public class ScoutJettyLowResourceMonitorFactory {

  public LowResourceMonitor createLowResourceMonitor(Server server) {
    LowResourceMonitor lowResourceMonitor = new LowResourceMonitor(server);
    initLowResourceMonitor(lowResourceMonitor);
    return lowResourceMonitor;
  }

  protected void initLowResourceMonitor(LowResourceMonitor lowResourceMonitor) {
    lowResourceMonitor.setMonitorThreads(true); // monitor if the thread pool of the server is low on threads
  }
}
