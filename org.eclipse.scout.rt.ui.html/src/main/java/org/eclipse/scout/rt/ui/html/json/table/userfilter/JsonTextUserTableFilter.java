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
package org.eclipse.scout.rt.ui.html.json.table.userfilter;

import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TextUserTableFilter;
import org.json.JSONObject;

public class JsonTextUserTableFilter<T extends TextUserTableFilter> extends JsonUserTableFilter<T> {

  public JsonTextUserTableFilter(T filter) {
    super(filter);
  }

  @Override
  public String getObjectType() {
    return "TextUserTableFilter";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("text", getFilter().getText());
    return json;
  }

}
