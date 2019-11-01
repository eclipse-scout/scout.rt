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
import {MessageBoxes} from '../index';
import {objects} from '../index';
import {Status} from '../index';
import {EventSupport} from '../index';
import {MessageBox} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';

/**
 * Abstract base class for validation lifecycles as used for forms.
 * A subclass must set the properties, in order to display messages:
 * - emptyMandatoryElementsTextKey
 * - invalidElementsTextKey
 * - saveChangesQuestionTextKey
 *
 * @class
 * @constructor
 * @abstract
 */
export default class Lifecycle {

constructor() {
  this.emptyMandatoryElementsTextKey = null;
  this.emptyMandatoryElementsText = null;

  this.invalidElementsTextKey = null;
  this.invalidElementsText = null;

  this.saveChangesQuestionTextKey = null;
  this.askIfNeedSave = true;
  this.askIfNeedSaveText = null; // Java: cancelVerificationText

  this.events = new EventSupport();
  this.handlers = {
    'load': this._defaultLoad.bind(this),
    'save': this._defaultSave.bind(this)
  };
}

// Info: doExportXml, doImportXml, doSaveWithoutMarkerChange is not supported in Html UI

init(model) {
  scout.assertParameter('widget', model.widget);
  $.extend(this, model);
  if (objects.isNullOrUndefined(this.emptyMandatoryElementsText)) {
    this.emptyMandatoryElementsText = this.session().text(this.emptyMandatoryElementsTextKey);
  }
  if (objects.isNullOrUndefined(this.invalidElementsText)) {
    this.invalidElementsText = this.session().text(this.invalidElementsTextKey);
  }
  if (objects.isNullOrUndefined(this.askIfNeedSaveText)) {
    this.askIfNeedSaveText = this.session().text(this.saveChangesQuestionTextKey);
  }
}

load() {
  return this._load().then(function() {
    this.markAsSaved();
    this.events.trigger('postLoad');
  }.bind(this));
}

/**
 * @returns {Promise}
 */
_load() {
  return this.handlers.load()
    .then(function(status) {
      this.events.trigger('load');
    }.bind(this));
}

/**
 * @returns {Promise}
 */
_defaultLoad() {
  return $.resolvedPromise();
}

/**
 * @returns {Promise}
 */
ok() {
  // 1.) validate form
  return this._whenInvalid(this._validate)
    .then(function(invalid) {
      if (invalid) {
        return;
      }

      // 2.) check if save is required
      if (!this.requiresSave()) {
        return this.close();
      }

      // 3.) perform save operation
      return this._whenInvalid(this._save)
        .then(function(invalid) {
          if (invalid) {
            return;
          }

          this.markAsSaved();
          return this.close();
        }.bind(this));
    }.bind(this));
}

/**
 * @returns {Promise}
 */
cancel() {
  var showMessageBox = this.requiresSave() && this.askIfNeedSave;
  if (showMessageBox) {
    return this._showYesNoCancelMessageBox(
      this.askIfNeedSaveText,
      this.ok.bind(this),
      this.close.bind(this));
  } else {
    return this.close();
  }
}

/**
 * @returns {Promise}
 */
reset() {
  this._reset();

  // reload the state
  return this.load().then(function() {
    this.events.trigger('reset');
  }.bind(this));
}

/**
 * @returns {Promise}
 */
close() {
  return this._close();
}

/**
 * @returns {Promise}
 */
_close() {
  this.events.trigger('close');
  return $.resolvedPromise();
}

/**
 * @returns {Promise}
 */
save() {
  // 1.) validate form
  return this._whenInvalid(this._validate)
    .then(function(invalid) {

      // 2.) invalid or form has not been changed
      if (invalid || !this.requiresSave()) {
        return;
      }

      // 3.) perform save operation
      return this._whenInvalid(this._save)
        .then(function(invalid) {
          if (invalid) {
            return;
          }

          this.markAsSaved();
        }.bind(this));
    }.bind(this));
}

/**
 * @returns {Promise}
 */
_save() {
  return this.handlers.save()
    .then(function(status) {
      this.events.trigger('save');
      return status;
    }.bind(this));
}

/**
 * @returns {Promise}
 */
_defaultSave() {
  return $.resolvedPromise();
}

markAsSaved() {
  // NOP
}

/**
 * Override this function to check if any data has changed and saving is required.
 * @returns {boolean}
 */
requiresSave() {
  return false;
}

setAskIfNeedSave(askIfNeedSave) {
  this.askIfNeedSave = askIfNeedSave;
}

/**
 * Helper function to deal with functions that return a Status object.
 * Makes it easier to return early when that function returns an invalid status (= less code to write).
 *
 * @returns {Promise} If the resulting promise is resolved with "true", the life cycle is considered invalid.
 *                    Otherwise, the life cycle is considered valid and the store/save operation continues.
 *                    If the status returned by 'func' is absent or Status.Severity.OK, a promise resolved with
 *                    "false" is returned. Otherwise, the promise returned by _showStatusMessageBox() is returned.
 */
_whenInvalid(func) {
  return func.call(this)
    .then(function(status) {
      if (!status || status.severity === Status.Severity.OK) {
        return $.resolvedPromise(false); // invalid=false
      }
      return this._showStatusMessageBox(status);
    }.bind(this))
    .catch(function(error) {
      return this._showStatusMessageBox(errorToStatus(error));
    }.bind(this));

  // See ValueField#_createInvalidValueStatus, has similar code to transfor error to status
  function errorToStatus(error) {
    if (error instanceof Status) {
      return error;
    }
    if (typeof error === 'string') {
      return Status.error({
        message: error
      });
    }
    return Status.error({
      message: error.message
    });
  }
}

_showYesNoCancelMessageBox(message, yesAction, noAction) {
  return MessageBoxes.create(this.widget)
    .withSeverity(Status.Severity.WARNING)
    .withHeader(message)
    .withYes()
    .withNo()
    .withCancel()
    .buildAndOpen()
    .then(function(option) {
      if (option === MessageBox.Buttons.YES) {
        return yesAction();
      } else if (option === MessageBox.Buttons.NO) {
        return noAction();
      }
      return $.resolvedPromise();
    });
}

/**
 * @param status
 * @returns {Promise} If the resulting promise is resolved with "true", the life cycle is considered invalid.
 *                    Otherwise, the life cycle is considered valid and the store/save operation continues.
 *                    By default, a promise that is resolved with "true" is returned.
 */
_showStatusMessageBox(status) {
  return MessageBoxes.createOk(this.widget)
    .withSeverity(status.severity)
    .withBody(status.message, true)
    .buildAndOpen()
    .then(function(option) {
      var invalid = (status.severity === Status.Severity.ERROR);
      return $.resolvedPromise(invalid);
    });
}

/**
 * @returns {Promise}
 */
_validate() {
  var status = this._validateElements();
  if (status.isValid()) {
    status = this._validateWidget();
  }
  return $.resolvedPromise(status);
}

/**
 * Validates all elements (i.e form-fields) covered by the lifecycle and checks for missing or invalid elements.
 *
 * @return Status
 */
_validateElements() {
  var elements = this._invalidElements();
  var status = new Status();
  if (elements.missingElements.length === 0 && elements.invalidElements.length === 0) {
    status.severity = Status.Severity.OK;
  } else {
    status.severity = Status.Severity.ERROR;
    status.message = this._createInvalidElementsMessageHtml(elements.missingElements, elements.invalidElements);
  }
  return status;
}

/**
 * Validates the widget (i.e. form) associated with this lifecycle. This function is only called when there are
 * no missing or invalid elements. It is used to implement an overall-validate logic which has nothing to do
 * with a specific element or field. For instance you could validate if an internal member variable of a Lifecycle
 * or Form is set.
 *
 * @return Status
 */
_validateWidget() {
  return Status.ok();
}

/**
 * Override this function to check for invalid elements on the parent which prevent
 * saving of the parent.(eg. check if all mandatory elements contain a value)
 *
 * @returns Object with
 * valid: (true or false)
 * missingElements: Elements which should have a value
 * invalidElements: Elements which have an invalid value
 */
_invalidElements() {
  return {
    missingElements: [],
    invalidElements: []
  };
}

/**
 * Creates a HTML message used to display missing and invalid fields in a message box.
 */
_createInvalidElementsMessageHtml(missing, invalid) {
  var $div = $('<div>'),
    hasMissing = missing.length > 0,
    hasInvalid = invalid.length > 0;
  if (hasMissing) {
    appendTitleAndList.call(this, $div, this.emptyMandatoryElementsText, missing, this._missingElementText);
  }
  if (hasMissing && hasInvalid) {
    $div.appendElement('<br>');
  }
  if (hasInvalid) {
    appendTitleAndList.call(this, $div, this.invalidElementsText, invalid, this._invalidElementText);
  }
  return $div.html();

  // ----- Helper function -----

  function appendTitleAndList($div, title, elements, elementTextFunc) {
    $div.appendElement('<strong>').text(title);
    var $ul = $div.appendElement('<ul>');
    elements.forEach(function(element) {
      $ul.appendElement('<li>').text(elementTextFunc.call(this, element));
    }, this);
  }
}

/**
 * Override this function to retrieve the text of an invalid element
 * @param element
 * @returns {String}
 */
_invalidElementText(element) {
  return '';
}

/**
 * Override this function to retrieve the text of an missing mandatory element
 * @param element
 * @returns {String}
 */
_missingElementText(element) {
  return '';
}

session() {
  return this.widget.session;
}

/**
 * Register a handler function for save actions.
 * All handler functions must return a Status. In case of an error a Status object with severity error must be returned.
 * Note: in contrast to events, handlers can control the flow of the lifecycle. They also have a return value where events have none.
 *   Only one handler can be registered for each type.
 */
handle(type, func) {
  var supportedTypes = ['load', 'save'];
  if (supportedTypes.indexOf(type) === -1) {
    throw new Error('Cannot register handler for unsupported type \'' + type + '\'');
  }
  this.handlers[type] = func;
}

/**
 * Register an event handler for the given type.
 * Event handlers don't have a return value. They do not have any influence on the lifecycle flow. There can be multiple event
 * handler for each type.
 */
on(type, func) {
  return this.events.on(type, func);
}

off(type, func) {
  return this.events.off(type, func);
}
}
