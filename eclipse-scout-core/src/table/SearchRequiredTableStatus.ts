/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, Status} from '../index';

export class SearchRequiredTableStatus extends Status {

  constructor(model?: InitModelOf<SearchRequiredTableStatus>) {
    super(model);
  }

  static override info(message: string): SearchRequiredTableStatus {
    return new SearchRequiredTableStatus(Status.ensureModel(message, Status.Severity.INFO));
  }
}
