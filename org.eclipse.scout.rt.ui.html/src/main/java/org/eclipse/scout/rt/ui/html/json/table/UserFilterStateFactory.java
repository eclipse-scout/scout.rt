/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
