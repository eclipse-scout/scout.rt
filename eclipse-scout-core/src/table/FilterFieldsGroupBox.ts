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
import {ColumnUserFilter, FilterFieldsGroupBoxModel, GroupBox, InitModelOf, ObjectType, scout, SomeRequired, ValueField} from '../index';


export class FilterFieldsGroupBox extends GroupBox implements FilterFieldsGroupBoxModel {
  declare model: FilterFieldsGroupBoxModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'filter'>;

  filter: ColumnUserFilter;

  constructor() {
    super();
    this.gridColumnCount = 1;
    this.cssClass = 'filter-fields';
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.filter.addFilterFields(this);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this.filter.modifyFilterFields();
  }

  addFilterField<TValue>(objectType: ObjectType<ValueField<TValue>>, text: string): ValueField<TValue> {
    let field = scout.create(objectType, {
      parent: this,
      label: this.session.text(text),
      statusVisible: false,
      labelWidthInPixel: 50
    });
    this.addField0(field);
    return field;
  }

  // Info from awe, cgu: Added '0' to the name to avoid temporarily to avoid naming conflict with FormField#addField
  // This should be refactored in a future release
  addField0(field: ValueField<any>) {
    this.fields.push(field);
    this._prepareFields();
  }
}
