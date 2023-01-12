/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility used to handle unexpected interruptions on poolead threads.
 *
 * @since 9.0
 */
@ApplicationScoped
public class UiThreadInterruption {
  private static final Logger LOG = LoggerFactory.getLogger(UiThreadInterruption.class);

  /**
   * If a thread pool does not reset thread interruptions, this may lead to unexpected misbehaviour of processing. This
   * utility method detects, clears and logs such unexpected interruptions.
   */
  public void detectAndClear(Object caller, String path) {
    if (Thread.currentThread().isInterrupted()) {
      Thread.interrupted();
      boolean success = !Thread.currentThread().isInterrupted();
      LOG.warn("DETECTED_THREAD_INTERRUPTION in {}#{} '{}', clearing interrupt status {}",
          caller.getClass().getName(),
          Integer.toHexString(caller.hashCode()),
          path,
          success ? "successful" : "failed",
          new Exception("Calling trace"));
    }
  }
}
