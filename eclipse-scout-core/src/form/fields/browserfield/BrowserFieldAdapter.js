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
import {ValueFieldAdapter} from '../../../index';

export default class BrowserFieldAdapter extends ValueFieldAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['location']);
  }

  _onWidgetMessage(event) {
    this._send('postMessage', {
      data: event.data,
      origin: event.origin
    });
  }

  _onWidgetExternalWindowStateChange(event) {
    this._send('externalWindowStateChange', {
      windowState: event.windowState
    });
  }

  _onWidgetEvent(event) {
    if (event.type === 'message') {
      this._onWidgetMessage(event);
    } else if (event.type === 'externalWindowStateChange') {
      this._onWidgetExternalWindowStateChange(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  _onModelPostMessage(event) {
    this.widget.postMessage(event.message, event.targetOrigin);
  }

  onModelAction(event) {
    if (event.type === 'postMessage') {
      this._onModelPostMessage(event);
    } else {
      super.onModelAction(event);
    }
  }

  _orderPropertyNamesOnSync(newProperties) {
    // IE won't show scrollbars if the location is set before scrollBarEnabled is set to true.
    // Rendering the location again after setting the scrollBarEnabled property as done in IFrame.js doesn't seem to work.
    // It looks like the scrollBarEnabled property cannot be changed anymore once the location is set, even if location is unset and set again.
    return Object.keys(newProperties).sort(this._createPropertySortFunc(['scrollBarEnabled', 'location']));
  }
}
