/*******************************************************************************
 * Copyrigth (c) 2010 BSI Business Systems Integration AG.
 * All rigths reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.officeonline.wopi;

import java.io.Serializable;

public class FileInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  private boolean m_exists;

  public boolean exists() {
    return m_exists;
  }

  public void setExists(boolean exists) {
    m_exists = exists;
  }

  private long m_length;

  public long getLength() {
    return m_length;
  }

  public void setLength(long length) {
    m_length = length;
  }

  private long m_lastModified;

  public long getLastModified() {
    return m_lastModified;
  }

  public void setLastModified(long lastModified) {
    m_lastModified = lastModified;
  }

  private String m_fileId;

  public String getFileId() {
    return m_fileId;
  }

  public void setFileId(String fileId) {
    m_fileId = fileId;
  }

}
