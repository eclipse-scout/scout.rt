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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.exception.IProcessingStatus;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.AbstractInterruptionError;
import org.eclipse.scout.rt.shared.AbstractIcons;

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
        .withIconId(computeIconId())
        .show();

    if (m_reloadOnYesClick && result == IMessageBox.YES_OPTION) {
      ClientSessionProvider.currentSession().getDesktop().reloadGui();
    }
  }

  protected String computeIconId() {
    if (m_parsedError instanceof VetoException) {
      return null;
    }
    if (m_status != null && m_status.getSeverity() == IStatus.ERROR) {
      return AbstractIcons.Slippery;
    }
    return null;
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
   * Returns {@link Throwable} parsed out of given error
   *
   * @see #ensureErrorParsed(Throwable)
   */
  protected Throwable getParsedError() {
    return m_parsedError;
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
    if (isNetError(t)) {
      parseNetError(t);
      return true;
    }
    if (t instanceof AbstractInterruptionError ||
        t instanceof InterruptedException ||
        t instanceof TimeoutException ||
        t instanceof CancellationException) {
      parseInterruptedError(t);
      return true;
    }
    return false;
  }

  /**
   * Checks if given {@link Throwable} indicates a network error
   * <ul>
   * <li>{@link RemoteSystemUnavailableException} is thrown by HTTP ServiceTunnel
   * <li>{@link SocketException} is the parent of {@link ConnectException} and {@link NoRouteToHostException} (server is
   * not available)
   * <li>{@link UnknownHostException} indicates that server IP could not be resolved
   * </ul>
   */
  protected boolean isNetError(Throwable t) {
    return t instanceof RemoteSystemUnavailableException ||
        t instanceof SocketException ||
        t instanceof UnknownHostException;
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
    if (Platform.get().inDevelopmentMode()) {
      m_noButtonText = TEXTS.get("Ignore");
    }
    m_reloadOnYesClick = true;
  }
}
