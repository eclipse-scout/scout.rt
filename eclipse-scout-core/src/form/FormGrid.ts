/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, GridData, LogicalGrid} from '../index';

/**
 * Dummy grid for the form which actually just creates the actual grid data for the root group box from the hints.
 */
export class FormGrid extends LogicalGrid {
  protected _validate(form: Form) {
    if (!form.rootGroupBox) {
      return;
    }
    // The form does not have a real logical grid but needs the gridData anyway (widthInPixel, heightInPixel, see GroupBoxLayout).
    // Grid.w is not relevant for the form, no need to pass a gridColumnCount
    form.rootGroupBox._setGridData(GridData.createFromHints(form.rootGroupBox));
  }
}
