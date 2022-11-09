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
import {CompositeFieldAdapter, Event, SplitBox, SplitBoxPositionChangeEvent} from '../../../index';

export class SplitBoxAdapter extends CompositeFieldAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['collapsibleField', 'fieldCollapsed', 'minSplitterPosition', 'fieldMinimized']);
  }

  protected _onWidgetPositionChange(event: SplitBoxPositionChangeEvent) {
    this._send('setSplitterPosition', {
      splitterPosition: event.position
    });
  }

  protected override _onWidgetEvent(event: Event<SplitBox>) {
    if (event.type === 'positionChange') {
      this._onWidgetPositionChange(event as SplitBoxPositionChangeEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
