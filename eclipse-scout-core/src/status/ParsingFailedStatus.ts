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
import {Status, StatusModel} from '../index';

export default class ParsingFailedStatus extends Status {

  constructor(model: StatusModel) {
    super(model);
  }

  /**
   * @returns a {@link ParsingFailedStatus} object with severity ERROR.
   */
  static override error(model: StatusModel | string): ParsingFailedStatus {
    return new ParsingFailedStatus(Status.ensureModel(model, Status.Severity.ERROR));
  }
}
