/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * JUnit test for {@link UserAgent}
 * 
 * @since 3.8.0
 */
public class UserAgentTest {

  @Test
  public void testEqualsAndHashCode() {
    UserAgent userAgentRap = UserAgent.create(UiLayer.RAP, UiDeviceType.DESKTOP);
    UserAgent userAgentRap2 = UserAgent.create(UiLayer.RAP, UiDeviceType.DESKTOP);
    UserAgent userAgentRapMobile = UserAgent.create(UiLayer.RAP, UiDeviceType.MOBILE);

    assertEquals(userAgentRap, userAgentRap2);
    assertEquals(userAgentRap.hashCode(), userAgentRap2.hashCode());

    assertFalse(userAgentRap.equals(userAgentRapMobile));
    assertFalse(userAgentRap.hashCode() == userAgentRapMobile.hashCode());

    String chromeUserAgentStr = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19";
    String firefoxUserAgentStr = "Mozilla/5.0 .. Firefox ..";
    UserAgent userAgentRapChrome = UserAgent.create(UiLayer.RAP, UiDeviceType.DESKTOP, chromeUserAgentStr);
    UserAgent userAgentRapChrome2 = UserAgent.create(UiLayer.RAP, UiDeviceType.DESKTOP, chromeUserAgentStr);
    UserAgent userAgentRapFirefox = UserAgent.create(UiLayer.RAP, UiDeviceType.DESKTOP, firefoxUserAgentStr);

    assertEquals(userAgentRapChrome, userAgentRapChrome2);
    assertEquals(userAgentRapChrome.hashCode(), userAgentRapChrome2.hashCode());

    assertFalse(userAgentRap.equals(userAgentRapChrome));
    assertFalse(userAgentRap.hashCode() == userAgentRapChrome.hashCode());

    assertFalse(userAgentRapChrome.equals(userAgentRapFirefox));
    assertFalse(userAgentRapChrome.hashCode() == userAgentRapFirefox.hashCode());
  }

}
