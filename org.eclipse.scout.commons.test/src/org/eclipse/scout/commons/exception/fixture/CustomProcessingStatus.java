/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.exception.fixture;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.commons.exception.IProcessingStatus;

public class CustomProcessingStatus implements IProcessingStatus {
  private static final long serialVersionUID = 1L;
  private final String m_message;
  private final Throwable m_cause;

  public CustomProcessingStatus(String message, Throwable cause) {
    super();
    this.m_message = message;
    this.m_cause = cause;
  }

  @Override
  public void addContextMessage(String message) {
    // nop
  }

  @Override
  public Throwable getCause() {
    return m_cause;
  }

  @Override
  public String[] getContextMessages() {
    return null;
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public IStatus[] getChildren() {
    return null;
  }

  @Override
  public int getCode() {
    return 0;
  }

  @Override
  public Throwable getException() {
    return getCause();
  }

  @Override
  public String getMessage() {
    return m_message;
  }

  @Override
  public String getPlugin() {
    return null;
  }

  @Override
  public int getSeverity() {
    return 0;
  }

  @Override
  public boolean isMultiStatus() {
    return false;
  }

  @Override
  public boolean isOK() {
    return false;
  }

  @Override
  public boolean matches(int severityMask) {
    return false;
  }
}
