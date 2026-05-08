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
  AbstractLayout, App, aria, arrays, EnumObject, focusUtils, FormField, InitModelOf, objects, ParsingFailedStatus, promises, scout, Status, StatusSeverity, StatusType, strings, ValidationFailedStatus, ValueFieldEventMap, ValueFieldModel
} from '../../index';
import $ from 'jquery';

export class ValueField<TValue extends TModelValue, TModelValue = TValue> extends FormField implements ValueFieldModel<TValue, TModelValue> {
  declare model: ValueFieldModel<TValue, TModelValue>;
  declare eventMap: ValueFieldEventMap<TValue>;
  declare self: ValueField<any>;

  clearable: ValueFieldClearable;
  formatter: ValueFieldFormatter<TValue>;
  hasText: boolean;
  /**
   * The initial value is used to determine whether the field needs to be saved (see {@link computeSaveNeeded}) and is used to reset the value when {@link ValueField.resetValue} is called.
   * It will be set to the {@link value} during initialization of the field and whenever {@link markAsSaved} is called.
   */
  initialValue: TValue;
  invalidValueMessageKey: string;
  parser: ValueFieldParser<TValue>;
  value: TValue;
  validators: ValueFieldValidator<TValue>[];
  validatePending: boolean;
  protected _validatePendingCounter: number;
  protected _updateDisplayTextPending: boolean;

  constructor() {
    super();

    this.defaultMenuTypes = [...this.defaultMenuTypes, ValueField.MenuType.NotNull, ValueField.MenuType.Null];
    this.clearable = ValueField.Clearable.FOCUSED;
    this.displayText = null;
    this.formatter = this._formatValue.bind(this);
    this.hasText = false;
    this.initialValue = null;
    this.invalidValueMessageKey = 'InvalidValueMessageX';
    this.parser = this._parseValue.bind(this);
    this.value = null;
    this.validators = [];
    this.validators.push(this._validateValue.bind(this));
    this._validatePendingCounter = 0;
    this._updateDisplayTextPending = false;
    this.$clearIcon = null;

    this._addCloneProperties(['value', 'displayText', 'clearable']);
  }

  static Clearable = {
    /**
     * The clear icon is shown when the field has text.
     */
    ALWAYS: 'always',
    /**
     * The clear icon will be shown when the field is focused and has text.
     */
    FOCUSED: 'focused',
    /**
     * Never show the clear icon.
     */
    NEVER: 'never'
  } as const;

  static MenuType = {
    Null: 'ValueField.Null',
    NotNull: 'ValueField.NotNull'
  } as const;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    if (model.validator) {
      // Validators are kept in a list, allow a single validator to be set in the model, similar to parser and formatter.
      // setValidator will add the new validator to this.validators and remove the other ones.
      this.setValidator(model.validator);
      delete model.validator;
    }
    this._initValue(this.value);
    this.initialValue = this.value;
  }

  /**
   * Override this method if you need to influence the value initialization (e.g. do something before the value is initially set)
   */
  protected _initValue(value: TValue) {
    // Delete value first, value may be invalid and must not be set
    this.value = null;
    this._setValue(value);
    this._updateEmpty();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderDisplayText();
    this._renderClearable();
    this._renderHasText();
  }

  protected override _remove() {
    super._remove();
    this.$clearIcon = null;
  }

  /**
   * The default impl. is a NOP, because not every ValueField has a sensible display text.
   */
  protected _renderDisplayText() {
    this._updateHasText();
  }

  /**
   * The default impl. returns this.displayText or empty string if displayText is null.
   */
  protected _readDisplayText(): string {
    return scout.nvl(this.displayText, '');
  }

  protected _onClearIconMouseDown(event: JQuery.MouseDownEvent) {
    this.clear();
    event.preventDefault();
  }

  protected override _onFieldBlur(event: JQuery.BlurEvent) {
    super._onFieldBlur(event);
    this.acceptInput(false);
  }

  /**
   * Accepts the current input and writes it to the model.
   *
   * This method is typically called by the {@link _onFieldBlur} function of the field, but may actually be called from anywhere (e.g. button, actions, cell editor, etc.).
   * It is also called by the {@link aboutToBlurByMouseDown} function, which is required because our Ok- and Cancel-buttons are not focusable (thus {@link _onFieldBlur} is
   * never called) but changes in the value-field must be sent to the server anyway when a button is clicked.
   *
   * The default reads the display text using {@link _readDisplayText} and writes it to the model by calling {@link _triggerAcceptInput}.
   * If subclasses don't have a display-text or want to write another state to the server, they may override this method.
   */
  acceptInput(whileTyping?: boolean): JQuery.Promise<void> | void {
    whileTyping = !!whileTyping; // cast to boolean
    let displayText = scout.nvl(this._readDisplayText(), '');

    // trigger only if displayText has really changed
    if (this._checkDisplayTextChanged(displayText, whileTyping)) {
      // Don't call setDisplayText() to prevent re-rendering of display text (which is unnecessary and
      // might change the cursor position). Don't call _callSetProperty() as well, as this eventually
      // executes this._setDisplayText(), which updates the value.
      this._setProperty('displayText', displayText);
      if (!whileTyping) {
        let result = this.parseAndSetValue(displayText);
        if (objects.isPromise(result)) {
          return result.then(() => this._triggerAcceptInput(whileTyping));
        }
      }
      // Display text may be formatted -> Use this.displayText
      this._triggerAcceptInput(whileTyping);
    }
  }

  /**
   * Parses the text using {@link parseValue} and sets the result using {@link setValue}.
   *
   * @returns `void` or a promise if an asynchronous validator has been called (see {@link ValueFieldModel.validators})
   */
  parseAndSetValue(displayText: string): JQuery.Promise<void> | void {
    this.removeErrorStatus(ParsingFailedStatus);
    let parsedValue;
    try {
      let event = this.trigger('parse', {
        displayText: displayText
      });
      if (!event.defaultPrevented) {
        parsedValue = this.parseValue(displayText);
      }
    } catch (error) {
      this._parsingFailed(displayText, error);
      return;
    }
    return this.setValue(parsedValue);
  }

  protected _parsingFailed(displayText: string, error: any) {
    $.log.isInfoEnabled() && $.log.info('Parsing failed for field with id ' + this.id, error);
    let event = this.trigger('parseError', {
      displayText: displayText,
      error: error
    });
    if (!event.defaultPrevented) {
      this._addParsingFailedErrorStatus(displayText, error);
    }
  }

  protected _addParsingFailedErrorStatus(displayText: string, error: any) {
    let status = this._createParsingFailedStatus(displayText, error);
    this.addErrorStatus(status);
  }

  protected _createParsingFailedStatus(displayText: string, error: any): Status {
    return this._createInvalidValueStatus('ParsingFailedStatus', displayText, error);
  }

  /**
   * Replaces the existing parser. The parser is called during {@link parseValue}.
   *
   * Remember calling the default parser passed as parameter to the parse function, if needed.
   * @param parser the new parser. If null, the default parser is used.
   *
   * @see ValueFieldModel.parser
   */
  setParser(parser: ValueFieldParser<TValue>) {
    this.setProperty('parser', parser);
    if (this.initialized) {
      this.parseAndSetValue(this.displayText);
    }
  }

  protected _setParser(parser: ValueFieldParser<TValue>) {
    if (!parser) {
      parser = this._parseValue.bind(this);
    }
    this._setProperty('parser', parser);
  }

  /**
   * @returns the parsed value
   * @throws a message, a {@link Status} or an error if the parsing fails
   */
  parseValue(displayText: string): TValue {
    let defaultParser = this._parseValue.bind(this);
    return this.parser(displayText, defaultParser);
  }

  /**
   * @throws a message, a {@link Status} or an error if the parsing fails
   */
  protected _parseValue(displayText: string): TValue {
    return displayText as TValue;
  }

  protected _checkDisplayTextChanged(displayText: string, whileTyping?: boolean): boolean {
    let oldDisplayText = scout.nvl(this.displayText, '');
    return displayText !== oldDisplayText;
  }

  /**
   * Method invoked upon a mousedown click with this field as the currently focused control, and is invoked just before the mousedown click will be interpreted.
   * However, the mousedown target must not be this control, but any other control instead.
   *
   * The default implementation checks, whether the click occurred outside this control, and if so invokes 'ValueField.acceptInput'.
   *
   * @param target
   *        the DOM target where the mouse down event occurred.
   */
  aboutToBlurByMouseDown(target: Element) {
    let eventOnField = this.isFocusOnField(target);
    if (!eventOnField) {
      this.acceptInput(); // event outside this value field.
    }
  }

  override isFocused(): boolean {
    return this.rendered && focusUtils.isActiveElement(this.$field);
  }

  isFocusOnField(target: Element): boolean {
    return this.$field.isOrHas(target) || (this.$clearIcon && this.$clearIcon.isOrHas(target));
  }

  /** @internal */
  _triggerAcceptInput(whileTyping: boolean) {
    let event = {
      displayText: this.displayText,
      whileTyping: !!whileTyping
    };
    this.trigger('acceptInput', event);
  }

  /** @see ValueFieldModel.displayText */
  setDisplayText(displayText: string) {
    this.setProperty('displayText', displayText);
  }

  protected _updateHasText() {
    this.setHasText(strings.hasText(this._readDisplayText()));
  }

  setHasText(hasText: boolean) {
    this.setProperty('hasText', hasText);
  }

  protected _renderHasText() {
    if (this.$field) {
      this.$field.toggleClass('has-text', this.hasText);
    }
    this.$container.toggleClass('has-text', this.hasText);
  }

  /** @see ValueFieldModel.clearable */
  setClearable(clearableStyle: ValueFieldClearable) {
    this.setProperty('clearable', clearableStyle);
  }

  protected _renderClearable() {
    if (this.isClearable()) {
      if (!this.$clearIcon) {
        this.addClearIcon();
      }
    } else {
      if (this.$clearIcon) {
        this.$clearIcon.remove();
        this.$clearIcon = null;
      }
    }
    this.invalidateLayoutTree(false);
    this._updateClearableStyles();
  }

  protected _updateClearableStyles() {
    this.$container.removeClass('clearable-always clearable-focused');
    if (this.isClearable()) {
      if (this.clearable === ValueField.Clearable.ALWAYS) {
        this.$container.addClass('clearable-always');
      } else if (this.clearable === ValueField.Clearable.FOCUSED) {
        this.$container.addClass('clearable-focused');
      }
    }
  }

  isClearable(): boolean {
    return this.clearable === ValueField.Clearable.ALWAYS || this.clearable === ValueField.Clearable.FOCUSED;
  }

  /**
   * Clears the display text and the value to null.
   */
  clear() {
    this._clear();
    this._updateHasText();
    this.acceptInput();
    this._triggerClear();
  }

  protected _clear() {
    // to be implemented by subclasses
  }

  protected _triggerClear() {
    this.trigger('clear');
  }

  /**
   * Sets a new value if it is valid.
   *
   * The value is first converted to the expected type using {@link _ensureValue}, if supported by the field.
   * It is then validated using {@link validateValue}.
   * If the value is valid, it will be formatted using {@link formatValue} which will set the result as {@link displayText}.
   * If it is not valid, a {@link ValidationFailedStatus} is added using {@link addErrorStatus}.
   * Finally, a `propertyChange:value` event will be emitted.
   *
   * @returns `void` or a promise if an asynchronous validator has been called (see {@link ValueFieldModel.validators})
   * @see ValueFieldModel.value
   **/
  setValue(value: TValue | TModelValue): JQuery.Promise<void> | void {
    // Same code as in Widget#setProperty expect for the equals check
    // -> _setValue has to be called even if the value is equal so that update display text will be executed
    value = this._prepareProperty('value', value);
    if (this.rendered) {
      this._callRemoveProperty('value');
    }
    let result = this._setValue(value);
    return promises.thenOrNow(result, () => {
      if (this.rendered) {
        this._callRenderProperty('value');
      }
    });
  }

  /**
   * Resets the value to its initial value.
   */
  resetValue() {
    this.setValue(this.initialValue);
  }

  /**
   * Default does nothing because the value field does not know which type the concrete field uses.
   * May be overridden to cast the value to the required type.
   * @returns the value with the correct type.
   */
  protected _ensureValue(value: TValue | TModelValue): TValue {
    return value as TValue;
  }

  /**
   * @returns `void` or a promise if an asynchronous validator has been called (see {@link ValueFieldModel.validators})
   */
  protected _setValue(value: TValue | TModelValue): JQuery.Promise<void> | void {
    let oldValue = this.value;
    let typedValue = null;
    try {
      typedValue = this._ensureValue(value);
    } catch (conversionError) {
      this._ensureValueFailed(value, conversionError);
      return;
    }

    let valueOrPromise: TValue | JQuery.Promise<TValue>;
    try {
      valueOrPromise = this.validateValue(typedValue);
    } catch (error) {
      this._validationFailed(typedValue, error);
      return;
    }

    if (objects.isPromise(valueOrPromise)) {
      this._setValidatePending(true);
      valueOrPromise.then(
        validatedValue => {
          if (this.destroyed) {
            return;
          }
          if (this._validatePendingCounter === 1) {
            this._validationSucceeded(validatedValue, oldValue);
          }
          this._setValidatePending(false);
        },
        // Don't use catch() to not catch errors that happen in _validationSucceeded to make sure it behaves the same as the synchronous implementation
        error => {
          if (this.destroyed) {
            return;
          }
          if (this._validatePendingCounter === 1) {
            this._validationFailed(typedValue, error);
          }
          this._setValidatePending(false);
        }
      ).catch(error => {
        // Error in succeeded or failed
        // Just calling errorHandler.handle() does not create fatal errors if error is a string or a Status
        let errorHandler = App.get().errorHandler;
        errorHandler.analyzeError(error).then(errorInfo => errorHandler.handleErrorInfo({
          ...errorInfo,
          showAsFatalError: true
        }));
      });
      return this.when('propertyChange:validatePending').then(() => undefined);
    }
    this._validationSucceeded(valueOrPromise, oldValue);
  }

  protected _setValidatePending(validatePending: boolean) {
    let counter = Math.max(validatePending ? this._validatePendingCounter + 1 : this._validatePendingCounter - 1, 0);
    this._validatePendingCounter = counter;
    this._setProperty('validatePending', counter > 0);
  }

  /**
   * Validates the value by executing the {@link validators}. If a new value is the result, it will be set.
   *
   * @returns `void` or a promise if an asynchronous validator has been called (see {@link ValueFieldModel.validators})
   */
  validate(): JQuery.Promise<void> | void {
    return this._setValue(this.value);
  }

  /**
   * @param validator the validator to be added.
   *     A validator is a function that accepts a raw value and either returns the validated value or
   *     throws an {@link Error}, a {@link Status} or an error message as `string` if the value is invalid.
   * @param revalidate True, to revalidate the value, false to just add the validator and do nothing else. Default is true.
   * @see ValueFieldModel.validators
   */
  addValidator(validator: ValueFieldValidator<TValue>, revalidate?: boolean) {
    let validators = this.validators.slice();
    validators.push(validator);
    this.setValidators(validators, revalidate);
  }

  /**
   * @param validator the validator to be removed
   * @param revalidate True, to revalidate the value, false to just remove the validator and do nothing else. Default is true.
   * @see ValueFieldModel.validators
   */
  removeValidator(validator: ValueFieldValidator<TValue>, revalidate?: boolean) {
    let validators = this.validators.slice();
    arrays.remove(validators, validator);
    this.setValidators(validators, revalidate);
  }

  /**
   * Replaces all existing validators with the given one.
   * If you want to add multiple validators, use {@link addValidator}.
   *
   * Remember calling the default validator which is passed as parameter to the validate function, if needed.
   *
   * @param validator the new validator which replaces every other. If null, the default validator is used.
   *     A validator is a function that accepts a raw value and either returns the validated value or
   *     throws an {@link Error}, a {@link Status} or an error message as `string` if the value is invalid.
   */
  setValidator(validator: ValueFieldValidator<TValue>, revalidate?: boolean) {
    if (!validator) {
      validator = this._validateValue.bind(this);
    }
    let validators = [];
    if (validator) {
      validators = [validator];
    }
    this.setValidators(validators, revalidate);
  }

  /** @see ValueFieldModel.validators */
  setValidators(validators: ValueFieldValidator<TValue>[], revalidate?: boolean) {
    this.setProperty('validators', validators);
    if (this.initialized && scout.nvl(revalidate, true)) {
      this.validate();
    }
  }

  /**
   * Validates the given value using the {@link ValueFieldModel.validators}.
   *
   * @param value the value to be validated
   * @returns the validated value or a promise if an asynchronous validator has been called (see {@link ValueFieldModel.validators})
   * @throws a message, a {@link Status} or an {@link Error} if the validation fails
   */
  validateValue(value: TValue): TValue | JQuery.Promise<TValue> {
    let defaultValidator = this._validateValue.bind(this);

    // Ensure value is never undefined (necessary for updateSaveNeeded and should make it easier in general)
    let ensureNotUndefined = v => scout.nvl(v, null);
    let validators = [...this.validators, ensureNotUndefined];

    return this._validateValueImpl(value, validators, defaultValidator);
  }

  protected _validateValueImpl(value: TValue, validators: ValueFieldValidator<TValue>[], defaultValidator: ValueFieldValidator<TValue>): TValue | JQuery.Promise<TValue> {
    for (let i = 0; i < validators.length; i++) {
      const validator = validators[i];
      const valueOrPromise = validator(value, defaultValidator);
      if (objects.isPromise(valueOrPromise)) {
        return valueOrPromise.then(result => this._validateValueImpl(result, validators.slice(i + 1), defaultValidator));
      }
      value = valueOrPromise as TValue;
    }
    return value;
  }

  /**
   * @returns the validated value or a promise if the validation happens asynchronously
   * @throws a message, a {@link Status} or an {@link Error} if the validation fails
   */
  protected _validateValue(value: TValue): TValue | JQuery.Promise<TValue> {
    if (typeof value === 'string' && value === '') {
      // Convert empty string to null.
      // Not using strings.nullIfEmpty is by purpose because it also removes white space characters which may not be desired here
      value = null;
    }
    return value;
  }

  protected _validationSucceeded(newValue: TValue, oldValue: TValue) {
    // When widget is initialized with a given errorStatus and a value -> don't remove the error
    // status. This is a typical case for Scout Classic: field has a ParsingFailedError and user
    // hits reload.
    if (this.initialized) {
      this.removeErrorStatus(ParsingFailedStatus);
      this.removeErrorStatus(ValidationFailedStatus);
    }
    this.value = newValue;
    this._updateDisplayText();
    if (this._valueEquals(oldValue, this.value)) {
      return;
    }

    this._valueChanged();
    this._updateMenus();
    this._updateEmpty();
    this.updateSaveNeeded();
    this.triggerPropertyChange('value', oldValue, this.value);
  }

  protected _valueEquals(valueA: TValue, valueB: TValue): boolean {
    if (Array.isArray(valueA) && Array.isArray(valueB)) {
      return arrays.equals(valueA, valueB);
    }
    return objects.equals(valueA, valueB);
  }

  /**
   * Is called after a value is changed. May be implemented by subclasses. The default does nothing.
   */
  protected _valueChanged() {
    // NOP
  }

  protected _validationFailed(value: TValue, error: any) {
    $.log.isInfoEnabled() && $.log.info('Validation failed for field with id ' + this.id, error);
    this.removeErrorStatus(ParsingFailedStatus);
    this.removeErrorStatus(ValidationFailedStatus);
    let status = this._createValidationFailedStatus(value, error);
    this.addErrorStatus(status);
    this._updateDisplayText(value);
  }

  protected _ensureValueFailed(value: TModelValue, error: any) {
    $.log.isInfoEnabled() && $.log.info('EnsureValue failed for field with id ' + this.id, error);
    this.removeErrorStatus(ParsingFailedStatus);
    this.removeErrorStatus(ValidationFailedStatus);
    let status = this._createValidationFailedStatus(value, error);
    this.addErrorStatus(status);
    this.setDisplayText(this._formatRawValue(value));
  }

  protected _formatRawValue(value: TModelValue): string {
    return value + '';
  }

  protected _createValidationFailedStatus(value: TValue | TModelValue, error: any): Status {
    return this._createInvalidValueStatus('ValidationFailedStatus', value, error);
  }

  protected _createInvalidValueStatus(statusType: StatusType, value: any, error: any): Status {
    let statusFunc = Status.classForName(statusType);
    // type of status is correct
    if (error instanceof statusFunc) {
      return error;
    }
    let message, severity: StatusSeverity = Status.Severity.ERROR;
    if (error instanceof Status) {
      // it's a Status, but it has the wrong specific type
      message = error.message;
      severity = error.severity;
    } else if (typeof error === 'string') {
      // convert string to status
      message = error;
    } else {
      // create status with default message
      message = this.session.text(this.invalidValueMessageKey, value);
    }
    return scout.create(statusType, {
      message: message,
      severity: severity
    });
  }

  protected _updateDisplayText(value?: TValue) {
    if (!this.initialized && !objects.isNullOrUndefined(this.displayText)) {
      // If a displayText is provided initially, use that text instead of using formatValue to generate a text based on the value
      return;
    }
    value = scout.nvl(value, this.value);
    let returned = this.formatValue(value);
    if (objects.isPromise(returned)) {
      this._updateDisplayTextPending = true;
      // Promise is returned -> set display text later
      returned
        .done(text => this.setDisplayText(text))
        .fail(() => {
          // If display text was updated in the meantime, don't override the text with an empty string
          if (this._updateDisplayTextPending) {
            this.setDisplayText('');
          }
          $.log.isInfoEnabled() && $.log.info('Could not resolve display text for value: ' + value);
        })
        .always(() => {
          this._updateDisplayTextPending = false;
        });
    } else {
      this.setDisplayText(returned);
      this._updateDisplayTextPending = false;
    }
  }

  /**
   * Replaces the existing {@link ValueFieldModel.formatter}.
   *
   * @param formatter the new formatter. If null, the default formatter is used.
   */
  setFormatter(formatter: ValueFieldFormatter<TValue>) {
    if (!formatter) {
      formatter = this._formatValue.bind(this);
    }
    this.setProperty('formatter', formatter);
    if (this.initialized) {
      this.validate();
    }
  }

  /**
   * Uses the {@link ValueFieldModel.formatter} to format the value.
   *
   * @returns the formatted value as display text or a promise if the formatting happens asynchronously.
   */
  formatValue(value: TValue): string | JQuery.Promise<string> {
    let defaultFormatter = this._formatValue.bind(this);
    return this.formatter(value, defaultFormatter);
  }

  /**
   * @returns the formatted value as display text or a promise if the formatting happens asynchronously.
   */
  protected _formatValue(value: TValue): string | JQuery.Promise<string> {
    return scout.nvl(value, '') + '';
  }

  override computeSaveNeeded(): boolean {
    if (this._hasValueChanged()) {
      return true;
    }
    return super.computeSaveNeeded();
  }

  protected _hasValueChanged(): boolean {
    return !this._valueEquals(this.value, this.initialValue);
  }

  addClearIcon($parent?: JQuery) {
    if (!$parent) {
      $parent = this.$container;
    }
    this.$clearIcon = $parent.appendSpan('clear-icon unfocusable text-field-icon action')
      .on('mousedown', this._onClearIconMouseDown.bind(this));
    aria.role(this.$clearIcon, 'button');
    aria.label(this.$clearIcon, this.session.text('ui.ClearField'));
  }

  override addContainer($parent: JQuery, cssClass?: string, layout?: AbstractLayout) {
    super.addContainer($parent, cssClass, layout);
    this.$container.addClass('value-field');
  }

  override addField($field: JQuery) {
    super.addField($field);
    this.$field.data('valuefield', this);
  }

  protected override _markAsSaved() {
    super._markAsSaved();
    this.initialValue = this.value;
  }

  /**
   * @returns true if the value is null or undefined. Also returns true if the value is an array and the array is empty.
   */
  protected override _computeEmpty(): boolean {
    return this.value === null || this.value === undefined || (Array.isArray(this.value) && arrays.empty(this.value));
  }

  protected override _getCurrentMenuTypes(): string[] {
    if (objects.isNullOrUndefined(this.value)) {
      return [...super._getCurrentMenuTypes(), ValueField.MenuType.Null];
    }
    return [...super._getCurrentMenuTypes(), ValueField.MenuType.NotNull];
  }

  // ==== static helper methods ==== //

  /**
   * Invokes 'ValueField.aboutToBlurByMouseDown' on the currently active value field.
   * This method has no effect if another element is the focus owner.
   */
  static invokeValueFieldAboutToBlurByMouseDown(target: Element) {
    let activeValueField = this._getActiveValueField(target);
    if (activeValueField) {
      activeValueField.aboutToBlurByMouseDown(target);
    }
  }

  /**
   * Invokes 'ValueField.acceptInput' on the currently active value field.
   * This method has no effect if another element is the focus owner.
   */
  static invokeValueFieldAcceptInput(target: Element) {
    let activeValueField = this._getActiveValueField(target);
    if (activeValueField) {
      activeValueField.acceptInput();
    }
  }

  /**
   * Returns the currently active value field, or null if another element is active.
   * Also, if no value field currently owns the focus, its parent is checked to be a value field and is returned accordingly.
   * That is used in DateField.js with multiple input elements.
   */
  protected static _getActiveValueField(target: Element): ValueField<any> {
    let $activeElement = $(target).activeElement(),
      activeWidget = scout.widget($activeElement);
    if (activeWidget instanceof ValueField) {
      return activeWidget.enabledComputed ? activeWidget : null;
    }
    const parent = activeWidget && activeWidget.findParent(parent => parent instanceof ValueField) as ValueField<any>;
    return (parent && parent.enabledComputed) ? parent : null;
  }
}

export type ValueFieldClearable = EnumObject<typeof ValueField.Clearable>;
export type ValueFieldMenuType = EnumObject<typeof ValueField.MenuType>;
export type ValueFieldValidator<TValue> = (value: TValue, defaultValidator?: ValueFieldValidator<TValue>) => TValue | JQuery.Promise<TValue>;
export type ValueFieldFormatter<TValue> = (value: TValue, defaultFormatter?: ValueFieldFormatter<TValue>) => string | JQuery.Promise<string>;
export type ValueFieldParser<TValue> = (displayText: string, defaultParser?: ValueFieldParser<TValue>) => TValue;
