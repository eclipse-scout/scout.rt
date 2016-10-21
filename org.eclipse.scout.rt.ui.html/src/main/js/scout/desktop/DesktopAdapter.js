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
  this._addRemoteProperties(['benchVisible', 'navigationVisible', 'headerVisible', 'geolocationServiceAvailable']);
};
scout.inherits(scout.DesktopAdapter, scout.ModelAdapter);

scout.DesktopAdapter.prototype._goOffline = function() {
  this.widget.goOffline();
};

scout.DesktopAdapter.prototype._goOnline = function() {
  this.widget.goOnline();
};

scout.DesktopAdapter.prototype._onWidgetHistoryEntryActivated = function(event) {
  this._send('historyEntryActivated', {
    deepLinkPath: event.deepLinkPath
  });
};

scout.DesktopAdapter.prototype._onWidgetFormActivated = function(event) {
  this._sendFormActivated(event.form);
};

scout.DesktopAdapter.prototype._sendFormActivated = function(form) {
  var eventData = {
    formId: form ? form.modelAdapter.id : null
  };

  this._send('formActivated', eventData, {
    coalesce: function(previous) {
      return this.type === previous.type;
    }
  });
};

scout.DesktopAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'formActivated') {
    this._onWidgetFormActivated(event);
  } else if (event.type === 'historyEntryActivated') {
    this._onWidgetHistoryEntryActivated(event);
  } else {
    scout.DesktopAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

scout.DesktopAdapter.prototype._onFormShow = function(event) {
  var form,
    displayParent = this.session.getModelAdapter(event.displayParent);

  if (displayParent) {
    form = this.session.getOrCreateWidget(event.form, this.widget);
    this.addFilterForWidgetEvent(function(widgetEvent) {
      return (widgetEvent.type === 'formActivated' &&
          widgetEvent.form === form);
    });
    this.widget.showForm(form, displayParent.widget, event.position);
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
    form = this.session.getOrCreateWidget(event.form, this.widget);
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
  this.widget.openUri(event.uri, event.action);
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

scout.DesktopAdapter.prototype._onRequestGeolocation = function(event) {
  if (navigator.geolocation) {
    var success = function(position) {
      this._send('geolocationDetermined', {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude
      });
    }.bind(this);
    var error = function(error) {
      this._send('geolocationDetermined', {
        errorCode: error.code,
        errorMessage: error.message
      });
    }.bind(this);
    navigator.geolocation.getCurrentPosition(success, error);
  }
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
  } else if (event.type === 'requestGeolocation') {
    this._onRequestGeolocation(event);
  } else {
    scout.DesktopAdapter.parent.prototype.onModelAction.call(this, event);
  }
};
