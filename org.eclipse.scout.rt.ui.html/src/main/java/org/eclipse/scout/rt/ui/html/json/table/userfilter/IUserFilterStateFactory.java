/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table.userfilter;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.json.JSONObject;

@ApplicationScoped
public interface IUserFilterStateFactory {
  /**
   * Creates a user filter state from the JSON object.
   */
  IUserFilterState createUserFilterState(JsonTable<? extends ITable> table, JSONObject object);
}
