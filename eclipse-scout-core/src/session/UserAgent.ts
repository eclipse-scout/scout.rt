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
import {scout} from '../index';
import {DeviceType} from '../util/Device';
import UserAgentModel from './UserAgentModel';
import {SomeRequired} from '../types';
import {InitModelOf} from '../scout';

export default class UserAgent implements UserAgentModel {
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
