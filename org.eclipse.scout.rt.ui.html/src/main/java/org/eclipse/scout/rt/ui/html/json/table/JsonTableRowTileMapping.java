/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowTileMapping;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;

public class JsonTableRowTileMapping<TABLE_ROW_TILE_MAPPING extends ITableRowTileMapping> extends AbstractJsonPropertyObserver<TABLE_ROW_TILE_MAPPING> {

  public JsonTableRowTileMapping(TABLE_ROW_TILE_MAPPING model, IUiSession uiSession, String id, IJsonAdapter parent) {
    super(model, uiSession, id, parent);
    assertTrue(parent instanceof JsonTable<?>);
  }

  @Override
  public String getObjectType() {
    return "TableRowTileMapping";
  }

  @Override
  public JsonTable<?> getParent() {
    return assertNotNull((JsonTable<?>) super.getParent());
  }

  @Override
  protected void initJsonProperties(TABLE_ROW_TILE_MAPPING model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ITableRowTileMapping>(ITableRowTileMapping.PROP_TABLE_ROW, model) {
      @Override
      protected ITableRow modelValue() {
        return getModel().getTableRow();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value instanceof ITableRow) {
          return getParent().getOrCreateRowId(((ITableRow) value));
        }
        return null;
      }
    });
    putJsonProperty(new JsonAdapterProperty<ITableRowTileMapping>(ITableRowTileMapping.PROP_TILE, model, getUiSession()) {
      @Override
      protected ITile modelValue() {
        return getModel().getTile();
      }
    });
  }

}
