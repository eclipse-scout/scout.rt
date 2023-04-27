/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.io.InterruptedIOException;
import java.net.SocketException;

import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public class ConnectionErrorDetector {

  public boolean isConnectionError(Throwable e) {
    Throwable cause = e;
    Throwable previousCause = null;
    while (cause != null && cause != previousCause) { // second check avoids endless loops
      String simpleName = cause.getClass().getSimpleName();
      String message = cause.getMessage();
      if ((cause instanceof SocketException
          || "EofException".equalsIgnoreCase(simpleName)
          || "ClientAbortException".equalsIgnoreCase(simpleName)
          || cause instanceof InterruptedIOException
          || "IOException".equalsIgnoreCase(simpleName))
          && (StringUtility.containsStringIgnoreCase(message, "Connection reset by peer")
           || StringUtility.containsStringIgnoreCase(message, "Broken pipe"))) {
        return true;
      }
      // set previous cause
      previousCause = cause;
      // set next cause
      cause = cause.getCause();
    }
    return false;
  }
}
