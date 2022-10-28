/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormFieldModel} from '../../index';
import {ValueFieldClearable, ValueFieldFormatter, ValueFieldParser, ValueFieldValidator} from './ValueField';

export default interface ValueFieldModel<TValue extends TModelValue, TModelValue = TValue> extends FormFieldModel {
  validator?: ValueFieldValidator<TValue>;
  validators?: ValueFieldValidator<TValue>[];
  clearable?: ValueFieldClearable;
  formatter?: ValueFieldFormatter<TValue>;
  initialValue?: TValue;
  value?: TModelValue;
  invalidValueMessageKey?: string;
  parser?: ValueFieldParser<TValue>;
}
