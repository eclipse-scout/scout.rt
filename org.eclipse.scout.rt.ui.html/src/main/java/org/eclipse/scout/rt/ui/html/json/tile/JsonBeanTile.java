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

import org.eclipse.scout.rt.client.ui.tile.IBeanTile;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonBean;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceMediator;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;

public class JsonBeanTile<BEAN_TILE extends IBeanTile<?>> extends JsonTile<BEAN_TILE> implements IBinaryResourceProvider {
  private final BinaryResourceMediator m_binaryResourceMediator;

  public JsonBeanTile(BEAN_TILE model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    m_binaryResourceMediator = createBinaryResourceMediator();
  }

  protected BinaryResourceMediator createBinaryResourceMediator() {
    return new BinaryResourceMediator(this);
  }

  @Override
  public String getObjectType() {
    return "BeanTile";
  }

  @Override
  protected void initJsonProperties(BEAN_TILE model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<BEAN_TILE>(IBeanTile.PROP_BEAN, model) {
      @Override
      protected Object modelValue() {
        return getModel().getBean();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return beanToJson(value);
      }
    });
  }

  protected Object beanToJson(Object value) {
    IJsonObject jsonObject = MainJsonObjectFactory.get().createJsonObject(value);
    JsonBean jsonBean = (JsonBean) jsonObject;
    jsonBean.setBinaryResourceMediator(m_binaryResourceMediator);
    return jsonObject.toJson();
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.APP_LINK_ACTION.matches(event.getType())) {
      handleUiAppLinkAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiAppLinkAction(JsonEvent event) {
    String ref = event.getData().optString("ref", null);
    getModel().getUIFacade().fireAppLinkActionFromUI(ref);
  }

  @Override
  public BinaryResourceHolder provideBinaryResource(String filename) {
    return m_binaryResourceMediator.getBinaryResourceHolder(filename);
  }
}
