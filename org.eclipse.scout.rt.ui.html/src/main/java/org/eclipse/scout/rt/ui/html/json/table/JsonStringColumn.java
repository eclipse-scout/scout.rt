/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.json.JSONObject;

public class JsonStringColumn<T extends IStringColumn> extends JsonColumn<T> {

  public JsonStringColumn(T model) {
    super(model);
  }

  /**
   * Use same object-type as Column, since StringColumn does not exist in JavaScript client.
   */
  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean isValueRequired() {
    return true;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("textWrap", getColumn().isTextWrap());
    return json;
  }
}
