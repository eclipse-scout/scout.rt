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
import {CompositeFieldAdapter} from '../../../index';

export default class SplitBoxAdapter extends CompositeFieldAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['collapsibleField', 'fieldCollapsed', 'minSplitterPosition', 'fieldMinimized']);
  }

  _onWidgetPositionChange(event) {
    this._send('setSplitterPosition', {
      splitterPosition: event.position
    });
  }

  _onWidgetEvent(event) {
    if (event.type === 'positionChange') {
      this._onWidgetPositionChange(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
