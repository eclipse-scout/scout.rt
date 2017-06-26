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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.exception.IProcessingStatus;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.AbstractInterruptionError;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.servicetunnel.HttpException;

/**
 * Popup to visualize an error.
 */
@Bean
public class ErrorPopup {

  private final AtomicBoolean m_parsed = new AtomicBoolean();

  protected String m_header;
  protected String m_body;
  protected IHtmlContent m_html;
  protected String m_yesButtonText;
  protected String m_noButtonText;
  protected boolean m_reloadOnYesClick;
  protected Throwable m_parsedError;
  protected IStatus m_status;

  /**
   * Opens the popup to describe the error.
   */
  public void showMessageBox(Throwable error) {
    ensureErrorParsed(error);

    int result = MessageBoxes.create()
        .withHeader(m_header)
        .withBody(m_body)
        .withHtml(m_html)
        .withYesButtonText(m_yesButtonText)
        .withNoButtonText(m_noButtonText)
        .withSeverity(m_status.getSeverity())
        .show();

    if (m_reloadOnYesClick && result == IMessageBox.YES_OPTION) {
      ClientSessionProvider.currentSession().getDesktop().reloadGui();
    }
  }

  /**
   * Fills the member variables based on the given <code>error</code>. This method has no effect after the first
   * execution.
   */
  protected void ensureErrorParsed(Throwable error) {
    if (!m_parsed.compareAndSet(false, true)) {
      return;
    }

    m_parsedError = unwrapException(error);

    // Defaults
    m_header = TEXTS.get("Error");
    m_body = TEXTS.get("ErrorAndRetryTextDefault");
    m_html = HTML.div(StringUtility.box(TEXTS.get("CorrelationId") + ": ", CorrelationId.CURRENT.get(), ""))
        .cssClass("error-popup-correlation-id");
    m_yesButtonText = TEXTS.get("Ok");
    m_noButtonText = null;
    m_reloadOnYesClick = false;
    m_status = new Status(IStatus.ERROR);

    Throwable t = m_parsedError;
    Throwable rootCause = m_parsedError;
    while (t != null) {
      if (parseError(t)) {
        extractStatus(t);
        m_parsedError = t;
        return;
      }
      rootCause = t;
      t = t.getCause();
    }
    parseUnexpectedProblem(rootCause);
  }

  /**
   * If the given exception is a "wrapper exception" (as returned by {@link #isWrapperException(Throwable)}), the
   * wrapped exception is returned. Otherwise, the original exception is returned.
   */
  protected Throwable unwrapException(Throwable t) {
    if (isWrapperException(t)) {
      return t.getCause();
    }
    return t;
  }

  /**
   * @return <code>true</code> for exceptions of the type {@link UndeclaredThrowableException},
   *         {@link InvocationTargetException} and {@link ExecutionException}.
   */
  protected boolean isWrapperException(final Throwable t) {
    return t instanceof UndeclaredThrowableException
        || t instanceof InvocationTargetException
        || t instanceof ExecutionException;
  }

  /**
   * If the given error is a {@link ProcessingException} that has a {@link IProcessingStatus}, this status is set to the
   * member variable {@link #m_status}. Otherwise, nothing happens.
   */
  protected void extractStatus(Throwable t) {
    if (t instanceof ProcessingException) {
      IProcessingStatus status = ((ProcessingException) t).getStatus();
      if (status != null) {
        m_status = status;
      }
    }
  }

  /**
   * @return <code>true</code> if the error was handled by this method and the parsing is finished.
   */
  protected boolean parseError(Throwable t) {
    if (t instanceof VetoException) {
      parseVetoException((VetoException) t);
      return true;
    }
    // HttpException is thrown by ServiceTunnel
    // SocketException is the parent of ConnectException (happens when server is not available) and NoRouteToHostException
    if (t instanceof HttpException || t instanceof UnknownHostException || t instanceof SocketException) {
      parseNetError(t);
      return true;
    }
    if (t instanceof AbstractInterruptionError ||
        t instanceof java.lang.InterruptedException ||
        t instanceof java.util.concurrent.TimeoutException ||
        t instanceof java.util.concurrent.CancellationException) {
      parseInterruptedError(t);
      return true;
    }
    return false;
  }

  /**
   * Sets the member variables for an exception of the type {@link VetoException}. No technical info is shown, instead
   * the text is extracted from the exceptions {@link IProcessingStatus}.
   */
  protected void parseVetoException(VetoException ve) {
    m_header = ve.getStatus().getTitle();
    if (ve.getHtmlMessage() != null) {
      m_body = null;
      m_html = ve.getHtmlMessage();
    }
    else if (ve.getStatus().getBody() != null) {
      m_body = ve.getStatus().getBody();
      m_html = null;
    }
  }

  /**
   * Sets the member variables for an exception that indicates a problem with another required system (back-end server,
   * database etc.).
   */
  protected void parseNetError(Throwable t) {
    m_header = TEXTS.get("NetErrorTitle");
    m_body = TEXTS.get("NetSystemsNotAvailable") + "\n\n" + TEXTS.get("PleaseTryAgainLater");
  }

  /**
   * Sets the member variables for an exception that indicates an interruption.
   */
  protected void parseInterruptedError(Throwable t) {
    m_header = TEXTS.get("InterruptedErrorTitle");
    m_body = TEXTS.get("InterruptedErrorText");
    m_html = null;
    m_status = new Status(IStatus.INFO);
  }

  /**
   * Sets the member variables for all errors that were not handled by {@link #parseError(Throwable)}. By default,
   * produces a generic "some internal problem has happened" message, hiding the technical details.
   */
  protected void parseUnexpectedProblem(Throwable error) {
    String errorCode = null;
    if (error != null) {
      extractStatus(error);
      if (m_status.getCode() > 0) {
        errorCode = String.valueOf(m_status.getCode());
      }
      else {
        String s = error.getClass().getSimpleName();
        errorCode = s.charAt(0) + "" + s.length();
      }
    }

    // The application might be in an inconsistent state -> user should reload it.
    // The same message is shown if an exception occurs on the UI.
    m_header = TEXTS.get("UnexpectedProblem");
    m_body = StringUtility.join("\n\n",
        TEXTS.get("InternalProcessingErrorMsg", (errorCode == null ? "" : " (" + TEXTS.get("ErrorCodeX", errorCode) + ")")),
        TEXTS.get("UiInconsistentMsg"));
    m_yesButtonText = TEXTS.get("Reload");
    m_noButtonText = TEXTS.get("Ignore");
    m_reloadOnYesClick = true;
  }
}
