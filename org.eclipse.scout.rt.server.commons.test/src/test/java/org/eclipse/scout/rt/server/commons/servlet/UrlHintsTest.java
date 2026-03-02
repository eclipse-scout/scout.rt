/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public class UrlHintsTest {
  /**
   * Tests that the {@link UrlHints#setFromCookieString(String)} can also read old cookie strings with the 'Z' (=compress) attribute which has been removed in 26.2
   */
  @Test
  public void testSetFromCookieString() {
    // old format with 'Z' (=compress) attribute
    assertEquals("cache=true, minify=true, inspector=true", BEANS.get(UrlHints.class).setFromCookieString("C1Z1M1I1").toHumanReadableString());
    assertEquals("cache=false, minify=false, inspector=false", BEANS.get(UrlHints.class).setFromCookieString("C0Z0M0I0").toHumanReadableString());

    // new format with removed 'Z'=compress attribute
    assertEquals("cache=true, minify=true, inspector=true", BEANS.get(UrlHints.class).setFromCookieString("C1M1I1").toHumanReadableString());
    assertEquals("cache=false, minify=false, inspector=false", BEANS.get(UrlHints.class).setFromCookieString("C0M0I0").toHumanReadableString());
  }
}
