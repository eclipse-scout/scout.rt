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
