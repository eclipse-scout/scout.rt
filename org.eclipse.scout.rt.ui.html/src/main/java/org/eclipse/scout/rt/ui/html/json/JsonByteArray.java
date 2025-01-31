/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
