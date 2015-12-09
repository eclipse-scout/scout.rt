/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.exceptionhandler;

import java.util.concurrent.Semaphore;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.ProcessingExceptionTranslator;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code ClientExceptionHandler} is the central point for handling client-side exceptions. For processing exceptions,
 * typically an error dialog is opened.
 */
@Replace
public class ClientExceptionHandler extends ExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ClientExceptionHandler.class);
  private static final String SESSION_DATA_KEY = "ClientExceptionHandler#loopDetectionSemaphore";

  @Override
  protected void handleThrowable(final Throwable t) {
    // Same error handling for all exceptions, except InterruptedException which is ignored.
    handleProcessingException(BEANS.get(ProcessingExceptionTranslator.class).translate(t));
  }

  @Override
  protected void handleProcessingException(final ProcessingException pe) {
    super.handleProcessingException(pe);

    final IClientSession session = ClientSessionProvider.currentSession();
    if (session == null) {
      LOG.debug("No session in current RunContext. ProcessingException cannot be visualized");
      return;
    }

    // Only open the error dialog if not running headless.
    if (session.getDesktop() == null || !session.getDesktop().isOpened()) {
      return;
    }

    // Prevent loops while displaying the exception.
    final Semaphore loopDetectionSemaphore = getLoopDetectionSemaphore(session);
    if (loopDetectionSemaphore.tryAcquire()) {
      try {
        // Synchronize with the model thread if not applicable.
        if (ModelJobs.isModelThread()) {
          showExceptionInUI(pe);
        }
        else {
          try {
            ModelJobs.schedule(new IRunnable() {

              @Override
              public void run() throws Exception {
                showExceptionInUI(pe);
              }
            }, ModelJobs.newInput(ClientRunContexts.copyCurrent())
                .withExceptionHandling(null, true)
                .withName("Visualizing ProcessingException"))
                .awaitDone();
          }
          catch (final InterruptedException e) {
            // NOOP
          }
        }
      }
      finally {
        loopDetectionSemaphore.release();
      }
    }
    else {
      LOG.error("Loop detection in {} when handling '{}'. StackTrace: ", getClass().getName(), pe, new Exception());
    }
  }

  /**
   * Method invoked to visualize the exception. This method is called in the model thread.
   */
  protected void showExceptionInUI(final ProcessingException pe) {
    BEANS.get(ErrorPopup.class).showMessageBox(pe);
  }

  @Internal
  protected Semaphore getLoopDetectionSemaphore(final IClientSession session) {
    Semaphore semaphore = (Semaphore) session.getData(SESSION_DATA_KEY);
    if (semaphore == null) {
      semaphore = new Semaphore(1);
      session.setData(SESSION_DATA_KEY, semaphore);
    }
    return semaphore;
  }
}
