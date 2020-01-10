/*******************************************************************************
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("keys", function() {

  it("forBrowser", function() {
    var device = scout.device;
    device.browser = scout.Device.Browser.FIREFOX;
    expect(scout.keys.forBrowser(scout.keys.ANGULAR_BRACKET)).toEqual(60);
    device.browser = scout.Device.Browser.CHROME;
    expect(scout.keys.forBrowser(scout.keys.ANGULAR_BRACKET)).toEqual(226);
  });

  it("fromBrowser", function() {
    var device = scout.device;
    device.browser = scout.Device.Browser.FIREFOX;
    expect(scout.keys.fromBrowser(60)).toEqual(scout.keys.ANGULAR_BRACKET);
    device.browser = scout.Device.Browser.CHROME;
    expect(scout.keys.fromBrowser(226)).toEqual(scout.keys.ANGULAR_BRACKET);
  });

});
