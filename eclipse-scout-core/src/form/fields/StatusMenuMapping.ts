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
import {Widget} from '../../index';

export default class StatusMenuMapping extends Widget {

  constructor() {
    super();
    this.codes = [];
    this.severities = [];
    this.menu = null;
    this._addWidgetProperties(['menu']);
  }

  _createChild(model) {
    if (typeof model === 'string') {
      // If the model is a string it is probably the id of the menu.
      // Menus are defined by the parent (form field) -> search the parent's children for the menu
      let existingWidget = this.parent.widget(model);
      if (!existingWidget) {
        throw new Error('Referenced widget not found: ' + model);
      }
      return existingWidget;
    }
    return super._createChild(model);
  }
}
