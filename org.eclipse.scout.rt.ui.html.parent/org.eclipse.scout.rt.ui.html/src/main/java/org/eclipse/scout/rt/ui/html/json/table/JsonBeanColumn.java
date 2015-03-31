/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;

public class JsonBeanColumn<T extends IColumn<?>> extends JsonColumn<T> {

  public JsonBeanColumn(T model, IJsonSession jsonSession) {
    super(model, jsonSession);
  }

  @Override
  public String getObjectType() {
    return "BeanColumn";
  }

  @Override
  public Object cellValueToJson(Object value) {
    return JsonObjectUtility.javaToJson(value);
  }
}
