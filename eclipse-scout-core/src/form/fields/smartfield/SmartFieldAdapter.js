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
import {LookupFieldAdapter, scout, strings} from '../../../index';

export default class SmartFieldAdapter extends LookupFieldAdapter {

  constructor() {
    super();

    this._addRemoteProperties(['activeFilter']);
  }

  /**
   * Property lookup-row must be handled before value, since the smart-field has either a lookup-row
   * or a value but never both (when we only have a value, the smart-field must perform a lookup by key
   * in order to resolve the display name for that value).
   * <br>
   * Intentionally don't re-use properties from super-classes.
   */
  static PROPERTIES_ORDER = ['lookupRow', 'value', 'errorStatus', 'displayText'];

  _postCreateWidget() {
    super._postCreateWidget();
    this.widget.lookupCall = scout.create('RemoteLookupCall', this);
  }

  _syncResult(result) {
    let executedLookupCall = this.widget._currentLookupCall;
    if (!executedLookupCall && this.widget.touchMode && this.widget.popup && this.widget.popup._field) {
      // in case lookupCall is executed from within the popup (touch):
      executedLookupCall = this.widget.popup._field._currentLookupCall;
    }
    if (executedLookupCall) {
      executedLookupCall.resolveLookup(result);
    }
  }

  // When displayText comes from the server we must not call parseAndSetValue here.
  _syncDisplayText(displayText) {
    this.widget.setDisplayText(displayText);
  }

  _orderPropertyNamesOnSync(newProperties) {
    return Object.keys(newProperties).sort(this._createPropertySortFunc(SmartFieldAdapter.PROPERTIES_ORDER));
  }

  _onWidgetEvent(event) {
    if (event.type === 'acceptByText') {
      this._onWidgetAcceptByText(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  _onWidgetAcceptByText(event) {
    this._sendProperty('errorStatus', event.errorStatus);
  }

  _onWidgetAcceptInput(event) {
    let eventData = {
      displayText: event.displayText,
      errorStatus: event.errorStatus
    };

    if (event.errorStatus) {
      // 'clear' case
      if (strings.empty(event.displayText)) {
        eventData.value = null;
      }
    } else {
      eventData.value = event.value;
      if (event.acceptByLookupRow) {
        eventData.lookupRow = event.lookupRow;
      }
    }

    this._send('acceptInput', eventData, {
      showBusyIndicator: !event.whileTyping,
      coalesce: function(previous) {
        return this.target === previous.target && this.type === previous.type && this.whileTyping === previous.whileTyping;
      }
    });
  }
}
