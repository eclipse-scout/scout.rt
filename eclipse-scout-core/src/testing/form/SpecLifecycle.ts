/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormLifecycle, Status, ValidationResult} from '../../index';

export class SpecLifecycle extends FormLifecycle {
  override _validate(): JQuery.Promise<Status> {
    return super._validate();
  }

  override _createInvalidElementsMessageHtml(missing: ValidationResult[], invalid: ValidationResult[]): string {
    return super._createInvalidElementsMessageHtml(missing, invalid);
  }
}
