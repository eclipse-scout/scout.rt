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

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.json.JSONObject;

public class JsonNumberColumn<NUMBER_COLUMN extends INumberColumn<?>> extends JsonColumn<NUMBER_COLUMN> {

  public JsonNumberColumn(NUMBER_COLUMN model) {
    super(model);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("format", ((INumberColumn) getColumn()).getFormat().toPattern());
    json.put("aggregationFunction", ((INumberColumn) getColumn()).getAggregationFunction());
    return json;
  }

  @Override
  public Object cellValueToJson(Object value) {
    return value;
  }

  @Override
  protected String computeColumnType(IColumn column) {
    return "number";
  }
}
