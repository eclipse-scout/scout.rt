/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.contenteditor;

import org.eclipse.scout.rt.client.ui.contenteditor.IContentEditorElementTile;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.tile.JsonTile;

/**
 * @since 7.1
 */
public class JsonContentEditorElementTile<T extends IContentEditorElementTile> extends JsonTile<T> {

  public JsonContentEditorElementTile(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ContentEditorElementTile";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(IContentEditorElementTile.PROP_LABEL, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabel();
      }
    });
    putJsonProperty(new JsonProperty<T>(IContentEditorElementTile.PROP_DESCRIPTION, model) {
      @Override
      protected String modelValue() {
        return getModel().getDescription();
      }
    });
    putJsonProperty(new JsonProperty<T>(IContentEditorElementTile.PROP_CONTENT_ELEMENT_DESIGN_HTML, model) {
      @Override
      protected String modelValue() {
        return getModel().getContentElementDesignHtml();
      }
    });
    putJsonProperty(new JsonProperty<T>(IContentEditorElementTile.PROP_IDENTIFIER, model) {
      @Override
      protected String modelValue() {
        return getModel().getIdentifier();
      }
    });
    putJsonProperty(new JsonProperty<T>(IContentEditorElementTile.PROP_ICON_ID, model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }
    });
  }
}
