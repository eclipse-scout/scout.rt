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

/**
 * The DefaultStatus class is used add programmatic Status triggered by business logic
 * in cases where you don't want or don't have to implement your own Status sub-class.
 */
export default class DefaultStatus extends Status {

  constructor(model) {
    super(model);
  }

  /**
   * @returns {DefaultStatus} a DefaultStatus object with severity ERROR.
   */
  static error(model) {
    return new DefaultStatus(Status.ensureModel(model, Status.Severity.ERROR));
  }

  /**
   * @returns {DefaultStatus} a DefaultStatus object with severity WARNING.
   */
  static warning(model) {
    return new DefaultStatus(Status.ensureModel(model, Status.Severity.WARNING));
  }

  /**
   * @returns {DefaultStatus} a DefaultStatus object with severity INFO.
   */
  static info(model) {
    return new DefaultStatus(Status.ensureModel(model, Status.Severity.INFO));
  }

}
