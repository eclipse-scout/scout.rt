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
scout.AbstractLifecycle = function() {
  this.emptyMandatoryElementsTextKey = null;
  this.invalidElementsTextKey = null;

  this.displayParent = null;
  this.askSaveChanges = true;
  this.askSaveChangesText = null; // Java: cancelVerificationText
  this.events = new scout.EventSupport();
  this.handlers = {
    'save': this._defaultSave.bind(this)
  };

  // FIXME CGU awe: why not always add lifecycle? remove case may remove it or empty method
  // FIXME CGU awe: on ui we should use destroy, not dispose. Event name should not contain 'form'.
  // FIXME CGU awe: Form lifecycle methods should be available on form delegating to lifecycle
  // FIXME CGU awe: do we need the 'do' prefix?
  // FIXME CGU awe: form.close() currently only works for remote case, delegate to lifecycle in js case
};

// Info: doExportXml, doImportXml, doSaveWithoutMarkerChange is not supported in Html UI

scout.AbstractLifecycle.prototype.init = function(model) {
  scout.assertParameter('widget', model.widget);
  $.extend(this, model);
  this.askSaveChangesText = this.session().text('FormSaveChangesQuestion');
  this.markAsSaved();
};

/**
 * @returns {Promise}
 */
scout.AbstractLifecycle.prototype.doCancel = function() {
  var showMessageBox = this.requiresSave() && this.askSaveChanges;
  if (showMessageBox) {
    return this._showYesNoCancelMessageBox(
      this.askSaveChangesText,
      this.doOk.bind(this),
      this.doClose.bind(this));
  } else {
    return this.doClose();
  }
};

/**
 * @returns {Promise}
 */
scout.AbstractLifecycle.prototype.disposeWidget = function() {
  this.events.trigger('disposeWidget');
  return $.resolvedPromise();
};

/**
 * @returns {Promise}
 */
scout.AbstractLifecycle.prototype.doReset = function() {
  throw new Error('doReset not implemented yet');
};

/**
 * Helper function to deal with functions that return a Status object.
 * Makes it easier to return early when that function returns an invalid status (= less code to write).
 *
 * @returns {Promise}
 */
scout.AbstractLifecycle.prototype._whenInvalid = function(func) {
  return func.call(this)
    .then(function(status) {
      if (!(status instanceof scout.Status)) {
        throw new Error('Expected function to return a scout.Status object');
      }
      if (status.isValid()) {
        return false;
      }
      return this._showStatusMessageBox(status)
        .then(function(){
          return true;
        });
    }.bind(this));
};

/**
 * @returns {Promise}
 */
scout.AbstractLifecycle.prototype.doOk = function() {
  // 1.) validate form
  return this._whenInvalid(this._validateWidget)
    .then(function(invalid) {
      if (invalid) {
        return;
      }

      // 2.) check if save is required
      if (!this.requiresSave()) {
        return this.doClose();
      }

      // 3.) perform save operation
      return this._whenInvalid(this._save)
        .then(function(invalid) {
          if (invalid) {
            return;
          }

          this.markAsSaved();
          return this.doClose();
        }.bind(this));
    }.bind(this));
};

scout.AbstractLifecycle.prototype._showYesNoCancelMessageBox = function(message, yesAction, noAction) {
  return new scout.MessageBoxes(this.widget)
    .withSeverity(scout.MessageBox.SEVERITY.WARNING)
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
scout.AbstractLifecycle.prototype._showStatusMessageBox = function(status) {
  return new scout.MessageBoxes(this.widget)
    .withSeverity(scout.MessageBox.SEVERITY.ERROR)
    .withBody(status.message, true)
    .withYes(this.session().text('Ok'))
    .buildAndOpen();
};

/**
 * @returns {Promise}
 */
scout.AbstractLifecycle.prototype._validateWidget = function() {
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
scout.AbstractLifecycle.prototype._invalidElements = function() {
  return {
    missingElements: [],
    invalidElements: []
  };
};

/**
 * Creates a HTML message used to display missing and invalid fields in a message box.
 */
scout.AbstractLifecycle.prototype._createInvalidElementsMessageHtml = function(missing, invalid) {
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
scout.AbstractLifecycle.prototype._invalidElementText = function(element) {
  return '';
};

/**
 * Override this function to retrieve the text of an missing mandatory element
 * @param element
 * @returns {String}
 */
scout.AbstractLifecycle.prototype._missingElementText = function(element) {
  return '';
};

/**
 * @returns {Promise}
 */
scout.AbstractLifecycle.prototype._defaultSave = function() {
  return $.resolvedPromise(scout.Status.ok());
};

/**
 * @returns {Promise}
 */
scout.AbstractLifecycle.prototype._save = function() {
  return this.handlers.save()
    .then(function(status) {
      this.events.trigger('save');
      return status;
    }.bind(this));
};

/**
 * @returns {Promise}
 */
scout.AbstractLifecycle.prototype.doSave = function() {
  // 1.) validate form
  return this._whenInvalid(this._validateWidget)
    .then(function(invalid) {

      // 2.) invalid or form does has not been changed
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
scout.AbstractLifecycle.prototype.doClose = function() {
  return this.doFinally()
    .then(this.disposeWidget.bind(this));
};

/**
 * @returns {Promise}
 */
scout.AbstractLifecycle.prototype.doFinally = function() {
  this.events.trigger('finally');
  return $.resolvedPromise();
};

scout.AbstractLifecycle.prototype.markAsSaved = function() {
  // NOP
};

/**
 * Override this function to check if any data has changed and saving is required.
 * @returns {boolean}
 */
scout.AbstractLifecycle.prototype.requiresSave = function() {
  return false;
};

scout.AbstractLifecycle.prototype.session = function() {
  return this.widget.session;
};

/**
 * Register a handler function for save actions.
 * All handler functions must return a scout.Status. In case of an error a Status object with severity error must be returned.
 * Note: in contrast to events, handlers can control the flow of the lifecycle. They also have a return value where events have none.
 *   Only one handler can be registered for each type.
 */
scout.AbstractLifecycle.prototype.handle = function(type, func) {
  var supportedTypes = ['save'];
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
scout.AbstractLifecycle.prototype.on = function(type, func) {
  return this.events.on(type, func);
};

scout.AbstractLifecycle.prototype.off = function(type, func) {
  return this.events.off(type, func);
};
