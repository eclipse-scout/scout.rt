/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {GridData, MenuAdapter} from '../../../index';

export default class FormFieldMenuAdapter extends MenuAdapter {

  constructor() {
    super();
  }

  /**
   * @override
   */
  _postCreateWidget() {
    super._postCreateWidget();
    // Use grid data from server as hints
    if (this.widget.field) {
      this.widget.field.gridDataHints = new GridData(this.widget.field.gridData);
    }
  }
}
