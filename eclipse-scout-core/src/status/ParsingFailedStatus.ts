/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, Status, StatusModel} from '../index';

export class ParsingFailedStatus extends Status {

  constructor(model?: InitModelOf<ParsingFailedStatus>) {
    super(model);
  }

  /**
   * @returns a {@link ParsingFailedStatus} object with severity ERROR.
   */
  static override error(model: StatusModel | string): ParsingFailedStatus {
    return new ParsingFailedStatus(Status.ensureModel(model, Status.Severity.ERROR));
  }
}
