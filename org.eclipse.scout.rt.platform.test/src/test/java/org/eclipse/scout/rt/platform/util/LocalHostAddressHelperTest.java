/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public class LocalHostAddressHelperTest {

  @Test
  public void testGetLocalHostName() {
    LocalHostAddressHelper helper = BEANS.get(LocalHostAddressHelper.class);

    assertEquals(LocalHostAddressHelper.UNKNOWN, helper.parseUnknownHostException(null));
    assertEquals(LocalHostAddressHelper.UNKNOWN, helper.parseUnknownHostException(new UnknownHostException()));
    assertEquals(LocalHostAddressHelper.UNKNOWN, helper.parseUnknownHostException(new UnknownHostException(null)));
    assertEquals(LocalHostAddressHelper.UNKNOWN, helper.parseUnknownHostException(new UnknownHostException("foo")));
    assertEquals(LocalHostAddressHelper.UNKNOWN, helper.parseUnknownHostException(new UnknownHostException("vd1234.example.local Name or service not known")));

    assertEquals("vd1234.example.local", helper.parseUnknownHostException(new UnknownHostException("vd1234.example.local: Name or service not known")));
    assertEquals("vd1234.example.local", helper.parseUnknownHostException(new UnknownHostException("vd1234.example.local: vd1234.example.local: Name or service not known")));
  }
}
