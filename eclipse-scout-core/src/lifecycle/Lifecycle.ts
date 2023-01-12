/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, EventEmitter, InitModelOf, LifecycleEventMap, LifecycleModel, MessageBox, MessageBoxes, objects, ObjectWithType, scout, Session, SomeRequired, Status, Widget} from '../index';
import $ from 'jquery';

/**
 * Abstract base class for validation lifecycles as used for forms.
 * A subclass must set the properties, in order to display messages:
 * - emptyMandatoryElementsTextKey
 * - invalidElementsTextKey
 * - saveChangesQuestionTextKey
 */
export abstract class Lifecycle<TValidationResult> extends EventEmitter implements LifecycleModel, ObjectWithType {
  declare model: LifecycleModel;
  declare initModel: SomeRequired<this['model'], 'widget'>;
  declare eventMap: LifecycleEventMap<TValidationResult>;
  declare self: Lifecycle<any>;

  objectType: string;
  widget: Widget;
  validationFailedTextKey: string;
  validationFailedText: string;
  emptyMandatoryElementsTextKey: string;
  emptyMandatoryElementsText: string;
  invalidElementsTextKey: string;
  invalidElementsText: string;
  saveChangesQuestionTextKey: string;
  askIfNeedSave: boolean;
  askIfNeedSaveText: string;
  handlers: Record<string, () => JQuery.Promise<Status>>;

  constructor() {
    super();

    this.widget = null;
    this.validationFailedTextKey = null;
    this.validationFailedText = null;
    this.emptyMandatoryElementsTextKey = null;
    this.emptyMandatoryElementsText = null;
    this.invalidElementsTextKey = null;
    this.invalidElementsText = null;
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
    if (objects.isNullOrUndefined(this.validationFailedText)) {
      this.validationFailedText = this.session().text(this.validationFailedTextKey);
    }
    if (objects.isNullOrUndefined(this.emptyMandatoryElementsText)) {
      this.emptyMandatoryElementsText = this.session().text(this.emptyMandatoryElementsTextKey);
    }
    if (objects.isNullOrUndefined(this.invalidElementsText)) {
      this.invalidElementsText = this.session().text(this.invalidElementsTextKey);
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
    return this.handlers.load()
      .then(status => {
        this.trigger('load');
      });
  }

  protected _defaultLoad(): JQuery.Promise<Status> {
    return $.resolvedPromise();
  }

  ok(): JQuery.Promise<void> {
    // 1. validate form
    return this._whenInvalid(this._validate)
      .then(invalid => {
        if (invalid) {
          return;
        }

        // 2. check if save is required
        if (!this.requiresSave()) {
          return this.close();
        }

        // 3. perform save operation
        return this._whenInvalid(this._save)
          .then(invalid => {
            if (invalid) {
              return;
            }

            this.markAsSaved();
            return this.close();
          });
      });
  }

  cancel(): JQuery.Promise<void> {
    let showMessageBox = this.requiresSave() && this.askIfNeedSave;
    if (showMessageBox) {
      return this._showYesNoCancelMessageBox(
        this.askIfNeedSaveText,
        this.ok.bind(this),
        this.close.bind(this));
    }
    return this.close();
  }

  reset(): JQuery.Promise<void> {
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
    return this._whenInvalid(this._validate)
      .then(invalid => {

        // 2. invalid or form has not been changed
        if (invalid || !this.requiresSave()) {
          return;
        }

        // 3. perform save operation
        return this._whenInvalid(this._save)
          .then(invalid => {
            if (invalid) {
              return;
            }

            this.markAsSaved();
          });
      });
  }

  protected _save(): JQuery.Promise<Status> {
    return this.handlers.save()
      .then(status => {
        this.trigger('save');
        return status;
      });
  }

  protected _defaultSave(): JQuery.Promise<Status> {
    return $.resolvedPromise();
  }

  markAsSaved() {
    // NOP
  }

  /**
   * Override this function to check if any data has changed and saving is required.
   */
  requiresSave(): boolean {
    return false;
  }

  setAskIfNeedSave(askIfNeedSave: boolean) {
    this.askIfNeedSave = askIfNeedSave;
  }

  /**
   * Helper function to deal with functions that return a Status object.
   * Makes it easier to return early when that function returns an invalid status (= less code to write).
   *
   * @returns If the resulting promise is resolved with "true", the life cycle is considered invalid.
   *                    Otherwise, the life cycle is considered valid and the store/save operation continues.
   *                    If the status returned by 'func' is absent or Status.Severity.OK, a promise resolved with
   *                    "false" is returned. Otherwise, the promise returned by _showStatusMessageBox() is returned.
   */
  protected _whenInvalid(func: () => JQuery.Promise<Status>): JQuery.Promise<boolean> {
    return func.call(this)
      .then(status => {
        if (!status || status.severity === Status.Severity.OK) {
          return $.resolvedPromise(false); // invalid=false
        }
        return this._showStatusMessageBox(status);
      })
      .catch(error => this._showStatusMessageBox(errorToStatus(error)));

    // See ValueField#_createInvalidValueStatus, has similar code to transform error to status
    function errorToStatus(error: Status | string | { message: string }): Status {
      if (error instanceof Status) {
        return error;
      }
      if (typeof error === 'string') {
        return Status.error({
          message: error
        });
      }
      return Status.error({
        message: error.message
      });
    }
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

  /**
   * @returns If the resulting promise is resolved with "true", the life cycle is considered invalid.
   *                    Otherwise, the life cycle is considered valid and the store/save operation continues.
   *                    By default, a promise that is resolved with "true" is returned.
   */
  protected _showStatusMessageBox(status: Status): JQuery.Promise<boolean> {
    return MessageBoxes.createOk(this.widget)
      .withSeverity(status.severity)
      .withHeader(this.validationFailedText)
      .withBody(status.message, true)
      .buildAndOpen()
      .then(() => {
        let invalid = (status.severity === Status.Severity.ERROR);
        return $.resolvedPromise(invalid);
      });
  }

  protected _validate(): JQuery.Promise<Status> {
    let status = this._validateElements();
    if (status.isValid()) {
      status = this._validateWidget();
    }
    return $.resolvedPromise(status);
  }

  /**
   * Validates all elements (i.e. form-fields) covered by the lifecycle and checks for missing or invalid elements.
   */
  protected _validateElements(): Status {
    let elements = this.invalidElements();
    let status = new Status();
    if (elements.missingElements.length === 0 && elements.invalidElements.length === 0) {
      status.severity = Status.Severity.OK;
    } else {
      status.severity = Status.Severity.ERROR;
      status.message = this._createInvalidElementsMessageHtml(elements.missingElements, elements.invalidElements);
      this._revealInvalidElement(arrays.first(elements.missingElements) || arrays.first(elements.invalidElements));
    }
    return status;
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
  protected _validateWidget(): Status {
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
    let $div = $('<div>'),
      hasMissing = missing.length > 0,
      hasInvalid = invalid.length > 0;
    if (hasMissing) {
      appendTitleAndList.call(this, $div, this.emptyMandatoryElementsText, missing, this._missingElementText);
    }
    if (hasMissing && hasInvalid) {
      $div.appendElement('<br>');
    }
    if (hasInvalid) {
      appendTitleAndList.call(this, $div, this.invalidElementsText, invalid, this._invalidElementText);
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
  handle(type: 'load' | 'save', func: () => JQuery.Promise<Status>) {
    let supportedTypes = ['load', 'save'];
    if (supportedTypes.indexOf(type) === -1) {
      throw new Error('Cannot register handler for unsupported type \'' + type + '\'');
    }
    this.handlers[type] = func;
  }
}
