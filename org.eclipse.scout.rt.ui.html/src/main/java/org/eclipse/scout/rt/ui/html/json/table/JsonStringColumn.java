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

import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;

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
}
