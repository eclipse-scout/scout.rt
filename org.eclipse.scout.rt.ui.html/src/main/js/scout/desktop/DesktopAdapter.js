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
scout.DesktopAdapter = function() {
  scout.DesktopAdapter.parent.call(this);
  this._addAdapterProperties(['activeForm', 'viewButtons', 'menus', 'views', 'dialogs', 'outline', 'messageBoxes', 'fileChoosers', 'addOns', 'keyStrokes']);
  this._addRemoteProperties(['benchVisible', 'navigationVisible', 'headerVisible']);
};
scout.inherits(scout.DesktopAdapter, scout.ModelAdapter);

scout.DesktopAdapter.prototype._goOffline = function() {
  this.widget.goOffline();
};

scout.DesktopAdapter.prototype._goOnline = function() {
  this.widget.goOnline();
};

scout.DesktopAdapter.prototype._onFormShow = function(event) {
  var form,
    displayParent = this.session.getModelAdapter(event.displayParent);

  if (displayParent) {
    form = this.session.getOrCreateWidget(event.form, this.widget);
    this.widget.showForm(form, displayParent.widget, event.position, false);
  }
};

scout.DesktopAdapter.prototype._onFormHide = function(event) {
  var form,
    displayParent = this.session.getModelAdapter(event.displayParent);

  if (displayParent) {
    form = this.session.getModelAdapter(event.form);
    this.widget.hideForm(form.widget);
  }
};


scout.DesktopAdapter.prototype._onFormActivate = function(event) {
  var form,
    displayParent = this.session.getModelAdapter(event.displayParent);

  if (displayParent) {
    form = this.session.getOrCreateWidget(event.form, this.widghet);
    this.widget.activateForm(form, false);
  }
};

scout.DesktopAdapter.prototype._onMessageBoxShow = function(event) {
  var messageBox,
    displayParent = this.session.getModelAdapter(event.displayParent);

  if (displayParent) {
    messageBox = this.session.getOrCreateWidget(event.messageBox, this.widget);
    displayParent.widget.messageBoxController.registerAndRender(messageBox);
  }
};

scout.DesktopAdapter.prototype._onMessageBoxHide = function(event) {
  var messageBox,
    displayParent = this.session.getModelAdapter(event.displayParent);

  if (displayParent) {
    messageBox = this.session.getModelAdapter(event.messageBox);
    displayParent.widget.messageBoxController.unregisterAndRemove(messageBox.widget);
  }
};

scout.DesktopAdapter.prototype._onFileChooserShow = function(event) {
  var fileChooser,
    displayParent = this.session.getModelAdapter(event.displayParent);

  if (displayParent) {
    fileChooser = this.session.getOrCreateWidget(event.fileChooser, this.widget);
    displayParent.widget.fileChooserController.registerAndRender(fileChooser);
  }
};

scout.DesktopAdapter.prototype._onFileChooserHide = function(event) {
  var fileChooser,
    displayParent = this.session.getModelAdapter(event.displayParent);

  if (displayParent) {
    fileChooser = this.session.getModelAdapter(event.fileChooser);
    displayParent.widget.fileChooserController.unregisterAndRemove(fileChooser.widget);
  }
};

scout.DesktopAdapter.prototype._onOpenUri = function(event) {
  $.log.debug('(Desktop#_onOpenUri) uri=' + event.uri + ' action=' + event.action);
  if (!event.uri) {
    return;
  }

  if (event.action === 'download') {
    if (scout.device.isIos()) {
      // The iframe trick does not work for ios
      // Since the file cannot be stored on the file system it will be shown in the browser if possible
      // -> create a new window to not replace the existing content.
      // Drawback: Popup-Blocker will show up
      this.widget._openUriAsNewWindow(event.uri); // FIXME [awe] 6.1 - handle this in Widget? Make public?
    } else {
      this.widget._openUriInIFrame(event.uri);
    }
  } else if (event.action === 'open') {
    // Open in same window.
    // Don't call _openUriInIFrame here, if action is set to open, an url is expected to be opened in the same window
    // Additionally, some url types require to be opened in the same window like tel or mailto, at least on mobile devices
    window.location.href = event.uri;
  } else if (event.action === 'newWindow') {
    this.widget._openUriAsNewWindow(event.uri);
  }
};

scout.DesktopAdapter.prototype._onOutlineChanged = function(event) {
  var outline = this.session.getOrCreateWidget(event.outline, this.widget);
  this.widget.setOutline(outline);
};

scout.DesktopAdapter.prototype._onAddNotification = function(event) {
  scout.create('DesktopNotification', {
    parent: this.widget,
    id: event.id,
    duration: event.duration,
    status: event.status,
    closable: event.closable
  }).show();
};

scout.DesktopAdapter.prototype._onRemoveNotification = function(event) {
  this.widget.removeNotification(event.id);
};

scout.DesktopAdapter.prototype._onOutlineContentActivate = function(event) {
  this.widget.bringOutlineToFront();
};

scout.DesktopAdapter.prototype.onModelAction = function(event) {
  if (event.type === 'formShow') {
    this._onFormShow(event);
  } else if (event.type === 'formHide') {
    this._onFormHide(event);
  } else if (event.type === 'formActivate') {
    this._onFormActivate(event);
  } else if (event.type === 'messageBoxShow') {
    this._onMessageBoxShow(event);
  } else if (event.type === 'messageBoxHide') {
    this._onMessageBoxHide(event);
  } else if (event.type === 'fileChooserShow') {
    this._onFileChooserShow(event);
  } else if (event.type === 'fileChooserHide') {
    this._onFileChooserHide(event);
  } else if (event.type === 'openUri') {
    this._onOpenUri(event);
  } else if (event.type === 'outlineChanged') {
    this._onOutlineChanged(event);
  } else if (event.type === 'outlineContentActivate') {
    this._onOutlineContentActivate(event);
  } else if (event.type === 'addNotification') {
    this._onAddNotification(event);
  } else if (event.type === 'removeNotification') {
    this._onRemoveNotification(event);
  } else {
    scout.DesktopAdapter.parent.prototype.onModelAction.call(this, event);
  }
};
