/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, LookupFieldAdapter, RemoteLookupCall, scout, SmartField, SmartFieldAcceptByTextEvent, SmartFieldAcceptInputEvent, SmartFieldLookupResult, SmartFieldTouchPopup, strings} from '../../../index';

export class SmartFieldAdapter extends LookupFieldAdapter {
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

  /** @internal */
  override _postCreateWidget() {
    super._postCreateWidget();
    this.widget.lookupCall = scout.create(RemoteLookupCall, this);
  }

  protected _syncResult(result: SmartFieldLookupResult<any>) {
    let executedLookupCall = this.widget._currentLookupCall;
    if (!executedLookupCall && this.widget.touchMode && this.widget.popup && (this.widget.popup as SmartFieldTouchPopup<any>)._field) {
      // in case lookupCall is executed from within the popup (touch):
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
