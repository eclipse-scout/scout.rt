/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  App, arrays, ElementsValidationStatus, EventEmitter, InitModelOf, LifecycleEventMap, LifecycleModel, LifecycleValidateEvent, MessageBox, MessageBoxes, objects, ObjectWithType, promises, scout, Session, SomeRequired, Status,
  StatusSeverity, Widget
} from '../index';
import $ from 'jquery';

/**
 * Abstract base class for validation lifecycles as used for forms.
 * A subclass must set the properties, in order to display messages:
 * - emptyMandatoryElementsTextKey
 * - invalidElementsErrorTextKey
 * - invalidElementsWarningTextKey
 * - saveChangesQuestionTextKey
 */
export abstract class Lifecycle<TValidationResult extends { errorStatus?: Status; promise?: JQuery.Promise<void> }> extends EventEmitter implements LifecycleModel, ObjectWithType {
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
        this._okAfterAskIfSaveNeeded.bind(this),
        this.close.bind(this));
    }
    return this.close();
  }

  protected _okAfterAskIfSaveNeeded(): JQuery.Promise<void> {
    return this.ok();
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
      .then(status => this._handleValidationResult(status))
      .catch(error => {
        const errorHandler = App.get().errorHandler;
        return errorHandler.analyzeError(error)
          .then(errorInfo => errorHandler.errorInfoToStatus(this.session(), errorInfo))
          .then(status => this._handleInvalid(status));
      });
  }

  protected _handleValidationResult(status: Status): JQuery.Promise<Status> {
    // 1. reveal first invalid element
    if (status instanceof ElementsValidationStatus && (status.elementsValidationResult.missingElements.length || status.elementsValidationResult.invalidElements.length)) {
      this._revealInvalidElement(arrays.first(status.elementsValidationResult.missingElements) || arrays.first(status.elementsValidationResult.invalidElements));
    }

    // 2. trigger event (allow modification of status)
    const event = this.trigger('validate', {status}) as LifecycleValidateEvent<TValidationResult>;
    status = event.status || Status.ok();
    if (status.isValid()) {
      return $.resolvedPromise(status);
    }

    // 3. handle if warning or error
    return this._handleInvalid(status);
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
    return this._validateElements().then(elementStatus => {
      if (elementStatus.isError()) {
        return elementStatus;
      }
      const widgetValidation = this._validateWidget();
      return promises.ensure(widgetValidation)
        .then(widgetStatus => this._combineValidationStatuses(elementStatus, widgetStatus));
    });
  }

  protected _combineValidationStatuses(elementStatus: Status, widgetStatus: Status): Status {
    return Status.ok().addStatuses(elementStatus, widgetStatus); // returned root status has severity of worst child status (max severity).
  }

  /**
   * Validates all elements (i.e. form-fields) covered by the lifecycle and checks for missing or invalid elements.
   */
  protected _validateElements(): JQuery.Promise<Status> {
    const validationResult = this.invalidElements();
    if (validationResult.pendingElements.length > 0) {
      return $.promiseAll(validationResult.pendingElements.map(element => element.promise))
        .then(() => this._validateElements());
    }

    let severity: StatusSeverity;
    let message: string;
    if (validationResult.missingElements.length === 0 && validationResult.invalidElements.length === 0) {
      severity = Status.Severity.OK;
    } else {
      severity = validationResult.missingElements.length
        ? Status.Severity.ERROR
        : arrays.max(validationResult.invalidElements.map(e => e.errorStatus ? e.errorStatus.severity : 0)) as StatusSeverity;
      message = this._createInvalidElementsMessageHtml(validationResult.missingElements, validationResult.invalidElements);
    }
    return $.resolvedPromise(scout.create(ElementsValidationStatus<TValidationResult>, {severity, message, elementsValidationResult: validationResult}));
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
  invalidElements(): ElementsValidationResult<TValidationResult> {
    return {
      missingElements: [],
      invalidElements: [],
      pendingElements: []
    };
  }

  /**
   * Creates an HTML message used to display missing and invalid fields in a message box.
   */
  protected _createInvalidElementsMessageHtml(missing: TValidationResult[], invalid: TValidationResult[]): string {
    const groupedBySeverity = arrays.groupBy(invalid, e => e.errorStatus?.severity);
    const invalidError = groupedBySeverity.get(Status.Severity.ERROR);
    const invalidWarning = groupedBySeverity.get(Status.Severity.WARNING);
    const messageGroups = [
      {
        title: this.emptyMandatoryElementsText,
        elements: missing?.map(m => this._missingElementText.call(this, m))
      }, {
        title: this.invalidElementsErrorText,
        elements: invalidError?.map(m => this._invalidElementErrorText.call(this, m))
      }, {
        title: this.invalidElementsWarningText,
        elements: invalidWarning?.map(m => this._invalidElementWarningText.call(this, m))
      }
    ];
    return Lifecycle.createUnorderedListsWithTitle(messageGroups);
  }

  /**
   * @param lists The text list to render. Includes the title of the list and if html should be allowed in title and texts. Must be an array.
   * @param createBulletsOnSingleElement Optional flag to indicate if a bullet list should also be created if there is only one text element in a list. Default is true.
   */
  static createUnorderedListsWithTitle(lists: TextListWithTitle[], createBulletsOnSingleElement = true): string {
    const $div = $('<div>');
    let appendBr = false;
    for (const list of lists) {
      const elements = list.elements?.filter(e => !!e);
      if (!elements?.length) {
        continue; // skip empty lists
      }

      if (appendBr) {
        $div.appendElement('<br>');
      }
      const applyText: ($e: JQuery, s: string) => JQuery = list.html ? ($e, s) => $e.html(s) : ($e, s) => $e.text(s);
      if (list.title) {
        applyText($div.appendDiv(), list.title);
      }

      if (!createBulletsOnSingleElement && elements.length === 1) {
        applyText($div.appendDiv(), elements[0]);
      } else {
        let $ul = $div.appendElement('<ul>');
        elements.forEach(e => applyText($ul.appendElement('<li>'), e));
      }

      appendBr = true;
    }
    return $div.html();
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

export type ElementsValidationResult<TValidationResult extends { errorStatus?: Status }> = {
  missingElements: TValidationResult[];
  invalidElements: TValidationResult[];
  pendingElements: TValidationResult[];
};

export type TextListWithTitle = {
  title: string;
  elements: string[];
  html?: boolean;
};
