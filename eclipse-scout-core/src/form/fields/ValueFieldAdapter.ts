/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AdapterData, Event, FormFieldAdapter, ValueField, ValueFieldAcceptInputEvent} from '../../index';

export class ValueFieldAdapter extends FormFieldAdapter {
  declare widget: ValueField<any>;

  protected _onWidgetAcceptInput(event: ValueFieldAcceptInputEvent) {
    this._send('acceptInput', {
      displayText: event.displayText,
      whileTyping: event.whileTyping
    }, {
      showBusyIndicator: !event.whileTyping,
      coalesce: function(previous) {
        return this.target === previous.target && this.type === previous.type && this.whileTyping === previous.whileTyping;
      }
    });
  }

  protected override _onWidgetEvent(event: Event<ValueField<any>>) {
    if (event.type === 'acceptInput') {
      this._onWidgetAcceptInput(event as ValueFieldAcceptInputEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  override exportAdapterData(adapterData: AdapterData): AdapterData {
    adapterData = super.exportAdapterData(adapterData);
    delete adapterData.displayText;
    return adapterData;
  }

  protected _syncDisplayText(displayText: string) {
    this.widget.setDisplayText(displayText);
    this.widget.parseAndSetValue(displayText);
  }
}
