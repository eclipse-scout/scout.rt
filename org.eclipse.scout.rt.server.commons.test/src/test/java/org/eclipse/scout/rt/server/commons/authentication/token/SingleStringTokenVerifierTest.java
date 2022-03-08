/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.authentication.token;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class SingleStringTokenVerifierTest {

  @Test
  public void testVerify() {
    SingleStringTokenVerifier verifier = BEANS.get(SingleStringTokenVerifier.class).init(token -> ObjectUtility.equals("Correct".toCharArray(), token));

    assertEquals(ITokenVerifier.AUTH_OK, verifier.verify(List.of("Correct".getBytes())));
    assertEquals(ITokenVerifier.AUTH_FORBIDDEN, verifier.verify(List.of("Wrong".getBytes())));
    assertEquals(ITokenVerifier.AUTH_FAILED, verifier.verify(null));
    assertEquals(ITokenVerifier.AUTH_FAILED, verifier.verify(List.of("Correct".getBytes(), "Wrong".getBytes())));
  }
}
