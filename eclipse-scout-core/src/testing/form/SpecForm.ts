/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, Status} from '../../index';
import {SpecLifecycle} from '../index';

export class SpecForm extends Form {
  declare lifecycle: SpecLifecycle;

  override _load(): JQuery.Promise<object> {
    return super._load();
  }

  override _save(data: object): JQuery.Promise<Status> {
    return super._save(data);
  }
}
