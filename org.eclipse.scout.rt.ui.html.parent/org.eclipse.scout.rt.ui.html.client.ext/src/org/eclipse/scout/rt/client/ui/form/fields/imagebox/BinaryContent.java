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

  private String m_contentType;

  public BinaryContent(byte[] content, String contentType) {
    m_content = content;
    m_contentType = contentType; // FIXME AWE: rename to m_fileExtension
  }

  public byte[] getContent() {
    return m_content;
  }

  public String getContentType() {
    return m_contentType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(m_content);
    result = prime * result + ((m_contentType == null) ? 0 : m_contentType.hashCode());
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
    if (m_contentType == null) {
      if (other.m_contentType != null) {
        return false;
      }
    }
    else if (!m_contentType.equals(other.m_contentType)) {
      return false;
    }
    return true;
  }

}
