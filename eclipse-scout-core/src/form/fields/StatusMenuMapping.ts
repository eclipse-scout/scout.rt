/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Menu, ObjectOrChildModel, SomeRequired, StatusMenuMappingModel, StatusSeverity, Widget} from '../../index';

export class StatusMenuMapping extends Widget implements StatusMenuMappingModel {
  declare model: StatusMenuMappingModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'menu'>;

  codes: number[];
  severities: StatusSeverity[];
  menu: Menu;

  constructor() {
    super();
    this.codes = [];
    this.severities = [];
    this.menu = null;
    this._addWidgetProperties(['menu']);
  }

  /** @internal */
  override _createChild<T extends Widget>(model: ObjectOrChildModel<T> | string): T {
    if (typeof model === 'string') {
      // If the model is a string it is probably the id of the menu.
      // Menus are defined by the parent (form field) -> search the parent's children for the menu
      let existingWidget = this.parent.widget(model);
      if (!existingWidget) {
        throw new Error('Referenced widget not found: ' + model);
      }
      return existingWidget as T;
    }
    return super._createChild(model);
  }
}
