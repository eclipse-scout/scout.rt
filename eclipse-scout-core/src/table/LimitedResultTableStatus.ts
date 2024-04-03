/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, Status} from '../index';

export class LimitedResultTableStatus extends Status {
  constructor(model?: InitModelOf<LimitedResultTableStatus>) {
    super(model);
  }

  static override info(message: string): LimitedResultTableStatus {
    return new LimitedResultTableStatus(Status.ensureModel(message, Status.Severity.INFO));
  }
}
