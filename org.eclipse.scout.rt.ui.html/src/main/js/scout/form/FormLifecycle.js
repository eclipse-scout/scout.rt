scout.FormLifecycle = function() {
  this.askSaveChanges = true;
  this.askSaveChangesText; // Java: cancelVerificationText
};

// FIXME [awe] 6.1 renamen -> doSave -> save, etc.

scout.FormLifecycle.prototype.init = function(form) {
  scout.objects.mandatoryParameter('form', form, scout.Form);
  this.form = form;
  this.askSaveChangesText = this.session().text('FormSaveChangesQuestion');

  this.markAsSaved();
};

scout.FormLifecycle.prototype.doClose = function() {

};

scout.FormLifecycle.prototype.doCancel = function() {
  if (this.requiresSave() && this.askSaveChanges) {
    if (this._showYesNoCancelMessageBox(this.askSaveChangesText)) { // FIXME [awe] 6.1 - check why 3 buttons are needed and how these 3 states differ in java code
      this.doOk();
      return;
    } else {
      this.doClose();
      return;
    }
  }

  this.doFinally();
  this.disposeForm();
};

scout.FormLifecycle.prototype.disposeForm = function() {
  // FIXME [awe] 6.1 default impl. sollte form vom desktop entfernen
};

scout.FormLifecycle.prototype.doReset = function() {

};

/**
 * Helper function to deal with functions that return a Status object.
 * Makes it easier to return early when that function returns an invalid status (= less code to write).
 */
scout.FormLifecycle.prototype._whenInvalid = function(func) {
  var status = func.call(this);
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

scout.FormLifecycle.prototype._showYesNoCancelMessageBox = function(message) {
  var model = {
    parent: this.form,
    severity: scout.MessageBox.SEVERITY.WARNING,
    header: '%Validation error',
    body: status.message,
    yesButtonText: '%Yes',
    noButtonText: '%No',
    cancelButtonText: '%Cancel'
  };
  var mbController = this.form.messageBoxController;
  var messageBox = scout.create('MessageBox', model);
  messageBox.on('action', function(event) {
    // FIXME [awe] 6.1 continue... add callback functions?
    if (event.option === 'yes') {

    } else if (event.option === 'no') {

    }
    mbController.unregisterAndRemove.bind(mbController, messageBox);
  });
  mbController.registerAndRender(messageBox);
};

scout.FormLifecycle.prototype._showStatusMessageBox = function(status) {
  // FIXME [awe] 6.1 - make MessageBox easier to use in JS only case
  var model = {
    parent: this.form,
    severity: scout.MessageBox.SEVERITY.ERROR,
    header: '%Validation error',
    body: status.message,
    yesButtonText: '%Ok'
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
    if (!result.validByErrorStatus) {
      invalidFields.push(field);
      return;
    }
    if (!result.validByMandatory) {
      missingFields.push(field);
      return;
    }
  });

  var status = new scout.Status();
  if (missingFields.length === 0 && invalidFields.length === 0) {
    status.severity = scout.Status.Severity.OK;
  } else {
    status.severity = scout.Status.Severity.ERROR;
    status.message = '%Missing fields: ' + missingFields + ' Invalid fields: ' + invalidFields;
  }

  return status;
};

scout.FormLifecycle.prototype._save = function() {
  var status = new scout.Status();
  status.severity = scout.Status.Severity.OK;
  return status;
};

// FIXME [awe] 6.1 - required?
//scout.FormLifecycle.prototype.doSaveWithoutMarkerChange = function() {
//
//};

scout.FormLifecycle.prototype.doSave = function() {

};

scout.FormLifecycle.prototype.doFinally = function() {

};

scout.FormLifecycle.prototype.doClose = function() {

};

scout.FormLifecycle.prototype.doFinally = function() {
};

scout.FormLifecycle.prototype.markAsSaved = function() {
  this.form.visitFields(function(field) {
    field.markAsSaved();
  });
};

scout.FormLifecycle.prototype.requiresSave = function() { // isSaveNeeded, checkSaveNeeded
  var touched = false;
  this.form.visitFields(function(field) {
    if (field.touched) {
      touched = true;
    }
  });
  return touched;

  // FIXME [awe] 6.1 impl.
  // Java Scout geht dafür durch alle form fields durch und ruft pro field checkSaveNeeded auf.
  // Die Impl. davon ist:
  // propertySupport.setPropertyBool(PROP_SAVE_NEEDED, m_touched || interceptIsSaveNeeded());
  // Die Methode gibt aber nichts zurück, sondern setzt nur das saveNeeded property
};

// Info: doExportXml und doImportXml is not supported in Html UI

scout.FormLifecycle.prototype.session = function() {
  return this.form.session;
};
