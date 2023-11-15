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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;

@ApplicationScoped
public class ConnectionErrorDetector {

  public boolean isConnectionError(Throwable e) {
    return BEANS.get(DefaultExceptionTranslator.class).throwableCausesAccept(e, t -> isConnectionErrorThrowable(t) && isConnectionErrorMessage(t));
  }

  protected boolean isConnectionErrorThrowable(Throwable t) {
    if (t instanceof SocketException || t instanceof InterruptedIOException) {
      return true;
    }
    String simpleName = t.getClass().getSimpleName();
    return "EofException".equalsIgnoreCase(simpleName)
        || "ClientAbortException".equalsIgnoreCase(simpleName)
        || "IOException".equalsIgnoreCase(simpleName);
  }

  protected boolean isConnectionErrorMessage(Throwable t) {
    String message = t.getMessage();
    return StringUtility.containsStringIgnoreCase(message, "Connection reset")
        || StringUtility.containsStringIgnoreCase(message, "Closed")
        || StringUtility.containsStringIgnoreCase(message, "connection was aborted")
        || StringUtility.containsStringIgnoreCase(message, "cancel_stream_error")
        || StringUtility.containsStringIgnoreCase(message, "Broken pipe");
  }
}
