/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.tilefield;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.ResourceListTransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.tilefield.ITileField;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;

/**
 * @since 8.0
 */
public class JsonTileField<T extends ITileField<? extends ITileGrid<? extends ITile>>> extends JsonFormField<T> implements IBinaryResourceConsumer {

  public JsonTileField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TileField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<>(ITileField.PROP_TILE_GRID, model, getUiSession()) {
      @Override
      protected ITileGrid<?> modelValue() {
        return getModel().getTileGrid();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileField.PROP_DROP_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDropType();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileField.PROP_DROP_MAXIMUM_SIZE, model) {
      @Override
      protected Long modelValue() {
        return getModel().getDropMaximumSize();
      }
    });
  }

  @Override
  public void consumeBinaryResource(List<BinaryResource> binaryResources, Map<String, String> uploadProperties) {
    if ((getModel().getDropType() & IDNDSupport.TYPE_FILE_TRANSFER) == IDNDSupport.TYPE_FILE_TRANSFER) {
      ResourceListTransferObject transferObject = new ResourceListTransferObject(binaryResources);
      getModel().getUIFacade().fireDropActionFromUI(transferObject);
    }
  }

  @Override
  public long getMaximumUploadSize() {
    return getModel().getDropMaximumSize();
  }
}
