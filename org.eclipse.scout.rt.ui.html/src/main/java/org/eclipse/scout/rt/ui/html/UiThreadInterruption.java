/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
