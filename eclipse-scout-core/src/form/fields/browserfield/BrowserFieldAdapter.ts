/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BrowserField, Event, FormFieldAdapter} from '../../../index';
import {BrowserFieldExternalWindowStateChangeEvent, BrowserFieldMessageEvent} from './BrowserFieldEventMap';

export default class BrowserFieldAdapter extends FormFieldAdapter {
  declare widget: BrowserField;

  constructor() {
    super();
    this._addRemoteProperties(['location']);
  }

  protected _onWidgetMessage(event: BrowserFieldMessageEvent) {
    this._send('postMessage', {
      data: event.data,
      origin: event.origin
    });
  }

  protected _onWidgetExternalWindowStateChange(event: BrowserFieldExternalWindowStateChangeEvent) {
    this._send('externalWindowStateChange', {
      windowState: event.windowState
    });
  }

  protected override _onWidgetEvent(event: Event<BrowserField>) {
    if (event.type === 'message') {
      this._onWidgetMessage(event as BrowserFieldMessageEvent);
    } else if (event.type === 'externalWindowStateChange') {
      this._onWidgetExternalWindowStateChange(event as BrowserFieldExternalWindowStateChangeEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onModelPostMessage(event: any) {
    this.widget.postMessage(event.message, event.targetOrigin);
  }

  override onModelAction(event: any) {
    if (event.type === 'postMessage') {
      this._onModelPostMessage(event);
    } else {
      super.onModelAction(event);
    }
  }

  protected override _orderPropertyNamesOnSync(newProperties: Record<string, any>): string[] {
    // IE won't show scrollbars if the location is set before scrollBarEnabled is set to true.
    // Rendering the location again after setting the scrollBarEnabled property as done in IFrame.js doesn't seem to work.
    // It looks like the scrollBarEnabled property cannot be changed anymore once the location is set, even if location is unset and set again.
    return Object.keys(newProperties).sort(this._createPropertySortFunc(['scrollBarEnabled', 'location']));
  }
}
