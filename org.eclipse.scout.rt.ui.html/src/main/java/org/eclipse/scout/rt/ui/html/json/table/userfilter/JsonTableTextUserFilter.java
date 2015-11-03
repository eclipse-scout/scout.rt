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
package org.eclipse.scout.rt.ui.html.json.table.userfilter;

import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableTextUserFilterState;
import org.json.JSONObject;

public class JsonTableTextUserFilter<T extends TableTextUserFilterState> extends JsonTableUserFilter<T> {

  public JsonTableTextUserFilter(T filter) {
    super(filter);
  }

  @Override
  public String getObjectType() {
    return "TableTextUserFilter";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("text", getFilterState().getText());
    return json;
  }

}
