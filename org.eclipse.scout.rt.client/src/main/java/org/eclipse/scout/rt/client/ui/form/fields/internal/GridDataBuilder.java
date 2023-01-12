/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.internal;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public final class GridDataBuilder {
  private GridDataBuilder() {
  }

  public static GridData createFromHints(IFormField f, int gridColumnCount) {
    GridData data = new GridData(f.getGridDataHints());
    if (data.w == IFormField.FULL_WIDTH) {
      data.w = gridColumnCount;
    }
    return data;
  }
}
