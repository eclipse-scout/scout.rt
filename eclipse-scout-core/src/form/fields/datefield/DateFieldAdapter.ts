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
import {App, arrays, DateField, dates, objects, ParsingFailedStatus, ValueFieldAdapter} from '../../../index';

export default class DateFieldAdapter extends ValueFieldAdapter {

  constructor() {
    super();
  }

  static PROPERTIES_ORDER = ['hasTime', 'hasDate'];

  /**
   * @override
   */
  _onWidgetAcceptInput(event) {
    let parsingFailedError = null;
    let errorStatus = this.widget.errorStatus;
    // Only send Parsing errors to the server
    if (errorStatus && errorStatus.hasChildren()) {
      parsingFailedError = arrays.find(errorStatus.asFlatList(), childStatus => {
        return childStatus instanceof ParsingFailedStatus;
      });
    }

    let data = {
      displayText: this.widget.displayText,
      errorStatus: parsingFailedError
    };
    // In case of an error, the value is still the old, valid value -> don't send it
    if (!parsingFailedError) {
      data.value = dates.toJsonDate(this.widget.value);
    }
    this._send('acceptInput', data, {
      showBusyIndicator: !event.whileTyping,
      coalesce: function(previous) {
        return this.target === previous.target && this.type === previous.type;
      }
    });
  }

  /**
   * Make sure hasDate and hasTime are always set before displayText, otherwise toggling hasDate and hasTime dynamically
   * won't work because renderDisplayText would try to write the time into the date field
   *
   * @override
   */
  _orderPropertyNamesOnSync(newProperties) {
    return Object.keys(newProperties).sort(this._createPropertySortFunc(DateFieldAdapter.PROPERTIES_ORDER));
  }

  static isDateAllowedRemote(date) {
    if (!this.modelAdapter) {
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
