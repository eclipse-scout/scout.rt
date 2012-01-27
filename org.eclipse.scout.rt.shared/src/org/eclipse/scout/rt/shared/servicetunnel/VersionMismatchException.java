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
package org.eclipse.scout.rt.shared.servicetunnel;

import org.eclipse.scout.commons.exception.ProcessingException;

public class VersionMismatchException extends ProcessingException {
  private static final long serialVersionUID = 1L;

  private String m_oldVersion;
  private String m_newVersion;

  /**
   * Empty constructor is used to support auto-webservice publishing with java
   * bean support
   */
  public VersionMismatchException() {
    super();
  }

  public VersionMismatchException(String vOld, String vNew) {
    super("Version mismatch: got " + vOld + " required " + vNew);
    m_oldVersion = vOld;
    m_newVersion = vNew;
  }

  public String getOldVersion() {
    return m_oldVersion;
  }

  public String getNewVersion() {
    return m_newVersion;
  }

}
