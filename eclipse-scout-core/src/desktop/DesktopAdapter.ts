/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  App, Desktop, DesktopCancelFormsEvent, DesktopFormActivateEvent, DesktopHistoryState, DesktopNotification, DisplayParent, Event, FileChooser, FileChooserAdapter, Form, FormAdapter, MessageBox, MessageBoxAdapter, ModelAdapter, objects,
  Outline, RemoteEvent, Widget
} from '../index';
import $ from 'jquery';

export class DesktopAdapter extends ModelAdapter {
  declare widget: Desktop;

  constructor() {
    super();
    this._addRemoteProperties(['benchVisible', 'navigationVisible', 'navigationHandleVisible', 'headerVisible', 'geolocationServiceAvailable', 'inBackground', 'focusedElement']);
  }

  protected override _goOffline() {
    this.widget.goOffline();
  }

  protected override _goOnline() {
    this.widget.goOnline();
  }

  protected _onWidgetHistoryEntryActivate(event: Event & DesktopHistoryState) {
    this._send('historyEntryActivate', {
      deepLinkPath: event.deepLinkPath
    });
  }

  protected _onWidgetRender(event: Event<Desktop>) {
    this._send('desktopReady');
  }

  protected _onWidgetFormActivate(event: DesktopFormActivateEvent) {
    if (event.form && !event.form.modelAdapter) {
      return; // Ignore ScoutJS forms
    }
    this._sendFormActivate(event.form);
  }

  protected _sendFormActivate(form: Form) {
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

  protected override _prepareRemoteProperty(propertyName: string, value: any): any {
    if (propertyName === 'focusedElement') {
      return value;
    }
    return super._prepareRemoteProperty(propertyName, value);
  }

  protected _sendFocusedElement(focusedElement: Widget) {
    // Find the nearest widget with a model adapter
    while (focusedElement && !focusedElement.modelAdapter) {
      focusedElement = focusedElement.parent;
    }
    this._sendProperty('focusedElement', focusedElement ? focusedElement.id : null);
  }

  protected _logoAction(event: Event) {
    this._send('logoAction');
  }

  protected override _onWidgetEvent(event: Event<Desktop>) {
    if (event.type === 'formActivate') {
      this._onWidgetFormActivate(event as DesktopFormActivateEvent);
    } else if (event.type === 'historyEntryActivate') {
      this._onWidgetHistoryEntryActivate(event as Event<Desktop> & DesktopHistoryState);
    } else if (event.type === 'logoAction') {
      this._logoAction(event);
    } else if (event.type === 'cancelForms') {
      this._onWidgetCancelAllForms(event as DesktopCancelFormsEvent);
    } else if (event.type === 'render') {
      this._onWidgetRender(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onFormShow(event: RemoteEvent) {
    let displayParent = this.session.getModelAdapter(event.displayParent);
    if (displayParent) {
      let form = this.session.getOrCreateWidget(event.form, displayParent.widget) as Form;
      form.setDisplayParent(displayParent.widget as DisplayParent);

      let hasPendingFormActivateEvent = this.session.asyncEvents.some(event => event.type === 'formActivate' && event.target === this.id);
      if (!hasPendingFormActivateEvent) {
        this.addFilterForWidgetEvent(widgetEvent => widgetEvent.type === 'formActivate' && (widgetEvent as DesktopFormActivateEvent).form === form);
      }

      this.widget.showForm(form, event.position);
    }
  }

  protected _onFormHide(event: RemoteEvent) {
    let displayParent = this.session.getModelAdapter(event.displayParent);
    if (displayParent) {
      let form = this.session.getModelAdapter(event.form) as FormAdapter;
      this.widget.hideForm(form.widget);
    }
  }

  protected _onFormActivate(event: RemoteEvent) {
    let form = this.session.getWidget(event.form) as Form;
    this.widget.activateForm(form);
  }

  protected _onWidgetCancelAllForms(event: DesktopCancelFormsEvent) {
    const formIds: string[] = [];
    const jsForms: Form[] = [];
    for (const form of event.forms || []) {
      if (form.modelAdapter) {
        formIds.push(form.modelAdapter.id);
      } else {
        jsForms.push(form);
      }
    }

    if (!formIds.length) {
      // no remote forms, return without preventDefault
      // this will cancel the js forms implicitly
      return;
    }
    event.preventDefault();
    // cancel all js forms first
    let cancelJsForms: JQuery.Promise<void> = $.resolvedPromise();
    if (jsForms.length) {
      cancelJsForms = this.widget.cancelViews(jsForms);
    }
    // cancel remote forms after js forms are canceled (i.e. the promise was resolved) or the js-form-cancellation was cancelled (i.e. the promise was rejected)
    if (formIds.length) {
      cancelJsForms.always(() => this._send('cancelForms', {formIds}));
    }
  }

  protected _onMessageBoxShow(event: RemoteEvent) {
    let displayParent = this.session.getModelAdapter(event.displayParent);
    if (displayParent) {
      let messageBox = this.session.getOrCreateWidget(event.messageBox, displayParent.widget) as MessageBox;
      let parent = displayParent.widget as DisplayParent;
      messageBox.setDisplayParent(parent);
      parent.messageBoxController.registerAndRender(messageBox);
    }
  }

  protected _onMessageBoxHide(event: RemoteEvent) {
    let displayParent = this.session.getModelAdapter(event.displayParent);
    if (displayParent) {
      let messageBox = this.session.getModelAdapter(event.messageBox) as MessageBoxAdapter;
      let parent = displayParent.widget as DisplayParent;
      parent.messageBoxController.unregisterAndRemove(messageBox.widget);
    }
  }

  protected _onFileChooserShow(event: RemoteEvent) {
    let displayParent = this.session.getModelAdapter(event.displayParent);
    if (displayParent) {
      let parent = displayParent.widget as DisplayParent;
      let fileChooser = this.session.getOrCreateWidget(event.fileChooser, parent) as FileChooser;
      fileChooser.setDisplayParent(parent);
      parent.fileChooserController.registerAndRender(fileChooser);
    }
  }

  protected _onFileChooserHide(event: RemoteEvent) {
    let displayParent = this.session.getModelAdapter(event.displayParent);
    if (displayParent) {
      let fileChooser = this.session.getModelAdapter(event.fileChooser) as FileChooserAdapter;
      let parent = displayParent.widget as DisplayParent;
      parent.fileChooserController.unregisterAndRemove(fileChooser.widget);
    }
  }

  protected _onOpenUri(event: RemoteEvent) {
    this.widget.openUri(event.uri, event.action);
  }

  protected _onOutlineChanged(event: RemoteEvent) {
    let outline = this.session.getOrCreateWidget(event.outline, this.widget) as Outline;
    this.widget.setOutline(outline);
  }

  protected _onAddNotification(event: RemoteEvent) {
    let notification = this.session.getOrCreateWidget(event.notification, this.widget) as DesktopNotification;
    this.widget.addNotification(notification);
  }

  protected _onRemoveNotification(event: RemoteEvent) {
    this.widget.removeNotification(event.notification);
  }

  protected _onOutlineContentActivate(event: RemoteEvent) {
    this.widget.bringOutlineToFront();
  }

  override onModelAction(event: RemoteEvent) {
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
      super.onModelAction(event);
    }
  }

  /**
   * Static method to modify the prototype of Desktop.
   */
  static modifyDesktopPrototype() {
    if (!App.get().remote) {
      return;
    }

    objects.replacePrototypeFunction(Desktop, '_initTheme', DesktopAdapter._initTheme, true);
  }

  static _initTheme(this: Desktop & { _initThemeOrig; _activeTheme }) {
    if (this.modelAdapter) {
      // Write the active theme (defined by the UI server) to the desktop widget
      this.setTheme(this._activeTheme());
    } else {
      this._initThemeOrig();
    }
  }
}

App.addListener('bootstrap', DesktopAdapter.modifyDesktopPrototype);

