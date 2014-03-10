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
package org.eclipse.scout.rt.ui.json;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.json.JSONObject;

public class JsonDesktopTable extends AbstractJsonPropertyObserverRenderer<ITable> {

  public JsonDesktopTable(ITable modelObject, IJsonSession jsonSession) {
    super(modelObject, jsonSession);
  }

  @Override
  public JSONObject toJson() throws JsonUIException {
    return null;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) throws JsonUIException {
  }

}
