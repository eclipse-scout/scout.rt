/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.colorfield;

import org.eclipse.scout.rt.client.ui.form.fields.colorfield.IColorField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

/**
 * Currently not supported by Html UI. JsonColorField.java and ColorField.js are just stub implementations.
 *
 * @since 5.2
 */
public class JsonColorField<COLOR_FIELD extends IColorField> extends JsonValueField<COLOR_FIELD> {

  public JsonColorField(COLOR_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ColorField";
  }
}
