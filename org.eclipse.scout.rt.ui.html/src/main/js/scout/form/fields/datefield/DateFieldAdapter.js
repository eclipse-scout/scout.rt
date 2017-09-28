/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.DateFieldAdapter = function() {
  scout.DateFieldAdapter.parent.call(this);
  this._errorStatus = null;
  this._errorStatusDisplayText = null;
};
scout.inherits(scout.DateFieldAdapter, scout.ValueFieldAdapter);

/**
 * @override
 */
/**
 * @override
 */
scout.DateFieldAdapter.prototype._init = function(model) {
  scout.DateFieldAdapter.parent.prototype._init.call(this, model);
  this._errorStatus = scout.Status.ensure(model.errorStatus);
  if (this._errorStatus) {
    this._errorStatusDisplayText = model.displayText;
  }
};

/**
 * @override
 */
scout.DateFieldAdapter.prototype._attachWidget = function() {
  scout.DateFieldAdapter.parent.prototype._attachWidget.call(this);
  this.widget.setParser(function(displayText) {
    // If the server reported an error for that display text, make sure it will be shown in the UI if the user enters that display text again
    if (this.modelAdapter._errorStatus && displayText === this.modelAdapter._errorStatusDisplayText) {
      throw this.modelAdapter._errorStatus;
    }
    return this._parseValue(displayText);
  }.bind(this.widget));
};

/**
 * @override
 */
scout.DateFieldAdapter.prototype._onWidgetAcceptInput = function(event) {
  var data = {
      displayText: this.widget.displayText,
      errorStatus: this.widget.errorStatus
    };
  // In case of an error, the value is still the old, valid value -> don't send it
  if (!this.widget.errorStatus) {
    data.value = scout.dates.toJsonDate(this.widget.value);
  }
  this._send('acceptInput', data, {
    showBusyIndicator: !event.whileTyping,
    coalesce: function(previous) {
      return this.target === previous.target && this.type === previous.type;
    }
  });
  this._errorStatus = null;
  this._errorStatusDisplayText = null;
};

/**
 * @override
 */
scout.DateFieldAdapter.prototype._syncDisplayText = function(displayText) {
  // No need to call parseAndSetValue, the value will come separately
  this.widget.setDisplayText(displayText);

  if (this._errorStatus) {
    this._errorStatusDisplayText = displayText;
  }
};

/**
 * @override
 */
scout.DateFieldAdapter.prototype._orderPropertyNamesOnSync = function(newProperties) {
  return Object.keys(newProperties).sort(function(a, b) {
    if (a === 'hasDate' || a === 'hasTime') {
      // make sure hasDate and hasTime are always set before displayText,
      // otherwise toggling hasDate and hasTime dynamically won't work because renderDisplayText would try to write the time into the date field
      return -1;
    }
    return 1;
  });
};

scout.DateFieldAdapter.prototype._syncErrorStatus = function(errorStatus) {
  if (errorStatus) {
    if (this.widget.errorStatus) {
      // Don't loose information which part was invalid
      errorStatus.invalidDate = this.widget.errorStatus.invalidDate;
      errorStatus.invalidTime = this.widget.errorStatus.invalidTime;
    } else {
      // If the error happened only on server side, we do not know which part was invalid.
      // Set both to true so that DateField._isDateValid / isTimeValid does not return true
      errorStatus.invalidDate = true;
      errorStatus.invalidTime = true;
    }
  }

  // Remember errorStatus from model
  this._errorStatus = scout.Status.ensure(errorStatus);
  this._errorStatusDisplayText = this.widget.displayText;
  this.widget.setErrorStatus(errorStatus);
};
