/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
