/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormFieldModel, ValueFieldClearable, ValueFieldFormatter, ValueFieldParser, ValueFieldValidator} from '../../index';

export interface ValueFieldModel<TValue extends TModelValue, TModelValue = TValue> extends FormFieldModel {
  /**
   * Configures the validators to be used when the value of the field needs to be validated (e.g. when the value changes).
   *
   * When the value is validated, every validator is called and has to agree.
   * If one validation fails, the value is not accepted.
   *
   * By default, the list contains the default validator which is {@link ValueField._validateValue}.
   */
  validators?: ValueFieldValidator<TValue>[];
  /**
   * Allows to have a single active validator, replaces all existing validators including the default one.
   *
   * @see ValueField.setValidator
   */
  validator?: ValueFieldValidator<TValue>;
  /**
   * The formatter is responsible to format the {@link value} so it can be used as {@link displayText}.
   *
   * Default is {@link _formatValue}.
   *
   * @see ValueField.formatValue
   */
  formatter?: ValueFieldFormatter<TValue>;
  /**
   * The parser is responsible to parse the {@link displayText} and convert it into a {@link value}.
   *
   * Default is {@link _parseValue}.
   *
   * @see ValueField.parseValue
   */
  parser?: ValueFieldParser<TValue>;
  /**
   * Defines when the clear icon should be visible that allows the user to clear the value.
   *
   * Default is {@link ValueField.Clearable.FOCUSED}
   */
  clearable?: ValueFieldClearable;
  /**
   * The initial value is used to determine whether the field was {@link touched} and is used to replace the value when {@link ValueField.resetValue} is called.
   *
   * If the value field is used in a context of a {@link Form} with a {@link FormLifecycle}, the initial value will be set automatically by {@link ValueField.markAsSaved}
   * before the form is loaded (see {@link Form.load}).
   *
   * So in most cases you don't need to explicitly set the initial value because it would have no effect anyway.
   * Just use {@link value} if you would like to initialize the field with a value.
   */
  initialValue?: TValue;
  /**
   * The main asset of the {@link ValueField}.
   */
  value?: TModelValue;
  /**
   * The textual representation of the {@link value}.
   *
   * The display text is computed automatically based on the {@link value} using the {@link formatter}.
   */
  displayText?: string;
  /**
   * Configures the key of the message to be shown when the {@link value} is invalid.
   *
   * Default is `InvalidValueMessageX`.
   */
  invalidValueMessageKey?: string;
}
