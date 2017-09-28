/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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
    UserAgent userAgentHtml = UserAgents.create().withUiLayer(UiLayer.HTML).withUiDeviceType(UiDeviceType.DESKTOP).build();
    UserAgent userAgentHtml2 = UserAgents.create().withUiLayer(UiLayer.HTML).withUiDeviceType(UiDeviceType.DESKTOP).build();
    UserAgent userAgentHtmlMobile = UserAgents.create().withUiLayer(UiLayer.HTML).withUiDeviceType(UiDeviceType.MOBILE).build();

    assertEquals(userAgentHtml, userAgentHtml2);
    assertEquals(userAgentHtml.hashCode(), userAgentHtml2.hashCode());

    assertFalse(userAgentHtml.equals(userAgentHtmlMobile));
    assertFalse(userAgentHtml.hashCode() == userAgentHtmlMobile.hashCode());

    String chromeUserAgentStr = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19";
    String firefoxUserAgentStr = "Mozilla/5.0 .. Firefox ..";
    UserAgent userAgentHtmlChrome = UserAgents.create().withUiLayer(UiLayer.HTML).withUiDeviceType(UiDeviceType.DESKTOP).withDeviceId(chromeUserAgentStr).build();
    UserAgent userAgentHtmlChrome2 = UserAgents.create().withUiLayer(UiLayer.HTML).withUiDeviceType(UiDeviceType.DESKTOP).withDeviceId(chromeUserAgentStr).build();
    UserAgent userAgentHtmlFirefox = UserAgents.create().withUiLayer(UiLayer.HTML).withUiDeviceType(UiDeviceType.DESKTOP).withDeviceId(firefoxUserAgentStr).build();

    assertEquals(userAgentHtmlChrome, userAgentHtmlChrome2);
    assertEquals(userAgentHtmlChrome.hashCode(), userAgentHtmlChrome2.hashCode());

    assertFalse(userAgentHtml.equals(userAgentHtmlChrome));
    assertFalse(userAgentHtml.hashCode() == userAgentHtmlChrome.hashCode());

    assertFalse(userAgentHtmlChrome.equals(userAgentHtmlFirefox));
    assertFalse(userAgentHtmlChrome.hashCode() == userAgentHtmlFirefox.hashCode());
  }

}
