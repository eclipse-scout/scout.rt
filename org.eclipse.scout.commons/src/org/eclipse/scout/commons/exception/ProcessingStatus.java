/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.exception;

import java.io.Serializable;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;

/**
 * A concrete status implementation, suitable either for instantiating or
 * subclassing.
 * <p>
 * This class can be used without OSGi running.
 * </p>
 */
public class ProcessingStatus implements IProcessingStatus, Serializable {
  private static final long serialVersionUID = 1L;

  private int m_severity;

  /**
   * Custom status code.
   */
  private int m_code;

  private String m_plugin = "";

  /**
   * Title, localized to the current locale.
   */
  private String m_title;

  /**
   * Message, localized to the current locale.
   */
  private String m_message = "";

  private ArrayList<String> m_contextMessages;

  /**
   * Wrapped exception, or <code>null</code> if none.
   */
  private Throwable m_cause = null;

  /**
   * Creates a new status object. The created status has no children.
   * 
   * @param message
   *          a human-readable message, localized to the current locale, is
   *          never null
   * @param severity
   *          the severity; one of <code>FATAL</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>
   */
  public ProcessingStatus(String message, int severity) {
    setMessage(message);
    setSeverity(severity);
  }

  /**
   * Creates a new status object. The created status has no children.
   * 
   * @param title
   *          a human-readable title, localized to the current locale, can be
   *          null
   * @param message
   *          a human-readable message, localized to the current locale, is
   *          never null
   * @param severity
   *          the severity; one of <code>FATAL</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>
   */
  public ProcessingStatus(String title, String message, int severity) {
    setTitle(title);
    setMessage(message);
    setSeverity(severity);
  }

  /**
   * Creates a new status object. The created status has no children.
   * 
   * @param message
   *          a human-readable message, localized to the current locale, is
   *          never null
   * @param cause
   *          a low-level exception, or <code>null</code> if not applicable
   * @param code
   *          the custom status code
   * @param severity
   *          the severity; one of <code>FATAL</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>
   */
  public ProcessingStatus(String message, Throwable cause, int code, int severity) {
    setMessage(message);
    setException(cause);
    setCode(code);
    setSeverity(severity);
  }

  /**
   * Creates a new status object. The created status has no children.
   * 
   * @param title
   *          a human-readable title, localized to the current locale, can be
   *          null
   * @param message
   *          a human-readable message, localized to the current locale, is
   *          never null
   * @param cause
   *          a low-level exception, or <code>null</code> if not applicable
   * @param code
   *          the custom status code
   * @param severity
   *          the severity; one of <code>FATAL</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>
   */
  public ProcessingStatus(String title, String message, Throwable cause, int code, int severity) {
    setTitle(title);
    setMessage(message);
    setException(cause);
    setCode(code);
    setSeverity(severity);
  }

  public ProcessingStatus(IStatus s) {
    if (s != null) {
      setMessage(s.getMessage());
      setException(s.getException());
      setCode(s.getCode());
      setSeverity(s.getSeverity());
      if (s instanceof IProcessingStatus) {
        setTitle(((IProcessingStatus) s).getTitle());
      }
    }
  }

  @Override
  public IProcessingStatus[] getChildren() {
    return new IProcessingStatus[0];
  }

  @Override
  public boolean isMultiStatus() {
    return false;
  }

  @Override
  public int getCode() {
    return m_code;
  }

  @Override
  public Throwable getCause() {
    return m_cause;
  }

  @Override
  public Throwable getException() {
    return m_cause;
  }

  @Override
  public String getPlugin() {
    return m_plugin;
  }

  public void setPlugin(String plugin) {
    m_plugin = plugin;
  }

  @Override
  public String getMessage() {
    return m_message;
  }

  @Override
  public String getTitle() {
    return m_title;
  }

  @Override
  public int getSeverity() {
    return m_severity;
  }

  @Override
  public boolean isOK() {
    return m_severity == OK;
  }

  @Override
  public boolean matches(int severityMask) {
    return (getSeverity() & severityMask) != 0;
  }

  /**
   * Sets the status code.
   * 
   * @param code
   *          the custom status code
   */
  public void setCode(int code) {
    m_code = code;
  }

  /**
   * Sets the exception.
   * 
   * @param exception
   *          a low-level exception, or <code>null</code> if not applicable
   */
  public void setException(Throwable exception) {
    m_cause = exception;
  }

  /**
   * Sets the message.
   * 
   * @param title
   *          a human-readable message, localized to the current locale
   */
  public void setTitle(String title) {
    m_title = title;
  }

  /**
   * Sets the message.
   * 
   * @param message
   *          a human-readable message, localized to the current locale
   */
  public void setMessage(String message) {
    m_message = message;
  }

  @Override
  public void addContextMessage(String message) {
    if (message != null) {
      if (m_contextMessages == null) {
        m_contextMessages = new ArrayList<String>();
      }
      m_contextMessages.add(0, message);
    }
  }

  @Override
  public String[] getContextMessages() {
    return m_contextMessages != null ? m_contextMessages.toArray(new String[0]) : new String[0];
  }

  /**
   * Sets the severity.
   * 
   * @param severity
   *          the severity; one of <code>OK</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>, or
   *          <code>CANCEL</code>
   */
  public void setSeverity(int severity) {
    switch (severity) {
      case INFO:
      case WARNING:
      case ERROR:
      case FATAL:
      case OK:
      case CANCEL: {
        break;
      }
      default: {
        throw new IllegalArgumentException("illegal severity: " + severity);
      }
    }
    m_severity = severity;
  }

  /**
   * Returns a string representation of the status, suitable for debugging
   * purposes only.
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getClass().getSimpleName() + "["); //$NON-NLS-1$
    switch (getSeverity()) {
      case INFO: {
        buf.append("INFO"); //$NON-NLS-1$
        break;
      }
      case WARNING: {
        buf.append("WARNING"); //$NON-NLS-1$
        break;
      }
      case ERROR: {
        buf.append("ERROR"); //$NON-NLS-1$
        break;
      }
      case FATAL: {
        buf.append("FATAL"); //$NON-NLS-1$
        break;
      }
      case OK: {
        buf.append("OK"); //$NON-NLS-1$
        break;
      }
      case CANCEL: {
        buf.append("CANCEL"); //$NON-NLS-1$
        break;
      }
      default: {
        buf.append("severity=" + getSeverity());
      }
    }
    buf.append(" code=" + m_code); //$NON-NLS-1$
    if (m_contextMessages != null) {
      for (String s : m_contextMessages) {
        buf.append(" ");
        buf.append(s);
        buf.append(" /");
      }
    }
    buf.append(" ");
    buf.append(m_message);
    if (m_cause != null) {
      buf.append(" ");
      buf.append(m_cause);
    }
    buf.append("]"); //$NON-NLS-1$
    return buf.toString();
  }

}
