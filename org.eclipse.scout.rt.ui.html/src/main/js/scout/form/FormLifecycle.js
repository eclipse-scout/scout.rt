scout.FormLifecycle = function() {
  this.askSaveChanges = true;
  this.askSaveChangesText; // Java: cancelVerificationText
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

scout.FormLifecycle.prototype.init = function(form) {
  scout.assertParameter('form', form, scout.Form);
  this.form = form;
  this.askSaveChangesText = this.session().text('FormSaveChangesQuestion');
  this.markAsSaved();
};

/**
 * @returns {Promise}
 */
scout.FormLifecycle.prototype.doCancel = function() {
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
 * TODO [awe] 6.1 default impl. sollte form vom desktop entfernen
 * @returns {Promise}
 */
scout.FormLifecycle.prototype.disposeForm = function() {
  this.events.trigger('disposeForm');
  return $.resolvedPromise();
};

/**
 * @returns {Promise}
 */
scout.FormLifecycle.prototype.doReset = function() {
  throw new Error('doReset not implemented yet');
};

/**
 * Helper function to deal with functions that return a Status object.
 * Makes it easier to return early when that function returns an invalid status (= less code to write).
 *
 * @returns {Promise}
 */
scout.FormLifecycle.prototype._whenInvalid = function(func) {
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
scout.FormLifecycle.prototype.doOk = function() {
  // 1.) validate form
  return this._whenInvalid(this._validateForm)
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
        }.bind(this))
    }.bind(this));
};

scout.FormLifecycle.prototype._showYesNoCancelMessageBox = function(message, yesAction, noAction) {
  return new scout.MessageBoxes(this.form)
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
scout.FormLifecycle.prototype._showStatusMessageBox = function(status) {
  return new scout.MessageBoxes(this.form)
    .withSeverity(scout.MessageBox.SEVERITY.ERROR)
    .withBody(status.message, true)
    .withYes(this.session().text('Ok'))
    .buildAndOpen();
};

/**
 * @returns {Promise}
 */
scout.FormLifecycle.prototype._validateForm = function() {
  var missingFields = [];
  var invalidFields = [];

  this.form.visitFields(function(field) {
    var result = field.validate();
    if (result.valid) {
      return;
    }
    // when mandatory is not fulfilled, do not add to invalid fields
    if (!result.validByMandatory) {
      missingFields.push(field);
      return;
    }
    if (!result.validByErrorStatus) {
      invalidFields.push(field);
      return;
    }
  });

  var status = new scout.Status();
  if (missingFields.length === 0 && invalidFields.length === 0) {
    status.severity = scout.Status.Severity.OK;
  } else {
    status.severity = scout.Status.Severity.ERROR;
    status.message = this._createInvalidFieldsMessageHtml(missingFields, invalidFields);
  }

  return $.resolvedPromise(status);
};

/**
 * Creates a HTML message used to display missing and invalid fields in a message box.
 */
scout.FormLifecycle.prototype._createInvalidFieldsMessageHtml = function(missingFields, invalidFields) {
  var $div = $('<div>'), // cannot use $.makeDiv here because this needs to work without any rendered elements at all
    hasMissingFields = missingFields.length > 0,
    hasInvalidFields = invalidFields.length > 0;
  if (hasMissingFields) {
    appendTitleAndList.call(this, $div, 'FormEmptyMandatoryFieldsMessage', missingFields);
  }
  if (hasMissingFields && hasInvalidFields) {
    $div.appendElement('<br>');
  }
  if (hasInvalidFields) {
    appendTitleAndList.call(this, $div, 'FormInvalidFieldsMessage', invalidFields);
  }
  return $div.html();

  function appendTitleAndList($div, titleKey, fields) {
    $div
      .appendElement('<strong>')
      .text(this.session().text(titleKey));
    var $ul = $div.appendElement('<ul>');
    fields.forEach(function(field) {
      $ul.appendElement('<li>').text(field.label);
    });
  }
};

/**
 * @returns {Promise}
 */
scout.FormLifecycle.prototype._defaultSave = function() {
  return $.resolvedPromise(scout.Status.ok());
};

/**
 * @returns {Promise}
 */
scout.FormLifecycle.prototype._save = function() {
  return this.handlers.save()
    .then(function(status) {
      this.events.trigger('save');
      return status;
    }.bind(this));
};

/**
 * @returns {Promise}
 */
scout.FormLifecycle.prototype.doSave = function() {
  // 1.) validate form
  return this._whenInvalid(this._validateForm)
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
        }.bind(this))
    }.bind(this));
};

/**
 * @returns {Promise}
 */
scout.FormLifecycle.prototype.doClose = function() {
  return this.doFinally()
    .then(this.disposeForm.bind(this));
};

/**
 * @returns {Promise}
 */
scout.FormLifecycle.prototype.doFinally = function() {
  this.events.trigger('finally');
  return $.resolvedPromise();
};

scout.FormLifecycle.prototype.markAsSaved = function() {
  this.form.visitFields(function(field) {
    field.markAsSaved();
  });
};

/**
 * Visits all form fields and calls the updateRequiresSave() function. If any
 * field has the requiresSave flag set to true, this function returns true,
 * false otherwise.
 *
 * @see (Java) AbstractFormField #checkSaveNeeded, #isSaveNeeded
 */
scout.FormLifecycle.prototype.requiresSave = function() {
  var requiresSave = false;
  this.form.visitFields(function(field) {
    field.updateRequiresSave();
    if (field.requiresSave) {
      requiresSave = true;
    }
  });
  return requiresSave;
};

scout.FormLifecycle.prototype.session = function() {
  return this.form.session;
};

/**
 * Register a handler function for save actions.
 * All handler functions must return a scout.Status. In case of an error a Status object with severity error must be returned.
 * Note: in contrast to events, handlers can control the flow of the lifecycle. They also have a return value where events have none.
 *   Only one handler can be registered for each type.
 */
scout.FormLifecycle.prototype.handle = function(type, func) {
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
scout.FormLifecycle.prototype.on = function(type, func) {
  return this.events.on(type, func);
};

scout.FormLifecycle.prototype.off = function(type, func) {
  return this.events.off(type, func);
};
