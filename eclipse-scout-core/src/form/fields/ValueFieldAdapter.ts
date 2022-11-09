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
