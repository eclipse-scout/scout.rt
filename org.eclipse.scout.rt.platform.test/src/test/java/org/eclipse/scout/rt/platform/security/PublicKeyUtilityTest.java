/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.security.PublicKeyUtility;
import org.junit.Test;

/**
 * Unit Test for {@link PublicKeyUtility}
 */
public final class PublicKeyUtilityTest {

  @Test
  @SuppressWarnings("deprecation")
  public void test() throws Exception {
    byte[][] tmp = PublicKeyUtility.createKeyPair(null, 1024);
    assertEquals("array size", 2, tmp.length);

    byte[] publicKey = tmp[0];
    byte[] privateKey = tmp[1];

    byte[] data = "Hello World".getBytes();
    byte[] sig = PublicKeyUtility.sign(data, privateKey, null, null);
    boolean verify = PublicKeyUtility.verify(data, publicKey, sig, null, null);

    assertTrue(verify);
  }
}
