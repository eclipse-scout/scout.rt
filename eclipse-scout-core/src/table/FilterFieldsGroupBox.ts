/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {GroupBox, scout} from '../index';

export default class FilterFieldsGroupBox extends GroupBox {

  constructor() {
    super();
    this.gridColumnCount = 1;
    this.cssClass = 'filter-fields';
  }

  _init(model) {
    super._init(model);
    this.filter.addFilterFields(this);
  }

  /**
   * @override GroupBox.js
   */
  _renderProperties($parent) {
    super._renderProperties($parent);
    this.filter.modifyFilterFields();
  }

  addFilterField(objectType, text) {
    let field = scout.create(objectType, {
      parent: this,
      label: this.session.text(text),
      statusVisible: false,
      labelWidthInPixel: 50,
      maxLength: 100,
      updateDisplayTextOnModify: true
    });
    this.addField0(field);
    return field;
  }

  // Info from awe, cgu: Added '0' to the name to avoid temporarily to avoid naming conflict with FormField#addField
  // This should be refactored in a future release
  addField0(field) {
    this.fields.push(field);
    this._prepareFields();
  }
}
