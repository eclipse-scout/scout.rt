/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonBean;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;

public class JsonBeanColumn<T extends IColumn<?>> extends JsonColumn<T> {

  public JsonBeanColumn(T model) {
    super(model);
  }

  @Override
  public String getObjectType() {
    return "BeanColumn";
  }

  @Override
  public boolean isValueRequired() {
    return true;
  }

  @Override
  public Object cellValueToJson(Object value) {
    IJsonObject jsonObject = MainJsonObjectFactory.get().createJsonObject(value);
    handleBinaryResource(jsonObject);
    return jsonObject.toJson();
  }

  /**
   * Not every IJsonObject is a JsonBean, but we must handle binary resources if we have a JsonBean.
   */
  protected void handleBinaryResource(IJsonObject jsonObject) {
    if (jsonObject instanceof JsonBean) {
      ((JsonBean) jsonObject).setBinaryResourceMediator(getJsonTable().getBinaryResourceMediator());
    }
  }
}
