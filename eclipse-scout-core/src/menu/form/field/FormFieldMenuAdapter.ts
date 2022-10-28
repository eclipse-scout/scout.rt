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
import {FormFieldMenu, GridData, MenuAdapter} from '../../../index';

export default class FormFieldMenuAdapter extends MenuAdapter {
  declare widget: FormFieldMenu;

  constructor() {
    super();
  }

  /** @internal */
  override _postCreateWidget() {
    super._postCreateWidget();
    // Use grid data from server as hints
    if (this.widget.field) {
      this.widget.field.gridDataHints = new GridData(this.widget.field.gridData);
    }
  }
}
