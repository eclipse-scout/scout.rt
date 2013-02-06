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
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.rt.shared.AbstractIcons;

/**
 * Status type for form fields with additional property "iconId"
 */
public class ScoutFieldStatus extends ProcessingStatus {
  private static final long serialVersionUID = 1L;

  public ScoutFieldStatus(String message, int severity) {
    super(message, null, 0, severity);
  }

  public ScoutFieldStatus(String message, String iconId, int severity) {
    super(message, null, 0, severity);
    setIconId(iconId);
  }

  public ScoutFieldStatus(IProcessingStatus s, int severity) {
    super(s);
    setSeverity(severity);
  }

  private String m_iconId;

  /**
   * icon id are defined either in {@link AbstractIcons} or in the project
   * specific subclass named Icons
   */
  public String getIconId() {
    return m_iconId;
  }

  /**
   * icon id are defined either in {@link AbstractIcons} or in the project
   * specific subclass named Icons
   */
  public void setIconId(String s) {
    m_iconId = s;
  }
}
