/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.app;

import org.eclipse.scout.rt.jetty.IGracefulShutdownListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.ui.html.UiPollerRegistry;

/**
 * Releases all waiting UI poll requests when the graceful shutdown starts. Otherwise, these long polling requests would
 * delay the graceful shutdown until the stop timeout has elapsed.
 */
public class UiPollerGracefulShutdownListener implements IGracefulShutdownListener {

  @Override
  public void onGracefulShutdown() {
    BEANS.get(UiPollerRegistry.class).shutdown();
  }
}
