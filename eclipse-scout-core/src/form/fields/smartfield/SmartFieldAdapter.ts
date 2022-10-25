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
import {Event, LookupFieldAdapter, RemoteLookupCall, scout, SmartField, SmartFieldTouchPopup, strings} from '../../../index';
import {SmartFieldAcceptByTextEvent, SmartFieldAcceptInputEvent} from './SmartFieldEventMap';
import {SmartFieldLookupResult} from './SmartField';

export default class SmartFieldAdapter extends LookupFieldAdapter {
  declare widget: SmartField<any>;

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

  protected override _postCreateWidget() {
    super._postCreateWidget();
    this.widget.lookupCall = scout.create(RemoteLookupCall, this);
  }

  protected _syncResult(result: SmartFieldLookupResult<any>) {
    // @ts-ignore
    let executedLookupCall = this.widget._currentLookupCall;
    // @ts-ignore
    if (!executedLookupCall && this.widget.touchMode && this.widget.popup && (this.widget.popup as SmartFieldTouchPopup<any>)._field) {
      // in case lookupCall is executed from within the popup (touch):
      // @ts-ignore
      executedLookupCall = ((this.widget.popup as SmartFieldTouchPopup<any>)._field as SmartField<any>)._currentLookupCall;
    }
    if (executedLookupCall) {
      (executedLookupCall as RemoteLookupCall<any>).resolveLookup(result);
    }
  }

  // When displayText comes from the server we must not call parseAndSetValue here.
  protected override _syncDisplayText(displayText: string) {
    this.widget.setDisplayText(displayText);
  }

  protected override _orderPropertyNamesOnSync(newProperties: Record<string, any>): string[] {
    return Object.keys(newProperties).sort(this._createPropertySortFunc(SmartFieldAdapter.PROPERTIES_ORDER));
  }

  protected override _onWidgetEvent(event: Event<SmartField<any>>) {
    if (event.type === 'acceptByText') {
      this._onWidgetAcceptByText(event as SmartFieldAcceptByTextEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onWidgetAcceptByText(event: SmartFieldAcceptByTextEvent) {
    this._sendProperty('errorStatus', event.errorStatus);
  }

  protected override _onWidgetAcceptInput(event: SmartFieldAcceptInputEvent) {
    let eventData: Partial<SmartFieldAcceptInputEvent> = {
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
