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
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.client.IFieldStatus;
import org.eclipse.scout.rt.platform.status.Status;

/**
 * Status type for form fields with additional property "iconId"
 */
// TODO [6.2] aho: deprecation use {@link Status} instead.
public class ScoutFieldStatus extends Status implements IFieldStatus {
  private static final long serialVersionUID = 1L;

  public ScoutFieldStatus(String message, int severity) {
    this(message, null, severity);
  }

  public ScoutFieldStatus(String message, String iconId, int severity) {
    this(message, iconId, severity, 0);
  }

  public ScoutFieldStatus(String message, int severity, int code) {
    this(message, null, severity, code);
  }

  public ScoutFieldStatus(String message, String iconId, int severity, int code) {
    super(message, severity, code);
    withIconId(iconId);
  }

}
