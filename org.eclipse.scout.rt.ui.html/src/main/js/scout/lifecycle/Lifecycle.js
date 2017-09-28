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
/**
 * Abstract base class for validation lifecycles as used for forms.
 * A subclass must set the properties, in order to display error messages:
 * - emptyMandatoryElementsTextKey
 * - invalidElementsTextKey
 *
 * @class
 * @constructor
 * @abstract
 */
scout.Lifecycle = function() {
  this.emptyMandatoryElementsTextKey = null;
  this.invalidElementsTextKey = null;

  this.askIfNeedSave = true;
  this.askIfNeedSaveText = null; // Java: cancelVerificationText
  this.events = new scout.EventSupport();
  this.handlers = {
    'load': this._defaultLoad.bind(this),
    'save': this._defaultSave.bind(this)
  };
};

// Info: doExportXml, doImportXml, doSaveWithoutMarkerChange is not supported in Html UI

scout.Lifecycle.prototype.init = function(model) {
  scout.assertParameter('widget', model.widget);
  $.extend(this, model);
  this.askIfNeedSaveText = this.session().text('FormSaveChangesQuestion');
};

scout.Lifecycle.prototype.load = function() {
  return this._load().then(function() {
    this.markAsSaved();
    this.events.trigger('postLoad');
  }.bind(this));
};

/**
 * @returns {Promise}
 */
scout.Lifecycle.prototype._load = function() {
  return this.handlers.load()
    .then(function(status) {
      this.events.trigger('load');
    }.bind(this));
};

/**
 * @returns {Promise}
 */
scout.Lifecycle.prototype._defaultLoad = function() {
  return $.resolvedPromise();
};

/**
 * @returns {Promise}
 */
scout.Lifecycle.prototype.ok = function() {
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
};

/**
 * @returns {Promise}
 */
scout.Lifecycle.prototype.cancel = function() {
  var showMessageBox = this.requiresSave() && this.askIfNeedSave;
  if (showMessageBox) {
    return this._showYesNoCancelMessageBox(
      this.askIfNeedSaveText,
      this.ok.bind(this),
      this.close.bind(this));
  } else {
    return this.close();
  }
};

/**
 * @returns {Promise}
 */
scout.Lifecycle.prototype.reset = function() {
  this._reset();

  // reload the state
  return this.load().then(function() {
    this.events.trigger('reset');
  }.bind(this));
};

/**
 * @returns {Promise}
 */
scout.Lifecycle.prototype.close = function() {
  return this._close();
};

/**
 * @returns {Promise}
 */
scout.Lifecycle.prototype._close = function() {
  this.events.trigger('close');
  return $.resolvedPromise();
};

/**
 * @returns {Promise}
 */
scout.Lifecycle.prototype.save = function() {
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
};

/**
 * @returns {Promise}
 */
scout.Lifecycle.prototype._save = function() {
  return this.handlers.save()
    .then(function(status) {
      this.events.trigger('save');
      return status;
    }.bind(this));
};

/**
 * @returns {Promise}
 */
scout.Lifecycle.prototype._defaultSave = function() {
  return $.resolvedPromise();
};

scout.Lifecycle.prototype.markAsSaved = function() {
  // NOP
};

/**
 * Override this function to check if any data has changed and saving is required.
 * @returns {boolean}
 */
scout.Lifecycle.prototype.requiresSave = function() {
  return false;
};

scout.Lifecycle.prototype.setAskIfNeedSave = function(askIfNeedSave) {
  this.askIfNeedSave = askIfNeedSave;
};

/**
 * Helper function to deal with functions that return a Status object.
 * Makes it easier to return early when that function returns an invalid status (= less code to write).
 *
 * @returns {Promise}
 */
scout.Lifecycle.prototype._whenInvalid = function(func) {
  return func.call(this)
    .then(function(status) {
      if (!status || status.isValid()) {
        return false;
      }
      return this._showStatusMessageBox(status)
        .then(function() {
          return true;
        });
    }.bind(this))
    .catch(function(error) {
      var status = scout.Status.error({
        message: error.message
      });
      return this._showStatusMessageBox(status)
        .then(function() {
          return true;
        });
    }.bind(this));
};

scout.Lifecycle.prototype._showYesNoCancelMessageBox = function(message, yesAction, noAction) {
  return new scout.MessageBoxes(this.widget)
    .withSeverity(scout.Status.Severity.WARNING)
    .withHeader(message)
    .withYes()
    .withNo()
    .withCancel()
    .buildAndOpen()
    .then(function(option) {
      if (option === scout.MessageBox.Buttons.YES) {
        return yesAction();
      } else if (option === scout.MessageBox.Buttons.NO) {
        return noAction();
      }
      return $.resolvedPromise();
    });
};

/**
 * @param status
 * @returns {Promise}
 */
scout.Lifecycle.prototype._showStatusMessageBox = function(status) {
  return new scout.MessageBoxes(this.widget)
    .withSeverity(status.severity)
    .withBody(status.message, true)
    .withYes(this.session().text('Ok'))
    .buildAndOpen();
};

/**
 * @returns {Promise}
 */
scout.Lifecycle.prototype._validate = function() {
  var elements = this._invalidElements();
  var status = new scout.Status();
  if (elements.missingElements.length === 0 && elements.invalidElements.length === 0) {
    status.severity = scout.Status.Severity.OK;
  } else {
    status.severity = scout.Status.Severity.ERROR;
    status.message = this._createInvalidElementsMessageHtml(elements.missingElements, elements.invalidElements);
  }

  return $.resolvedPromise(status);
};

/**
 * Override this function to check for invalid elements on the parent which prevent
 * saving of the parent.(eg. check if all mandatory elements contain a value)
 *
 * @returns Object with
 * valid: (true or false)
 * missingElements: Elements which should have a value
 * invalidElements: Elements which have an invalid value
 */
scout.Lifecycle.prototype._invalidElements = function() {
  return {
    missingElements: [],
    invalidElements: []
  };
};

/**
 * Creates a HTML message used to display missing and invalid fields in a message box.
 */
scout.Lifecycle.prototype._createInvalidElementsMessageHtml = function(missing, invalid) {
  var $div = $('<div>'), // cannot use $.makeDiv here because this needs to work without any rendered elements at all
    hasMissing = missing.length > 0,
    hasInvalid = invalid.length > 0;
  if (hasMissing) {
    appendTitleAndList.call(this, $div, this.emptyMandatoryElementsTextKey, missing, this._missingElementText);
  }
  if (hasMissing && hasInvalid) {
    $div.appendElement('<br>');
  }
  if (hasInvalid) {
    appendTitleAndList.call(this, $div, this.invalidElementsTextKey, invalid, this._invalidElementText);
  }
  return $div.html();

  function appendTitleAndList($div, titleKey, elements, textFunc) {
    $div
      .appendElement('<strong>')
      .text(this.session().text(titleKey));
    var $ul = $div.appendElement('<ul>');
    elements.forEach(function(element) {
      $ul.appendElement('<li>').text(textFunc.call(this, element));
    }, this);
  }
};

/**
 * Override this function to retrieve the text of an invalid element
 * @param element
 * @returns {String}
 */
scout.Lifecycle.prototype._invalidElementText = function(element) {
  return '';
};

/**
 * Override this function to retrieve the text of an missing mandatory element
 * @param element
 * @returns {String}
 */
scout.Lifecycle.prototype._missingElementText = function(element) {
  return '';
};

scout.Lifecycle.prototype.session = function() {
  return this.widget.session;
};

/**
 * Register a handler function for save actions.
 * All handler functions must return a scout.Status. In case of an error a Status object with severity error must be returned.
 * Note: in contrast to events, handlers can control the flow of the lifecycle. They also have a return value where events have none.
 *   Only one handler can be registered for each type.
 */
scout.Lifecycle.prototype.handle = function(type, func) {
  var supportedTypes = ['load', 'save'];
  if (supportedTypes.indexOf(type) === -1) {
    throw new Error('Cannot register handler for unsupported type \'' + type + '\'');
  }
  this.handlers[type] = func;
};

/**
 * Register an event handler for the given type.
 * Event handlers don't have a return value. They do not have any influence on the lifecycle flow. There can be multiple event
 * handler for each type.
 */
scout.Lifecycle.prototype.on = function(type, func) {
  return this.events.on(type, func);
};

scout.Lifecycle.prototype.off = function(type, func) {
  return this.events.off(type, func);
};
