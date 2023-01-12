/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormFieldMenu, GridData, MenuAdapter} from '../../../index';

export class FormFieldMenuAdapter extends MenuAdapter {
  declare widget: FormFieldMenu;

  /** @internal */
  override _postCreateWidget() {
    super._postCreateWidget();
    // Use grid data from server as hints
    if (this.widget.field) {
      this.widget.field.gridDataHints = new GridData(this.widget.field.gridData);
    }
  }
}
