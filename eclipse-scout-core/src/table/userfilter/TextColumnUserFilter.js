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
import {ColumnUserFilter, scout, strings} from '../../index';
import $ from 'jquery';

export default class TextColumnUserFilter extends ColumnUserFilter {

  constructor() {
    super();

    this.freeText = null;
    this.freeTextField = null;
    this.hasFilterFields = true;
  }

  /**
   * @override ColumnUserFilter.js
   */
  createFilterAddedEventData() {
    let data = super.createFilterAddedEventData();
    data.freeText = this.freeText;
    return data;
  }

  /**
   * @override ColumnUserFilter.js
   */
  fieldsFilterActive() {
    return strings.hasText(this.freeText);
  }

  /**
   * @override ColumnUserFilter.js
   */
  acceptByFields(key, normKey, row) {
    let filterFieldText = strings.nvl(this.freeText).toLowerCase(),
      rowText = strings.nvl(this.column.cellTextForTextFilter(row)).toLowerCase();
    return rowText.indexOf(filterFieldText) > -1;
  }

  /**
   * @override
   */
  filterFieldsTitle() {
    return this.session.text('ui.FreeText');
  }

  /**
   * @override ColumnUserFilter.js
   */
  addFilterFields(groupBox) {
    this.freeTextField = scout.create('StringField', {
      parent: groupBox,
      labelVisible: false,
      statusVisible: false,
      maxLength: 100,
      displayText: this.freeText,
      updateDisplayTextOnModify: true
    });
    this.freeTextField.on('acceptInput', this._onAcceptInput.bind(this));
    groupBox.addField0(this.freeTextField);
  }

  _onAcceptInput(event) {
    this.freeText = this.freeTextField.$field.val().trim();
    $.log.isDebugEnabled() && $.log.debug('(TextColumnUserFilter#_onAcceptInput) freeText=' + this.freeText);
    this.triggerFilterFieldsChanged(event);
  }

  /**
   * @override ColumnUserFilter.js
   */
  modifyFilterFields() {
    this.freeTextField.removeMandatoryIndicator();
  }
}
