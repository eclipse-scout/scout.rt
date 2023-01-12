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
