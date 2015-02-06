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
package org.eclipse.scout.rt.client.ui.form.fields.imagebox;

import java.io.Serializable;
import java.util.Arrays;

public class BinaryContent implements Serializable {
  private static final long serialVersionUID = 1L;

  private byte[] m_content;
  private String m_fileExtension;

  public BinaryContent(byte[] content, String fileExtension) {
    m_content = content;
    m_fileExtension = fileExtension;
  }

  public byte[] getContent() {
    return m_content;
  }

  public String getFileExtension() {
    return m_fileExtension;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(m_content);
    result = prime * result + ((m_fileExtension == null) ? 0 : m_fileExtension.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BinaryContent other = (BinaryContent) obj;
    if (!Arrays.equals(m_content, other.m_content)) {
      return false;
    }
    if (m_fileExtension == null) {
      if (other.m_fileExtension != null) {
        return false;
      }
    }
    else if (!m_fileExtension.equals(other.m_fileExtension)) {
      return false;
    }
    return true;
  }
}
