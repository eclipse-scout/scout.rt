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

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableTextUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.IUserFilterStateFactory;
import org.json.JSONObject;

@Order(5500)
public class UserFilterStateFactory implements IUserFilterStateFactory {

  @Override
  public IUserFilterState createUserFilterState(JsonTable<? extends ITable> table, JSONObject data) {
    String filterType = data.getString("filterType");
    if ("column".equals(filterType)) {
      JsonColumn jsonColumn = table.extractJsonColumn(data);
      return jsonColumn.createFilterStateFromJson(data);
    }
    if ("text".equals(filterType)) {
      String text = data.getString("text");
      return new TableTextUserFilterState(text);
    }
    return null;
  }
}
