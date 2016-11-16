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

scout.FormLifecycle.prototype.doCancel = function() {
  var showMessageBox = this.requiresSave() && this.askSaveChanges;
  if (showMessageBox) {
    this._showYesNoCancelMessageBox(
      this.askSaveChangesText,
      this.doOk.bind(this),
      this.doClose.bind(this));
  } else {
    this.doFinally();
    this.disposeForm();
  }
};

scout.FormLifecycle.prototype.disposeForm = function() {
  // FIXME [awe] 6.1 default impl. sollte form vom desktop entfernen
  this.events.trigger('disposeForm');
};

scout.FormLifecycle.prototype.doReset = function() {
  throw new Error('doReset not implemented yet');
};

/**
 * Helper function to deal with functions that return a Status object.
 * Makes it easier to return early when that function returns an invalid status (= less code to write).
 */
scout.FormLifecycle.prototype._whenInvalid = function(func) {
  var status = func.call(this);
  if (!(status instanceof scout.Status)) {
    throw new Error('Expected function to return a scout.Status object');
  }
  if (status.isValid()) {
    return false;
  }
  this._showStatusMessageBox(status);
  return true;
};

scout.FormLifecycle.prototype.doOk = function() {
  if (this._whenInvalid(this._validateForm)) {
    return;
  }

  if (this.requiresSave()) {
    if (this._whenInvalid(this._save)) {
      return;
    }
    this.markAsSaved();
  }

  this.doFinally();
  this.disposeForm();
};

scout.FormLifecycle.prototype._showYesNoCancelMessageBox = function(message, yesAction, noAction) {
  var session = this.session();
  var model = {
    parent: this.form,
    severity: scout.MessageBox.SEVERITY.WARNING,
    header: message,
    yesButtonText: session.text('Yes'),
    noButtonText: session.text('No'),
    cancelButtonText: session.text('Cancel')
  };
  var mbController = this.form.messageBoxController;
  var messageBox = scout.create('MessageBox', model);
  messageBox.on('action', function(event) {
    mbController.unregisterAndRemove(messageBox);
    if (event.option === 'yes') {
      yesAction();
    } else if (event.option === 'no') {
      noAction();
    }
  });
  mbController.registerAndRender(messageBox);
};

scout.FormLifecycle.prototype._showStatusMessageBox = function(status) {
  // FIXME [awe] 6.1 - make MessageBox easier to use in JS only case (like MessageBoxes in Java)
  var model = {
    parent: this.form,
    severity: scout.MessageBox.SEVERITY.ERROR,
    html: status.message,
    yesButtonText: this.session().text('Ok')
  };
  var mbController = this.form.messageBoxController;
  var messageBox = scout.create('MessageBox', model);
  messageBox.on('action', mbController.unregisterAndRemove.bind(mbController, messageBox));
  mbController.registerAndRender(messageBox);
};

scout.FormLifecycle.prototype._validateForm = function() {
  var missingFields = [];
  var invalidFields = [];

  this.form.visitFields(function(field) {
    var result = field.validate();
    if (result.valid) {
      return;
    }
    // when mandatory is not fullfilled, do not add to invalid fields
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

  return status;
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

scout.FormLifecycle.prototype._defaultSave = function() {
  return scout.Status.ok();
};

scout.FormLifecycle.prototype._save = function() {
  var status = this.handlers.save();
  this.events.trigger('save');
  return status;
};

scout.FormLifecycle.prototype.doSave = function() {
  throw new Error('doSave not implemented yet');
};

scout.FormLifecycle.prototype.doClose = function() {
  this.doFinally();
  this.disposeForm();
};

scout.FormLifecycle.prototype.doFinally = function() {
  this.events.trigger('finally');
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
