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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.servicetunnel.HttpException;

/**
 * Popup to visualize an error.
 */
@Bean
public class ErrorPopup {

  private final AtomicBoolean m_parsed = new AtomicBoolean();
  protected String m_title;
  protected String m_text;
  protected String m_detail;
  protected IHtmlContent m_htmlDetail;
  protected String m_acceptText;
  protected ProcessingException m_cause;

  /**
   * Opens the popup to desribe the error.
   */
  public void showMessageBox(Throwable error) {
    ensureErrorParsed(error);

    MessageBoxes.create()
        .withHeader(m_text)
        .withBody(m_detail)
        .withHtml(m_htmlDetail)
        .withYesButtonText(m_acceptText)
        .withSeverity(m_cause.getStatus().getSeverity())
        .show();
  }

  @Internal
  protected void ensureErrorParsed(Throwable exception) {
    if (!m_parsed.compareAndSet(false, true)) {
      return;
    }

    if (exception instanceof UndeclaredThrowableException) {
      exception = ((UndeclaredThrowableException) exception).getCause();
    }
    if (exception instanceof ProcessingException) {
      m_cause = (ProcessingException) exception;
    }
    else {
      m_cause = new ProcessingException(ScoutTexts.get("ErrorAndRetryTextDefault"), exception).withTitle(ScoutTexts.get("Error"));
    }
    m_text = m_cause.getStatus().getTitle();
    if (m_text == null || m_text.length() == 0) {
      m_text = ScoutTexts.get("Error");
    }
    m_detail = m_cause.getStatus().getBody();
    m_acceptText = ScoutTexts.get("Ok");
    Throwable t = exception;
    while (t != null) {
      String msg = "\n\n" + StringUtility.wrapWord(ScoutTexts.get("OriginalErrorMessageIs", t.getClass().getSimpleName() + " " + t.getLocalizedMessage()), 80);
      if (t instanceof HttpException) {
        int statusCode = ((HttpException) t).getStatusCode();
        switch (statusCode) {
          case 401:
          case 403: {
            m_title = ScoutTexts.get("ErrorTitleLogin");
            m_text = ScoutTexts.get("ErrorTextLogin") + msg;
            break;
          }
          default: {
            createNetErrorMessage(msg);
          }
        }
        return;
      }
      else if (t instanceof GeneralSecurityException) {
        m_title = ScoutTexts.get("ErrorTitleLogin");
        m_text = ScoutTexts.get("ErrorTextLogin") + msg;
        return;
      }
      else if (t instanceof SecurityException) {
        m_title = ScoutTexts.get("ErrorTitleSecurity");
        m_text = ScoutTexts.get("ErrorTextSecurity") + msg;
        return;
      }
      else if (t instanceof MalformedURLException) {
        createNetErrorMessage(msg);
        return;
      }
      else if (t instanceof ThreadInterruptedException || t instanceof java.lang.InterruptedException) {
        m_title = ScoutTexts.get("InterruptedErrorTitle");
        m_text = ScoutTexts.get("InterruptedErrorText");
        return;
      }
      else if (t instanceof UnknownHostException) {
        createNetErrorMessage(msg);
        return;
      }
      else if (t instanceof FileNotFoundException) {
        m_title = ScoutTexts.get("FileNotFoundTitle");
        m_text = ScoutTexts.get("FileNotFoundMessage", ((FileNotFoundException) t).getMessage());
        return;
      }
      else if (t instanceof NoRouteToHostException) {
        createNetErrorMessage(msg);
        return;
      }
      else if (t instanceof SocketException) {
        createNetErrorMessage(msg);
        return;
      }
      else if (t instanceof IOException) {
        m_title = ScoutTexts.get("IOErrorTitle");
        m_text = ScoutTexts.get("IOErrorText") + ": " + t.getLocalizedMessage() + msg;
        m_detail = ScoutTexts.get("IOErrorInfo");
        return;
      }
      else if (t instanceof VetoException) {
        createVetoExceptionMessage((VetoException) t, msg);
        return;
      }
      t = t.getCause();
    }
    // default proceed
    StringWriter buf = new StringWriter();
    t = exception;
    String indent = "";
    while (t != null) {
      String s = null;
      if (t instanceof ProcessingException) {
        s = ((ProcessingException) t).getStatus().getBody();
      }
      else {
        s = t.getMessage();
      }
      buf.append(indent);
      if (s != null) {
        buf.append(s + (t.getClass() != ProcessingException.class ? " (" + t.getClass().getSimpleName() + ")" : ""));
      }
      else {
        buf.append(t.getClass().getSimpleName());
      }
      buf.append("\n");
      // next
      indent += "  ";
      t = t.getCause();
    }
    m_detail = StringUtility.wrapWord(ScoutTexts.get("OriginalErrorMessageIs", buf.toString()), 120);
  }

  protected void createNetErrorMessage(String msg) {
    m_title = ScoutTexts.get("NetErrorTitle");
    m_text = ScoutTexts.get("NetErrorText") + msg;
    m_detail = ScoutTexts.get("NetErrorInfo");
  }

  protected void createVetoExceptionMessage(VetoException exception, String msg) {
    m_text = exception.getStatus().getTitle();
    if (exception.getHtmlMessage() != null) {
      m_htmlDetail = exception.getHtmlMessage();
      m_detail = "";
    }
    else if (StringUtility.hasText(exception.getStatus().getBody())) {
      m_detail = exception.getStatus().getBody();
    }

    if (!StringUtility.hasText(m_detail)
        && m_htmlDetail == null) {
      m_detail = ScoutTexts.get("VetoErrorText") + msg;
    }
  }
}
