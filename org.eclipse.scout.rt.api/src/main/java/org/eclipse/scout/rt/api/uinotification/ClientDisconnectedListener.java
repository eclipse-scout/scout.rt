/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.uinotification;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When using HTTP/2, stream cancel errors are propagated but Jersey does not handle them correctly -> asyncResponse still looks valid but cannot be resumed anymore.
 * This means if a client disconnects / aborts the connection, resuming the response will fail and a severe error logged.
 * To prevent that, we need to track the disconnection by ourselves. Another solution would be to disable the stream cancel error propagation in Jetty.
 *
 * @see <a href="https://github.com/eclipse-ee4j/jersey/issues/3691">Jersey issue 3691</a>
 * @see <a href="https://github.com/eclipse/jetty.project/issues/1891">Jetty Issue 1891</a>
 */
public class ClientDisconnectedListener implements AsyncListener {
  private static final Logger LOG = LoggerFactory.getLogger(UiNotificationResource.class);
  private boolean m_disconnected;

  @Override
  public void onComplete(AsyncEvent event) {
  }

  @Override
  public void onTimeout(AsyncEvent event) {
  }

  @Override
  public void onError(AsyncEvent event) {
    setDisconnected(true);
    String originalMessage = "";
    if (event.getThrowable() != null) {
      originalMessage = " Original message: " + event.getThrowable().getMessage();
    }
    LOG.info("Error while waiting for async response, client probably has disconnected.{}", originalMessage);
  }

  @Override
  public void onStartAsync(AsyncEvent event) {
  }

  public boolean isDisconnected() {
    return m_disconnected;
  }

  private void setDisconnected(boolean disconnected) {
    m_disconnected = disconnected;
  }
}
