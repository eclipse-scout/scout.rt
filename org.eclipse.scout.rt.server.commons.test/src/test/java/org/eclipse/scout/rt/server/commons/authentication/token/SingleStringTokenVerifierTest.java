/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

  @Test
  public void testMultipleVerifierInstances() {
    SingleStringTokenVerifier verifier1 = BEANS.get(SingleStringTokenVerifier.class).init(token -> ObjectUtility.equals("Correct1".toCharArray(), token));
    SingleStringTokenVerifier verifier2 = BEANS.get(SingleStringTokenVerifier.class).init(token -> ObjectUtility.equals("Correct2".toCharArray(), token));

    assertEquals(ITokenVerifier.AUTH_OK, verifier1.verify(List.of("Correct1".getBytes())));
    assertEquals(ITokenVerifier.AUTH_FORBIDDEN, verifier1.verify(List.of("Correct2".getBytes())));
    assertEquals(ITokenVerifier.AUTH_FAILED, verifier1.verify(null));
    assertEquals(ITokenVerifier.AUTH_FAILED, verifier1.verify(List.of("Correct1".getBytes(), "Wrong".getBytes())));

    assertEquals(ITokenVerifier.AUTH_OK, verifier2.verify(List.of("Correct2".getBytes())));
    assertEquals(ITokenVerifier.AUTH_FORBIDDEN, verifier2.verify(List.of("Correct1".getBytes())));
    assertEquals(ITokenVerifier.AUTH_FAILED, verifier2.verify(null));
    assertEquals(ITokenVerifier.AUTH_FAILED, verifier2.verify(List.of("Correct2".getBytes(), "Wrong".getBytes())));
  }
}
