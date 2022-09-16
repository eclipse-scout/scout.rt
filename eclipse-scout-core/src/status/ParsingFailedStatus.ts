/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Status} from '../index';

export default class ParsingFailedStatus extends Status {

  constructor(model) {
    super(model);
  }

  /**
   * @returns {Status} a ParsingFailedStatus object with severity ERROR.
   */
  static error(model) {
    return new ParsingFailedStatus(Status.ensureModel(model, Status.Severity.ERROR));
  }

}
