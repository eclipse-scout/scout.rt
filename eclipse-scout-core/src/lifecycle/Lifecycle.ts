/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, arrays, EventEmitter, InitModelOf, LifecycleEventMap, LifecycleModel, LifecycleValidateEvent, MessageBox, MessageBoxes, objects, ObjectWithType, scout, Session, SomeRequired, Status, StatusSeverity, Widget} from '../index';
import $ from 'jquery';

/**
 * Abstract base class for validation lifecycles as used for forms.
 * A subclass must set the properties, in order to display messages:
 * - emptyMandatoryElementsTextKey
 * - invalidElementsErrorTextKey
 * - invalidElementsWarningTextKey
 * - saveChangesQuestionTextKey
 */
export abstract class Lifecycle<TValidationResult extends { errorStatus?: Status }> extends EventEmitter implements LifecycleModel, ObjectWithType {
  declare model: LifecycleModel;
  declare initModel: SomeRequired<this['model'], 'widget'>;
  declare eventMap: LifecycleEventMap<TValidationResult>;
  declare self: Lifecycle<any>;

  objectType: string;
  widget: Widget;
  emptyMandatoryElementsTextKey: string;
  emptyMandatoryElementsText: string;
  invalidElementsErrorTextKey: string;
  invalidElementsErrorText: string;
  invalidElementsWarningTextKey: string;
  invalidElementsWarningText: string;
  saveChangesQuestionTextKey: string;
  askIfNeedSave: boolean;
  askIfNeedSaveText: string;
  handlers: Record<string, () => JQuery.Promise<void>>;

  constructor() {
    super();

    this.widget = null;
    this.emptyMandatoryElementsTextKey = null;
    this.emptyMandatoryElementsText = null;
    this.invalidElementsErrorTextKey = null;
    this.invalidElementsErrorText = null;
    this.invalidElementsWarningTextKey = null;
    this.invalidElementsWarningText = null;
    this.saveChangesQuestionTextKey = null;
    this.askIfNeedSave = true;
    this.askIfNeedSaveText = null;
    this.handlers = {
      'load': this._defaultLoad.bind(this),
      'save': this._defaultSave.bind(this)
    };
  }

  // Info: doExportXml, doImportXml, doSaveWithoutMarkerChange is not supported in Html UI

  init(model: InitModelOf<this>) {
    scout.assertParameter('widget', model.widget);
    $.extend(this, model);
    if (objects.isNullOrUndefined(this.emptyMandatoryElementsText)) {
      this.emptyMandatoryElementsText = this.session().text(this.emptyMandatoryElementsTextKey);
    }
    if (objects.isNullOrUndefined(this.invalidElementsErrorText)) {
      this.invalidElementsErrorText = this.session().text(this.invalidElementsErrorTextKey);
    }
    if (objects.isNullOrUndefined(this.invalidElementsWarningText)) {
      this.invalidElementsWarningText = this.session().text(this.invalidElementsWarningTextKey);
    }
    if (objects.isNullOrUndefined(this.askIfNeedSaveText)) {
      this.askIfNeedSaveText = this.session().text(this.saveChangesQuestionTextKey);
    }
  }

  load(): JQuery.Promise<void> {
    return this._load().then(() => {
      this.markAsSaved();
      this.trigger('postLoad');
    });
  }

  protected _load(): JQuery.Promise<void> {
    return this.handlers.load().then(() => {
      this.trigger('load');
    });
  }

  protected _defaultLoad(): JQuery.Promise<void> {
    return $.resolvedPromise();
  }

  ok(): JQuery.Promise<void> {
    // 1. validate form
    return this._validateAndHandle()
      .then(status => {
        if (!status.isValid()) {
          return;
        }

        // 2. check if save is required
        if (!this.saveNeeded()) {
          return this.close();
        }

        // 3. perform save operation
        return this._save()
          .then(() => {
            this.markAsSaved();
            return this.close();
          });
      });
  }

  cancel(): JQuery.Promise<void> {
    let showMessageBox = this.saveNeeded() && this.askIfNeedSave;
    if (showMessageBox) {
      return this._showYesNoCancelMessageBox(
        this.askIfNeedSaveText,
        this.ok.bind(this),
        this.close.bind(this));
    }
    return this.close();
  }

  protected _showYesNoCancelMessageBox(message: string, yesAction: () => JQuery.Promise<void>, noAction: () => JQuery.Promise<void>): JQuery.Promise<void> {
    return MessageBoxes.createYesNoCancel(this.widget)
      .withHeader(message)
      .buildAndOpen()
      .then(option => {
        if (option === MessageBox.Buttons.YES) {
          return yesAction();
        } else if (option === MessageBox.Buttons.NO) {
          return noAction();
        }
        return $.resolvedPromise();
      });
  }

  reset(): JQuery.Promise<void> {
    this._reset();

    // reload the state
    return this.load().then(() => {
      this.trigger('reset');
    });
  }

  close(): JQuery.Promise<void> {
    return this._close();
  }

  protected _close(): JQuery.Promise<void> {
    this.trigger('close');
    return $.resolvedPromise();
  }

  save(): JQuery.Promise<void> {
    // 1. validate form
    return this._validateAndHandle()
      .then(status => {
        // 2. invalid or form has not been changed
        if (!status.isValid() || !this.saveNeeded()) {
          return;
        }

        // 3. perform save operation
        return this._save()
          .then(() => this.markAsSaved());
      });
  }

  protected _reset() {
    // NOP
  }

  protected _save(): JQuery.Promise<void> {
    return this.handlers.save().then(() => {
      this.trigger('save');
    });
  }

  protected _defaultSave(): JQuery.Promise<void> {
    return $.resolvedPromise();
  }

  markAsSaved() {
    // NOP
  }

  /**
   * Override this function to check if any data has changed and saving is required.
   */
  saveNeeded(): boolean {
    return false;
  }

  setAskIfNeedSave(askIfNeedSave: boolean) {
    this.askIfNeedSave = askIfNeedSave;
  }

  protected _validateAndHandle(): JQuery.Promise<Status> {
    return this._validate()
      .then(status => {
        const event = this.trigger('validate', {status}) as LifecycleValidateEvent<TValidationResult>;
        return event.status;
      })
      .then(status => {
        if (!status || status.isValid()) {
          return $.resolvedPromise(status || Status.ok());
        }
        return this._handleInvalid(status);
      })
      .catch(error => {
        const errorHandler = App.get().errorHandler;
        return errorHandler.analyzeError(error)
          .then(errorInfo => errorHandler.errorInfoToStatus(this.session(), errorInfo))
          .then(status => this._handleInvalid(status));
      });
  }

  protected _handleInvalid(status: Status): JQuery.Promise<Status> {
    return $.resolvedPromise(status); // default no handling
  }

  /**
   * @returns a promise resolved with the validation result as {@link Status}.
   */
  validate(): JQuery.Promise<Status> {
    return this._validateAndHandle();
  }

  protected _validate(): JQuery.Promise<Status> {
    let status = this._validateElements();
    if (!status.isValid()) {
      return $.resolvedPromise(status);
    }
    let statusOrPromise = this._validateWidget();
    if (objects.isPromise(statusOrPromise)) {
      return statusOrPromise;
    }
    return $.resolvedPromise(statusOrPromise);
  }

  /**
   * Validates all elements (i.e. form-fields) covered by the lifecycle and checks for missing or invalid elements.
   */
  protected _validateElements(): Status {
    let elements = this.invalidElements();
    if (elements.missingElements.length === 0 && elements.invalidElements.length === 0) {
      return Status.ok();
    }
    const severity = elements.missingElements.length
        ? Status.Severity.ERROR
        : arrays.max(elements.invalidElements.map(e => e.errorStatus ? e.errorStatus.severity : 0)) as StatusSeverity,
      message = this._createInvalidElementsMessageHtml(elements.missingElements, elements.invalidElements);
    this._revealInvalidElement(arrays.first(elements.missingElements) || arrays.first(elements.invalidElements));
    return Status.ensure({severity, message});
  }

  protected _revealInvalidElement(invalidElement: TValidationResult) {
    // NOP
  }

  /**
   * Validates the widget (i.e. form) associated with this lifecycle. This function is only called when there are
   * no missing or invalid elements. It is used to implement an overall-validate logic which has nothing to do
   * with a specific element or field. For instance, you could validate if an internal member variable of a Lifecycle
   * or Form is set.
   */
  protected _validateWidget(): Status | JQuery.Promise<Status> {
    return Status.ok();
  }

  /**
   * Override this function to check for invalid elements on the parent which prevent saving of the parent (e.g. check if all mandatory elements contain a value).
   */
  protected invalidElements(): { missingElements: TValidationResult[]; invalidElements: TValidationResult[] } {
    return {
      missingElements: [],
      invalidElements: []
    };
  }

  /**
   * Creates an HTML message used to display missing and invalid fields in a message box.
   */
  protected _createInvalidElementsMessageHtml(missing: TValidationResult[], invalid: TValidationResult[]): string {
    const $div = $('<div>'),
      hasMissing = missing.length > 0,
      invalidError = [], invalidWarning = [];

    invalid.forEach(e => {
      if (!e.errorStatus) {
        return;
      }
      if (e.errorStatus.isError()) {
        invalidError.push(e);
      } else if (e.errorStatus.isWarning()) {
        invalidWarning.push(e);
      }
    });

    const hasInvalidError = invalidError.length > 0,
      hasInvalidWarning = invalidWarning.length > 0;

    let appendBr = false;

    if (hasMissing) {
      appendTitleAndList.call(this, $div, this.emptyMandatoryElementsText, missing, this._missingElementText);
      appendBr = true;
    }
    if (hasInvalidError) {
      if (appendBr) {
        $div.appendElement('<br>');
      }
      appendTitleAndList.call(this, $div, this.invalidElementsErrorText, invalidError, this._invalidElementErrorText);
      appendBr = true;
    }
    if (hasInvalidWarning) {
      if (appendBr) {
        $div.appendElement('<br>');
      }
      appendTitleAndList.call(this, $div, this.invalidElementsWarningText, invalidWarning, this._invalidElementWarningText);
      appendBr = true;
    }
    return $div.html();

    // ----- Helper function -----

    function appendTitleAndList($div: JQuery, title: string, elements: TValidationResult[], elementTextFunc: (element: TValidationResult) => string) {
      $div.appendDiv().text(title);
      let $ul = $div.appendElement('<ul>');
      elements.forEach(element => {
        $ul.appendElement('<li>').text(elementTextFunc.call(this, element));
      });
    }
  }

  /**
   * Override this function to retrieve the text of an invalid element
   */
  protected _invalidElementText(element: TValidationResult): string {
    return '';
  }

  protected _invalidElementErrorText(element: TValidationResult): string {
    return this._invalidElementText(element);
  }

  protected _invalidElementWarningText(element: TValidationResult): string {
    return this._invalidElementText(element);
  }

  /**
   * Override this function to retrieve the text of a missing mandatory element
   */
  protected _missingElementText(element: TValidationResult): string {
    return '';
  }

  session(): Session {
    return this.widget.session;
  }

  /**
   * Register a handler function for save actions.
   * All handler functions must return a Status. In case of an error a Status object with severity error must be returned.
   * Note: in contrast to events, handlers can control the flow of the lifecycle. They also have a return value where events have none.
   *   Only one handler can be registered for each type.
   */
  handle(type: 'load' | 'save', func: () => JQuery.Promise<void>) {
    let supportedTypes = ['load', 'save'];
    if (supportedTypes.indexOf(type) === -1) {
      throw new Error('Cannot register handler for unsupported type \'' + type + '\'');
    }
    this.handlers[type] = func;
  }
}
