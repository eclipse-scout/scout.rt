/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ColumnUserFilter, FilterFieldsGroupBox, scout, StringField, strings, TableRow, TableUserFilterAddedEventData, TextColumnUserFilterModel, ValueFieldAcceptInputEvent} from '../../index';
import $ from 'jquery';

export class TextColumnUserFilter extends ColumnUserFilter {
  declare model: TextColumnUserFilterModel;

  freeText: string;
  freeTextField: StringField;

  constructor() {
    super();

    this.freeText = null;
    this.freeTextField = null;
    this.hasFilterFields = true;
  }

  override createFilterAddedEventData(): TableUserFilterAddedEventData {
    let data = super.createFilterAddedEventData();
    data.freeText = this.freeText;
    return data;
  }

  override fieldsFilterActive(): boolean {
    return strings.hasText(this.freeText);
  }

  override acceptByFields(key: any, normKey: number | string, row: TableRow): boolean {
    let filterFieldText = strings.nvl(this.freeText).toLowerCase(),
      rowText = strings.nvl(this.column.cellTextForTextFilter(row)).toLowerCase();
    return rowText.indexOf(filterFieldText) > -1;
  }

  override filterFieldsTitle(): string {
    return this.session.text('ui.FreeText');
  }

  override addFilterFields(groupBox: FilterFieldsGroupBox) {
    this.freeTextField = scout.create(StringField, {
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

  protected _onAcceptInput(event: ValueFieldAcceptInputEvent<string>) {
    let val = this.freeTextField.$field.val() as string;
    this.freeText = val.trim();
    $.log.isDebugEnabled() && $.log.debug('(TextColumnUserFilter#_onAcceptInput) freeText=' + this.freeText);
    this.triggerFilterFieldsChanged();
  }

  override modifyFilterFields() {
    this.freeTextField.removeMandatoryIndicator();
  }
}
