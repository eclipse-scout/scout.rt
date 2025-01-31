/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.exceptionhandler;

import java.util.concurrent.Semaphore;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.IProcessingStatus;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central point for client-side exception handling.
 * <p>
 * This implementation logs the exception and opens {@link ErrorPopup} to visualize the exception.
 */
@Replace
public class ClientExceptionHandler extends ExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ClientExceptionHandler.class);
  private static final String LOOP_DETECTION_KEY = "ClientExceptionHandler#loopDetectionSemaphore";

  @Override
  protected void handlePlatformException(final PlatformException e) {
    super.handlePlatformException(e);
    showExceptionInternal(e);
  }

  @Override
  protected void handleThrowable(final Throwable t) {
    super.handleThrowable(t);
    showExceptionInternal(t);
  }

  protected void showExceptionInternal(final Throwable t) {
    final IClientSession session = ClientSessionProvider.currentSession();
    if (session == null) {
      return;
    }

    if (session.isStopping() || session.getDesktop() == null || !session.getDesktop().isOpened()) {
      return;
    }

    // Prevent loops while displaying the exception.
    final Semaphore loopDetectionSemaphore = getLoopDetectionSemaphore();
    if (loopDetectionSemaphore.tryAcquire()) {
      try {
        // Synchronize with the model thread if not applicable.
        if (ModelJobs.isModelThread()) {
          showException(t);
        }
        else {
          try {
            ModelJobs.schedule(() -> showException(t), ModelJobs.newInput(ClientRunContexts.copyCurrent())
                    .withExceptionHandling(null, true)
                    .withName("Visualizing PlatformException"))
                .awaitDone();
          }
          catch (final ThreadInterruptedError e) { // NOSONAR
            // NOOP
          }
        }
      }
      finally {
        loopDetectionSemaphore.release();
      }
    }
    else {
      Exception e = new Exception("Stacktrace and suppressed exception");
      // add original exception for analysis
      e.addSuppressed(t);
      LOG.warn("Loop detection in {}", getClass().getName(), e);

      if (ModelJobs.isModelThread()) {
        IMessageBox msgBox = MessageBoxes
            .createOk()
            .withSeverity(IStatus.ERROR)
            .withHeader(TEXTS.get("Error"));

        if (t instanceof VetoException) {
          IProcessingStatus status = ((ProcessingException) t).getStatus();
          msgBox
              .withHeader(status.getTitle())
              .withBody(status.getBody());
        }

        msgBox.show();
      }
    }
  }

  /**
   * Method invoked to visualize the exception. This method is invoked in the model thread.
   */
  protected void showException(final Throwable t) {
    BEANS.get(ErrorPopup.class).showMessageBox(t);
  }

  protected Semaphore getLoopDetectionSemaphore() {
    RunContext runContext = RunContext.CURRENT.get();
    Semaphore semaphore = (Semaphore) runContext.getProperty(LOOP_DETECTION_KEY);
    if (semaphore == null) {
      semaphore = new Semaphore(1);
      runContext.withProperty(LOOP_DETECTION_KEY, semaphore);
    }
    return semaphore;
  }
}
