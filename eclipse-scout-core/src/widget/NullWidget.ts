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
import {NullWidgetEventMap, NullWidgetModel, RefModel, Widget, WidgetModel} from '../index';

export default class NullWidget extends Widget implements NullWidgetModel {
  declare model: NullWidgetModel;
  declare eventMap: NullWidgetEventMap;

  childWidget: Widget;

  constructor() {
    super();
    this.childWidget = null;
    this._addWidgetProperties(['childWidget']);
  }

  setChildWidget(childWidget: Widget | RefModel<WidgetModel>) {
    this.setProperty('childWidget', childWidget);
  }
}
