/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, EventSupport, MessageBox, MessageBoxes, objects, scout, Status} from '../index';
import $ from 'jquery';

/**
 * Abstract base class for validation lifecycles as used for forms.
 * A subclass must set the properties, in order to display messages:
 * - emptyMandatoryElementsTextKey
 * - invalidElementsTextKey
 * - saveChangesQuestionTextKey
 *
 * @class
 * @constructor
 * @abstract
 */
export default class Lifecycle {

  constructor() {
    this.widget = null;
    this.validationFailedTextKey = null;
    this.validationFailedText = null;

    this.emptyMandatoryElementsTextKey = null;
    this.emptyMandatoryElementsText = null;

    this.invalidElementsTextKey = null;
    this.invalidElementsText = null;

    this.saveChangesQuestionTextKey = null;
    this.askIfNeedSave = true;
    this.askIfNeedSaveText = null; // Java: cancelVerificationText

    this.events = new EventSupport();
    this.handlers = {
      'load': this._defaultLoad.bind(this),
      'save': this._defaultSave.bind(this)
    };
  }

  // Info: doExportXml, doImportXml, doSaveWithoutMarkerChange is not supported in Html UI

  init(model) {
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

  load() {
    return this._load().then(() => {
      this.markAsSaved();
      this.events.trigger('postLoad');
    });
  }

  /**
   * @returns {Promise}
   */
  _load() {
    return this.handlers.load()
      .then(status => {
        this.events.trigger('load');
      });
  }

  /**
   * @returns {Promise}
   */
  _defaultLoad() {
    return $.resolvedPromise();
  }

  /**
   * @returns {Promise}
   */
  ok() {
    // 1.) validate form
    return this._whenInvalid(this._validate)
      .then(invalid => {
        if (invalid) {
          return;
        }

        // 2.) check if save is required
        if (!this.requiresSave()) {
          return this.close();
        }

        // 3.) perform save operation
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

  /**
   * @returns {Promise}
   */
  cancel() {
    let showMessageBox = this.requiresSave() && this.askIfNeedSave;
    if (showMessageBox) {
      return this._showYesNoCancelMessageBox(
        this.askIfNeedSaveText,
        this.ok.bind(this),
        this.close.bind(this));
    }
    return this.close();
  }

  /**
   * @returns {Promise}
   */
  reset() {
    this._reset();

    // reload the state
    return this.load().then(() => {
      this.events.trigger('reset');
    });
  }

  /**
   * @returns {Promise}
   */
  close() {
    return this._close();
  }

  /**
   * @returns {Promise}
   */
  _close() {
    this.events.trigger('close');
    return $.resolvedPromise();
  }

  /**
   * @returns {Promise}
   */
  save() {
    // 1.) validate form
    return this._whenInvalid(this._validate)
      .then(invalid => {

        // 2.) invalid or form has not been changed
        if (invalid || !this.requiresSave()) {
          return;
        }

        // 3.) perform save operation
        return this._whenInvalid(this._save)
          .then(invalid => {
            if (invalid) {
              return;
            }

            this.markAsSaved();
          });
      });
  }

  /**
   * @returns {Promise}
   */
  _save() {
    return this.handlers.save()
      .then(status => {
        this.events.trigger('save');
        return status;
      });
  }

  /**
   * @returns {Promise}
   */
  _defaultSave() {
    return $.resolvedPromise();
  }

  markAsSaved() {
    // NOP
  }

  /**
   * Override this function to check if any data has changed and saving is required.
   * @returns {boolean}
   */
  requiresSave() {
    return false;
  }

  setAskIfNeedSave(askIfNeedSave) {
    this.askIfNeedSave = askIfNeedSave;
  }

  /**
   * Helper function to deal with functions that return a Status object.
   * Makes it easier to return early when that function returns an invalid status (= less code to write).
   *
   * @returns {Promise} If the resulting promise is resolved with "true", the life cycle is considered invalid.
   *                    Otherwise, the life cycle is considered valid and the store/save operation continues.
   *                    If the status returned by 'func' is absent or Status.Severity.OK, a promise resolved with
   *                    "false" is returned. Otherwise, the promise returned by _showStatusMessageBox() is returned.
   */
  _whenInvalid(func) {
    return func.call(this)
      .then(status => {
        if (!status || status.severity === Status.Severity.OK) {
          return $.resolvedPromise(false); // invalid=false
        }
        return this._showStatusMessageBox(status);
      })
      .catch(error => {
        return this._showStatusMessageBox(errorToStatus(error));
      });

    // See ValueField#_createInvalidValueStatus, has similar code to transform error to status
    function errorToStatus(error) {
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

  _showYesNoCancelMessageBox(message, yesAction, noAction) {
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
   * @param status
   * @returns {Promise} If the resulting promise is resolved with "true", the life cycle is considered invalid.
   *                    Otherwise, the life cycle is considered valid and the store/save operation continues.
   *                    By default, a promise that is resolved with "true" is returned.
   */
  _showStatusMessageBox(status) {
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

  /**
   * @returns {Promise}
   */
  _validate() {
    let status = this._validateElements();
    if (status.isValid()) {
      status = this._validateWidget();
    }
    return $.resolvedPromise(status);
  }

  /**
   * Validates all elements (i.e form-fields) covered by the lifecycle and checks for missing or invalid elements.
   *
   * @return Status
   */
  _validateElements() {
    let elements = this._invalidElements();
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

  _revealInvalidElement(invalidElement) {
    // NOP
  }

  /**
   * Validates the widget (i.e. form) associated with this lifecycle. This function is only called when there are
   * no missing or invalid elements. It is used to implement an overall-validate logic which has nothing to do
   * with a specific element or field. For instance you could validate if an internal member variable of a Lifecycle
   * or Form is set.
   *
   * @return Status
   */
  _validateWidget() {
    return Status.ok();
  }

  /**
   * Override this function to check for invalid elements on the parent which prevent
   * saving of the parent.(eg. check if all mandatory elements contain a value)
   *
   * @returns Object with
   * missingElements: Elements which should have a value
   * invalidElements: Elements which have an invalid value
   */
  _invalidElements() {
    return {
      missingElements: [],
      invalidElements: []
    };
  }

  /**
   * Creates a HTML message used to display missing and invalid fields in a message box.
   */
  _createInvalidElementsMessageHtml(missing, invalid) {
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

    function appendTitleAndList($div, title, elements, elementTextFunc) {
      $div.appendDiv().text(title);
      let $ul = $div.appendElement('<ul>');
      elements.forEach(function(element) {
        $ul.appendElement('<li>').text(elementTextFunc.call(this, element));
      }, this);
    }
  }

  /**
   * Override this function to retrieve the text of an invalid element
   * @param element
   * @returns {String}
   */
  _invalidElementText(element) {
    return '';
  }

  /**
   * Override this function to retrieve the text of an missing mandatory element
   * @param element
   * @returns {String}
   */
  _missingElementText(element) {
    return '';
  }

  session() {
    return this.widget.session;
  }

  /**
   * Register a handler function for save actions.
   * All handler functions must return a Status. In case of an error a Status object with severity error must be returned.
   * Note: in contrast to events, handlers can control the flow of the lifecycle. They also have a return value where events have none.
   *   Only one handler can be registered for each type.
   */
  handle(type, func) {
    let supportedTypes = ['load', 'save'];
    if (supportedTypes.indexOf(type) === -1) {
      throw new Error('Cannot register handler for unsupported type \'' + type + '\'');
    }
    this.handlers[type] = func;
  }

  /**
   * Register an event handler for the given type.
   * Event handlers don't have a return value. They do not have any influence on the lifecycle flow. There can be multiple event
   * handler for each type.
   */
  on(type, func) {
    return this.events.on(type, func);
  }

  off(type, func) {
    return this.events.off(type, func);
  }
}
