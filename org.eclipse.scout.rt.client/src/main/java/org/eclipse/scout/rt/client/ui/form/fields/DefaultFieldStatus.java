/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;

/**
 * Marker for field status
 */
public class DefaultFieldStatus extends Status {
  private static final long serialVersionUID = 1L;

  public DefaultFieldStatus(String message) {
    this(message, null, IStatus.ERROR);
  }

  public DefaultFieldStatus(String message, int severity) {
    this(message, null, severity);
  }

  public DefaultFieldStatus(String message, String iconId, int severity) {
    this(message, iconId, severity, 0);
  }

  public DefaultFieldStatus(String message, int severity, int code) {
    this(message, null, severity, code);
  }

  public DefaultFieldStatus(String message, String iconId, int severity, int code) {
    super(message, severity, code, iconId);
  }

}
