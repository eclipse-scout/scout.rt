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
import {InitModelOf, Status, StatusModel} from '../index';

/**
 * The DefaultStatus class is used add programmatic Status triggered by business logic
 * in cases where you don't want or don't have to implement your own Status subclass.
 */
export class DefaultStatus extends Status {

  constructor(model?: InitModelOf<DefaultStatus>) {
    super(model);
  }

  /**
   * @returns a {@link DefaultStatus} object with severity ERROR.
   */
  static override error(model: StatusModel | string): DefaultStatus {
    return new DefaultStatus(Status.ensureModel(model, Status.Severity.ERROR));
  }

  /**
   * @returns a {@link DefaultStatus} object with severity WARNING.
   */
  static override warning(model: StatusModel | string): DefaultStatus {
    return new DefaultStatus(Status.ensureModel(model, Status.Severity.WARNING));
  }

  /**
   * @returns a {@link DefaultStatus} object with severity INFO.
   */
  static override info(model: StatusModel | string): DefaultStatus {
    return new DefaultStatus(Status.ensureModel(model, Status.Severity.INFO));
  }
}
