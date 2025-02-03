/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.exception.fixture;

import org.eclipse.scout.rt.platform.exception.IProcessingStatus;
import org.eclipse.scout.rt.platform.status.IStatus;

public class CustomProcessingStatus implements IProcessingStatus {
  private final String m_message;
  private final Throwable m_cause;

  public CustomProcessingStatus(String message, Throwable cause) {
    super();
    this.m_message = message;
    this.m_cause = cause;
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public int getCode() {
    return 0;
  }

  @Override
  public Throwable getException() {
    return m_cause;
  }

  @Override
  public String getMessage() {
    return m_message;
  }

  @Override
  public int getSeverity() {
    return IStatus.ERROR;
  }

  @Override
  public String getIconId() {
    return null;
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

  @Override
  public int compareTo(IStatus o) {
    return 0;
  }

  @Override
  public String getBody() {
    return null;
  }

  @Override
  public double getOrder() {
    return 0;
  }
}
