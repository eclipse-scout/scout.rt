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
import {BeanTile, Event, TileAdapter} from '../index';
import {BeanTileAppLinkActionEvent} from './BeanTile';

export default class BeanTileAdapter extends TileAdapter {
  protected _onWidgetAppLinkAction(event: BeanTileAppLinkActionEvent) {
    this._send('appLinkAction', {
      ref: event.ref
    });
  }

  protected override _onWidgetEvent(event: Event<BeanTile>) {
    if (event.type === 'appLinkAction') {
      this._onWidgetAppLinkAction(event as BeanTileAppLinkActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
