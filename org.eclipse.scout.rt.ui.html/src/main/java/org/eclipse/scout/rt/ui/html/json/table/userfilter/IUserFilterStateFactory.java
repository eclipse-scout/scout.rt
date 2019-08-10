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
