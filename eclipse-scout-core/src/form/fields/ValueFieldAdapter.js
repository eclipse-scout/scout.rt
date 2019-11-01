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
import {comparators} from '../../index';
import {FormFieldAdapter} from '../../index';

export default class ValueFieldAdapter extends FormFieldAdapter {

constructor() {
  super();
}


_onWidgetAcceptInput(event) {
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

_onWidgetEvent(event) {
  if (event.type === 'acceptInput') {
    this._onWidgetAcceptInput(event);
  } else {
    super._onWidgetEvent( event);
  }
}

/**
 * @override ModelAdapter.js
 */
exportAdapterData(adapterData) {
  adapterData = super.exportAdapterData( adapterData);
  delete adapterData.displayText;
  return adapterData;
}

_syncDisplayText(displayText) {
  this.widget.setDisplayText(displayText);
  this.widget.parseAndSetValue(displayText);
}

_createPropertySortFunc(order) {
  return function(a, b) {
    var ia = order.indexOf(a);
    var ib = order.indexOf(b);
    if (ia > -1 && ib > -1) { // both are in the list
      return ia - ib;
    }
    if (ia > -1) { // B is not in list
      return -1;
    }
    if (ib > -1) { // A is not in list
      return 1;
    }
    return comparators.TEXT.compare(a, b); // both are not in list
  };
}
}
