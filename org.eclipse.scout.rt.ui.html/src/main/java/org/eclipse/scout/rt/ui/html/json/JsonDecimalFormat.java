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
    JSONObject json = new JSONObject();
    json.put("pattern", getDecimalFormat().toPattern());
    json.put("multiplier", getDecimalFormat().getMultiplier());
    json.put("roundingMode", getDecimalFormat().getRoundingMode().name());
    return json;
  }
}
