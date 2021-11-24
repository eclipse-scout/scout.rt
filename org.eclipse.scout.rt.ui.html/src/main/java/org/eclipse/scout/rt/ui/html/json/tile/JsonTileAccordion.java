/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.tile;

import org.eclipse.scout.rt.client.ui.tile.ITileAccordion;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.accordion.JsonAccordion;

public class JsonTileAccordion<T extends ITileAccordion> extends JsonAccordion<T> {

  public JsonTileAccordion(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TileAccordion";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);

    // Selectable and multi select have to be in sync because the selection behavior affects the whole accordion (every tile grid)
    // Other properties may theoretically be different for each tile grid (although it has not been tested so far if it really works).
    putJsonProperty(new JsonProperty<>(ITileAccordion.PROP_SELECTABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelectable();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileAccordion.PROP_MULTI_SELECT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultiSelect();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileAccordion.PROP_TEXT_FILTER_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTextFilterEnabled();
      }
    });
  }

}
