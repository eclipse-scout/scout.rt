/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ElementsValidationResult, ElementsValidationStatusModel, InitModelOf, Status} from '../index';

export class ElementsValidationStatus<TValidationResult extends { errorStatus?: Status }> extends Status implements ElementsValidationStatusModel<TValidationResult> {
  declare model: ElementsValidationStatusModel<TValidationResult>;

  elementsValidationResult: ElementsValidationResult<TValidationResult>;

  constructor(model?: InitModelOf<ElementsValidationStatus<TValidationResult>>) {
    super(model);
    this.elementsValidationResult = model.elementsValidationResult;
  }
}
