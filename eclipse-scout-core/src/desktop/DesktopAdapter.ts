/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ModelAdapter} from '../index';

export default class DesktopAdapter extends ModelAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['benchVisible', 'navigationVisible', 'navigationHandleVisible', 'headerVisible', 'geolocationServiceAvailable', 'inBackground', 'focusedElement']);
  }

  _goOffline() {
    this.widget.goOffline();
  }

  _goOnline() {
    this.widget.goOnline();
  }

  _onWidgetHistoryEntryActivate(event) {
    this._send('historyEntryActivate', {
      deepLinkPath: event.deepLinkPath
    });
  }

  _onWidgetFormActivate(event) {
    if (event.form && !event.form.modelAdapter) {
      return; // Ignore ScoutJS forms
    }
    this._sendFormActivate(event.form);
  }

  _sendFormActivate(form) {
    let eventData = {
      formId: form ? form.modelAdapter.id : null
    };

    this._send('formActivate', eventData, {
      coalesce: function(previous) {
        // Do not coalesce if formId was set to null by the previous event,
        // this is the only way the server knows that the desktop was brought to front
        return this.target === previous.target && this.type === previous.type &&
          !(previous.formId === null && this.formId !== null);
      }
    });
  }

  _prepareRemoteProperty(propertyName, value) {
    if (propertyName === 'focusedElement') {
      return value;
    }
    return super._prepareRemoteProperty(propertyName, value);
  }

  _sendFocusedElement(focusedElement) {
    // Find the nearest widget with a model adapter
    while (focusedElement && !focusedElement.modelAdapter) {
      focusedElement = focusedElement.parent;
    }
    focusedElement = focusedElement ? focusedElement.id : null;
    this._sendProperty('focusedElement', focusedElement);
  }

  _logoAction(event) {
    this._send('logoAction');
  }

  _onWidgetEvent(event) {
    if (event.type === 'formActivate') {
      this._onWidgetFormActivate(event);
    } else if (event.type === 'historyEntryActivate') {
      this._onWidgetHistoryEntryActivate(event);
    } else if (event.type === 'logoAction') {
      this._logoAction(event);
    } else if (event.type === 'cancelForms') {
      this._onWidgetCancelAllForms(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  _onFormShow(event) {
    let form,
      displayParent = this.session.getModelAdapter(event.displayParent);

    if (displayParent) {
      form = this.session.getOrCreateWidget(event.form, displayParent.widget);
      form.setDisplayParent(displayParent.widget);

      let hasPendingFormActivateEvent = this.session.asyncEvents.some(function(event) {
        return event.type === 'formActivate' && event.target === this.id;
      }, this);
      if (!hasPendingFormActivateEvent) {
        this.addFilterForWidgetEvent(widgetEvent => {
          return (widgetEvent.type === 'formActivate' && widgetEvent.form === form);
        });
      }

      this.widget.showForm(form, event.position);
    }
  }

  _onFormHide(event) {
    let form,
      displayParent = this.session.getModelAdapter(event.displayParent);

    if (displayParent) {
      form = this.session.getModelAdapter(event.form);
      this.widget.hideForm(form.widget);
    }
  }

  _onFormActivate(event) {
    let form = this.session.getWidget(event.form);
    this.widget.activateForm(form);
  }

  _onWidgetCancelAllForms(event) {
    event.preventDefault();
    let formIds = [];
    if (event.forms) {
      formIds = event.forms.map(form => {
        return form.modelAdapter.id;
      });
    }
    this._send('cancelForms', {
      formIds: formIds
    });
  }

  _onMessageBoxShow(event) {
    let messageBox,
      displayParent = this.session.getModelAdapter(event.displayParent);

    if (displayParent) {
      messageBox = this.session.getOrCreateWidget(event.messageBox, displayParent.widget);
      messageBox.setDisplayParent(displayParent.widget);
      displayParent.widget.messageBoxController.registerAndRender(messageBox);
    }
  }

  _onMessageBoxHide(event) {
    let messageBox,
      displayParent = this.session.getModelAdapter(event.displayParent);

    if (displayParent) {
      messageBox = this.session.getModelAdapter(event.messageBox);
      displayParent.widget.messageBoxController.unregisterAndRemove(messageBox.widget);
    }
  }

  _onFileChooserShow(event) {
    let fileChooser,
      displayParent = this.session.getModelAdapter(event.displayParent);

    if (displayParent) {
      fileChooser = this.session.getOrCreateWidget(event.fileChooser, displayParent.widget);
      fileChooser.setDisplayParent(displayParent.widget);
      displayParent.widget.fileChooserController.registerAndRender(fileChooser);
    }
  }

  _onFileChooserHide(event) {
    let fileChooser,
      displayParent = this.session.getModelAdapter(event.displayParent);

    if (displayParent) {
      fileChooser = this.session.getModelAdapter(event.fileChooser);
      displayParent.widget.fileChooserController.unregisterAndRemove(fileChooser.widget);
    }
  }

  _onOpenUri(event) {
    this.widget.openUri(event.uri, event.action);
  }

  _onOutlineChanged(event) {
    let outline = this.session.getOrCreateWidget(event.outline, this.widget);
    this.widget.setOutline(outline);
  }

  _onAddNotification(event) {
    let notification = this.session.getOrCreateWidget(event.notification, this.widget);
    this.widget.addNotification(notification);
  }

  _onRemoveNotification(event) {
    this.widget.removeNotification(event.notification);
  }

  _onOutlineContentActivate(event) {
    this.widget.bringOutlineToFront();
  }

  _onRequestGeolocation(event) {
    if (navigator.geolocation) {
      let success = function(position) {
        this._send('geolocationDetermined', {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        });
      }.bind(this);
      let error = function(error) {
        this._send('geolocationDetermined', {
          errorCode: error.code,
          errorMessage: error.message
        });
      }.bind(this);
      navigator.geolocation.getCurrentPosition(success, error);
    }
  }

  onModelAction(event) {
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
      super.onModelAction(event);
    }
  }
}
