/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DeviceType, InitModelOf, scout, SomeRequired, UserAgentModel} from '../index';

export class UserAgent implements UserAgentModel {
  declare model: UserAgentModel;
  declare initModel: SomeRequired<this['model'], 'deviceType'>;

  deviceType: DeviceType;
  touch: boolean;
  standalone: boolean;

  constructor(model: InitModelOf<UserAgent>) {
    model = model || {} as InitModelOf<UserAgent>;
    if (!model.deviceType) {
      throw new Error('deviceType needs to be defined');
    }
    this.deviceType = model.deviceType;
    this.touch = scout.nvl(model.touch, false);
    this.standalone = scout.nvl(model.standalone, false);
  }
}
