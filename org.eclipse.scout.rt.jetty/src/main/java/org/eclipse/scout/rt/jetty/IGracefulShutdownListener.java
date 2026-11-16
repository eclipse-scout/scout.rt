/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jetty;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Listener notified when the Jetty server of the Scout application ({@link org.eclipse.scout.rt.app.Application})
 * initiates its graceful shutdown, i.e. before the server waits for active requests to complete.
 * <p>
 * Implementations should release resources that keep requests active for a long time (e.g. long polling requests),
 * otherwise the graceful shutdown is delayed until the stop timeout has elapsed.
 */
@ApplicationScoped
public interface IGracefulShutdownListener {

  /**
   * Called when the graceful shutdown is initiated. Must not block.
   */
  void onGracefulShutdown();
}
