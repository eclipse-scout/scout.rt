/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * Marker for field status
 */
public class DefaultFieldStatus extends ScoutFieldStatus {
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
    super(message, iconId, severity, code);
  }

}
