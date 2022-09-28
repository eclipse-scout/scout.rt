/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Form, GridData, LogicalGrid} from '../index';

/**
 * Dummy grid for the form which actually just creates the actual grid data for the root group box from the hints.
 */
export default class FormGrid extends LogicalGrid {

  constructor() {
    super();
  }

  protected _validate(form: Form) {
    // The form does not have a real logical grid but needs the gridData anyway (widthInPixel, heightInPixel, see GroupBoxLayout).
    // Grid.w is not relevant for the form, no need to pass a gridColumnCount
    form.rootGroupBox.gridData = GridData.createFromHints(form.rootGroupBox);
  }
}
