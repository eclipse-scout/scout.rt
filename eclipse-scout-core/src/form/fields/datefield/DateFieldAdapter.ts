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
import {arrays, DateFieldModel, dates, ParsingFailedStatus, RemoteEvent, ValueFieldAdapter} from '../../../index';
import {ValueFieldAcceptInputEvent} from '../ValueFieldEventMap';

export default class DateFieldAdapter extends ValueFieldAdapter {

  static PROPERTIES_ORDER = ['hasTime', 'hasDate'];

  protected override _initProperties(model: DateFieldModel) {
    super._initProperties(model);
  }

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
}