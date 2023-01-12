/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, arrays, DateField, dates, objects, ParsingFailedStatus, RemoteEvent, ValueFieldAcceptInputEvent, ValueFieldAdapter} from '../../../index';

export class DateFieldAdapter extends ValueFieldAdapter {

  static PROPERTIES_ORDER = ['hasTime', 'hasDate'];

  protected override _onWidgetAcceptInput(event: ValueFieldAcceptInputEvent<Date>) {
    let parsingFailedError = null;
    let errorStatus = this.widget.errorStatus;
    // Only send Parsing errors to the server
    if (errorStatus && errorStatus.hasChildren()) {
      parsingFailedError = arrays.find(errorStatus.asFlatList(), childStatus => {
        return childStatus instanceof ParsingFailedStatus;
      });
    }

    let data: any = {
      displayText: this.widget.displayText,
      errorStatus: parsingFailedError
    };
    // In case of an error, the value is still the old, valid value -> don't send it
    if (!parsingFailedError) {
      data.value = dates.toJsonDate(this.widget.value);
    }
    this._send('acceptInput', data, {
      showBusyIndicator: !event.whileTyping,
      coalesce: function(previous: RemoteEvent) {
        return this.target === previous.target && this.type === previous.type;
      }
    });
  }

  /**
   * Make sure hasDate and hasTime are always set before displayText, otherwise toggling hasDate and hasTime dynamically
   * won't work because renderDisplayText would try to write the time into the date field
   */
  protected override _orderPropertyNamesOnSync(newProperties: Record<string, any>): string[] {
    return Object.keys(newProperties).sort(this._createPropertySortFunc(DateFieldAdapter.PROPERTIES_ORDER));
  }

  static isDateAllowedRemote(date: Date): boolean {
    // @ts-expect-error
    if (!this.modelAdapter) {
      // @ts-expect-error
      return this.isDateAllowedOrig(date);
    }
    // Server will take care of it
    return true;
  }

  static modifyPrototype() {
    if (!App.get().remote) {
      return;
    }

    objects.replacePrototypeFunction(DateField, 'isDateAllowed', DateFieldAdapter.isDateAllowedRemote, true);
  }
}

App.addListener('bootstrap', DateFieldAdapter.modifyPrototype);
