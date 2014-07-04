// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.BaseDesktop = function() {
  scout.BaseDesktop.parent.call(this);
  this.taskbar;
  this.modalDialogStack = [];
  this.focusedDialog;
  this._addAdapterProperties(['forms']);
};
scout.inherits(scout.BaseDesktop, scout.ModelAdapter);

scout.BaseDesktop.prototype._render = function($parent) {
  //this.$entryPoint.addClass('desktop'); //FIXME desktop elements use ids,
  // maybe better change to class to support multiple session divs with multiple
  // desktops

  var i, form;
  for (i = 0; i < this.forms.length; i++) {
    this._addForm(this.forms[i]);
  }
};

scout.BaseDesktop.prototype.showMessage = function(message, type) {
  if (!type) {
    type = 'info';
  }
  if (!this.$message) {
    this.$message = this.$parent.prependDiv('', type + '-message');
  }
  this.$message.text(message);
};

scout.BaseDesktop.prototype.goOffline = function() {
  scout.BaseDesktop.parent.prototype.goOffline.call(this);

  var message = 'Die Netzwerkverbindung ist unterbrochen.',
    $reconnect;//FIXME CGU translate

  if (this.$offline) {
    return;
  }

  this.$offline = this.$parent.prependDiv('', 'offline-message');
  this.$offline.text(message);
  $reconnect = this.$offline.appendDiv('', 'reconnect');
  $reconnect
    .text('Reconnecting...')
    .hide();
  if (scout.device.supportsCssAnimation()) {
    $reconnect.addClass('reconnect-animated');
  }
  this.layout.marginTop += this.$offline.outerHeight(true);
  this.layout.layout();
};

scout.BaseDesktop.prototype.goOnline = function() {
  scout.BaseDesktop.parent.prototype.goOnline.call(this);

  if (!this.hideOfflineMessagePending) {
    this.hideOfflineMessage();
  }
};

scout.BaseDesktop.prototype.hideOfflineMessage = function() {
  if (!this.$offline) {
    return;
  }

  this.layout.marginTop -= this.$offline.outerHeight(true);
  this.$offline.remove();
  this.layout.layout();
  this.hideOfflineMessagePending = false;
  this.$offline = null;
};

scout.BaseDesktop.prototype.onReconnecting = function() {
  if (!this.$offline) {
    return;
  }

  this.$offline.find('.reconnect').show();
};

scout.BaseDesktop.prototype.onReconnectingSucceeded = function() {
  var message = 'Die Verbindung wurde wieder hergestellt.';  //FIXME CGU translate
  if (!this.$offline) {
    return;
  }

  this.$offline.find('.reconnect').hide();
  this.$offline.text(message);
  this.$offline.addClass('reconnect-successful');
  this.hideOfflineMessagePending = true;
  setTimeout(this.hideOfflineMessage.bind(this), 3000);
};

scout.BaseDesktop.prototype.onReconnectingFailed = function() {
  if (!this.$offline) {
    return;
  }

  this.$offline.find('.reconnect').hide();
};

scout.BaseDesktop.prototype._addForm = function(form) {
  if (form.displayHint == "view") {
    form.attach(this._resolveViewContainer(form));
  } else if (form.displayHint == "dialog") {
    var previousModalForm;
    if (form.modal) {
      if (this.modalDialogStack.length > 0) {
        previousModalForm = this.modalDialogStack[this.modalDialogStack.length - 1];
        previousModalForm.disable();
      }
      this.modalDialogStack.push(form);
    }

    form.attach(this.$parent);
    this.focusedDialog = form;

    if (this.taskbar) {
      if (previousModalForm) {
        this.taskbar.formDisabled(previousModalForm);
      }
      this.taskbar.formAdded(form);
    }
  } else {
    $.log("Form displayHint not handled: '" + form.displayHint + "'.");
  }
};

scout.BaseDesktop.prototype._removeForm = function(form) {
  if (!form) {
    return;
  }

  form.detach();

  if (form.displayHint === "dialog") {
    var previousModalForm;
    if (form.modal) {
      scout.arrays.remove(this.modalDialogStack, form);
      previousModalForm = this.modalDialogStack[this.modalDialogStack.length - 1];
      if (previousModalForm) {
        previousModalForm.enable();
        this.activateForm(previousModalForm);
      }
    }
    if (form === this.focusedDialog) {
      this.focusedDialog = null;
      this.activateTopDialog();
    }

    if (this.taskbar) {
      if (previousModalForm) {
        this.taskbar.formEnabled(previousModalForm);
      }
      this.taskbar.formRemoved(form);
    }
  }
};

scout.BaseDesktop.prototype.activateForm = function(form) {
  //FIXME CGU send form activated
  if (!form || this.focusedDialog === form) {
    return;
  }

  if (form.displayHint === "dialog") {
    //re attach it at the end
    form.attach(this.$parent);
    this.focusedDialog = form;

    if (this.taskbar) {
      this.taskbar.formActivated(form);
    }
  }
};

scout.BaseDesktop.prototype.minimizeForm = function(form) {
  //FIXME CGU minimize maximize sind properties auf form, können auch vom modell gesteuert werden -> Steuerung eher über form.setMaximized
  if (form.displayHint !== "dialog") {
    return;
  }

  form.minized = true;
  form.detach();
  if (form === this.focusedDialog) {
    this.focusedDialog = null;
    this.activateTopDialog();
  }

};

scout.BaseDesktop.prototype.activateTopDialog = function() {
  var topDialog = this.$parent.find('.form:last');
  if (topDialog) {
    this.activateForm(topDialog.data('model'));
  }
};

scout.BaseDesktop.prototype.maximizeForm = function(form) {
  if (form.displayHint !== "dialog") {
    return;
  }

  form.minized = false;
  form.attach(this.$parent);
};

scout.BaseDesktop.prototype.onModelAction = function(event) {
  var form;

  if (event.type_ == 'formAdded') {
    form = this.updateModelAdapters(this.forms, event.form, this);
    this._addForm(form);
  } else if (event.type_ == 'formRemoved') {
    form = this.updateModelAdapters(this.forms, event.form, this);
    this._removeForm(form);
  } else if (event.type_ == 'formEnsureVisible') {
    form = this.updateModelAdapters(this.forms, event.form, this);
    this.activateForm(form);
  } else if (event.type_ == 'formRemoved') {
    form = this.updateModelAdapters(this.forms, event.form, this);
    this._removeForm(form);
  } else {
    $.log("Model event not handled. Widget: Desktop. Event: " + event.type_ + ".");
  }
};
