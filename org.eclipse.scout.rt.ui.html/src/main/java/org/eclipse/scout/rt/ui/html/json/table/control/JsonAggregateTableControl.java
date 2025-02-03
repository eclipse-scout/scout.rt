/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table.control;

import org.eclipse.scout.rt.client.ui.basic.table.controls.IAggregateTableControl;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

public class JsonAggregateTableControl<TABLE_CONTROL extends IAggregateTableControl> extends JsonTableControl<TABLE_CONTROL> {

  public JsonAggregateTableControl(TABLE_CONTROL model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "AggregateTableControl";
  }
}
