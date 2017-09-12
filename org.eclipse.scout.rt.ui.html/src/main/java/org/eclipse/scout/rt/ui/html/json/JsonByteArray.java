/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.platform.util.Base64Utility;

public class JsonByteArray implements IJsonObject {
  private final byte[] m_bytes;

  public JsonByteArray(byte[] bytes) {
    m_bytes = bytes;
  }

  public JsonByteArray(String jsonString) {
    m_bytes = Base64Utility.decode(jsonString);
  }

  public byte[] getBytes() {
    return m_bytes;
  }

  @Override
  public Object toJson() {
    if (m_bytes == null) {
      return null;
    }
    return Base64Utility.encode(m_bytes);
  }

}
