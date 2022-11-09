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
import {Device, keys} from '../../src/index';

describe('keys', () => {

  it('forBrowser', () => {
    let device = Device.get();
    device.browser = Device.Browser.FIREFOX;
    expect(keys.forBrowser(keys.ANGULAR_BRACKET)).toEqual(60);
    device.browser = Device.Browser.CHROME;
    expect(keys.forBrowser(keys.ANGULAR_BRACKET)).toEqual(226);
  });

  it('fromBrowser', () => {
    let device = Device.get();
    device.browser = Device.Browser.FIREFOX;
    expect(keys.fromBrowser(60)).toEqual(keys.ANGULAR_BRACKET);
    device.browser = Device.Browser.CHROME;
    expect(keys.fromBrowser(226)).toEqual(keys.ANGULAR_BRACKET);
  });

});
