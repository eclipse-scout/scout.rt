/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

scout.DateFieldAdapter.modifyPrototype = function() {
  if (!scout.app.remote) {
    return;
  }

  scout.objects.replacePrototypeFunction(scout.DateField, '_parseValue', function(displayText) {
    if (this.modelAdapter) {
      // If the server reported an error for that display text, make sure it will be shown in the UI if the user enters that display text again
      if (this.modelAdapter._errorStatus && displayText === this.modelAdapter._errorStatusDisplayText) {
        throw this.modelAdapter._errorStatus;
      }
      return this._parseValueOrig(displayText);
    } else {
      return this._parseValueOrig(displayText);
    }
  }, true);
};

scout.addAppListener('bootstrap', scout.DateFieldAdapter.modifyPrototype);
