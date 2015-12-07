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

import java.text.DecimalFormat;

import org.json.JSONObject;

/**
 * Transforms a DecimalFormat to a JsonObject.
 * <p>
 * Format is transfered as string representing the pattern. Properties not contained in pattern are transferred
 * separately.
 */
public class JsonDecimalFormat implements IJsonObject {

  private final DecimalFormat m_decimalFormat;

  public JsonDecimalFormat(DecimalFormat decimalFormat) {
    m_decimalFormat = decimalFormat;
  }

  public DecimalFormat getDecimalFormat() {
    return m_decimalFormat;
  }

  @Override
  public Object toJson() {
    if (getDecimalFormat() == null) {
      return null;
    }
    JSONObject json = JsonObjectUtility.newOrderedJSONObject();
    json.put("pattern", getDecimalFormat().toPattern());
    json.put("multiplier", getDecimalFormat().getMultiplier());
    json.put("roundingMode", getDecimalFormat().getRoundingMode().name());
    return json;
  }

}
