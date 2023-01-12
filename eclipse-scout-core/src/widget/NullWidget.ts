/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {NullWidgetEventMap, NullWidgetModel, ObjectOrChildModel, Widget} from '../index';

export class NullWidget extends Widget implements NullWidgetModel {
  declare model: NullWidgetModel;
  declare eventMap: NullWidgetEventMap;
  declare self: NullWidget;

  childWidget: Widget | Widget[];

  constructor() {
    super();
    this.childWidget = null;
    this._addWidgetProperties(['childWidget']);
  }

  setChildWidget(childWidget: ObjectOrChildModel<Widget> | ObjectOrChildModel<Widget>[]) {
    this.setProperty('childWidget', childWidget);
  }
}
