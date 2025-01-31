/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.tile;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.tile.IWidgetTile;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;

/**
 * @since 8.0
 */
public class JsonWidgetTile<T extends IWidgetTile> extends JsonTile<T> {

  public JsonWidgetTile(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "WidgetTile";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(IWidgetTile.PROP_TILE_WIDGET, model, getUiSession()) {
      @Override
      protected IWidget modelValue() {
        return getModel().getTileWidget();
      }
    });
  }
}
