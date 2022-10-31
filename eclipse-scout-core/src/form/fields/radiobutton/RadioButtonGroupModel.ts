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
import {FormField, FormFieldModel, LogicalGridLayoutConfig, LookupCall, ValueFieldModel} from '../../../index';
import {RefModel} from '../../../types';
import {LogicalGridLayoutConfigModel} from '../../../layout/logicalgrid/LogicalGridLayoutConfig';

export default interface RadioButtonGroupModel<TValue> extends ValueFieldModel<TValue> {
  layoutConfig?: LogicalGridLayoutConfig | LogicalGridLayoutConfigModel;
  fields?: (FormField | RefModel<FormFieldModel>)[];
  gridColumnCount?: number;
  lookupCall?: LookupCall<TValue>;
}
