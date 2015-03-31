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
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.commons.Base64Utility;
import org.json.JSONObject;

public class JsonByteArray implements IJsonObject {
  private byte[] m_bytes;

  public JsonByteArray(byte[] bytes) {
    m_bytes = bytes;
  }

  @Override
  public Object toJson() {
    //TODO CGU/IMO is a json object necessary?
    JSONObject b64 = new JSONObject();
    JsonObjectUtility.putProperty(b64, "b64", Base64Utility.encode((byte[]) m_bytes));
    return b64;
  }

}
