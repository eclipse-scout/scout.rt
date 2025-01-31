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

import org.eclipse.scout.rt.client.ui.tile.IHtmlTile;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;

public class JsonHtmlTile<T extends IHtmlTile> extends JsonTile<T> implements IBinaryResourceProvider {

  public JsonHtmlTile(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "HtmlTile";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(IHtmlTile.PROP_CONTENT, model) {
      @Override
      protected String modelValue() {
        return getModel().getContent();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.replaceImageUrls(JsonHtmlTile.this, (String) value);
      }
    });
  }

  @Override
  public BinaryResourceHolder provideBinaryResource(String filename) {
    BinaryResource attachment = getModel().getAttachment(filename);
    return attachment == null ? null : new BinaryResourceHolder(attachment);
  }
}
