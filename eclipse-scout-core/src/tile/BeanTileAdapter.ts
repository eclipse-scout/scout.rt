/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BeanTile, BeanTileAppLinkActionEvent, Event, TileAdapter} from '../index';

export class BeanTileAdapter extends TileAdapter {
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
