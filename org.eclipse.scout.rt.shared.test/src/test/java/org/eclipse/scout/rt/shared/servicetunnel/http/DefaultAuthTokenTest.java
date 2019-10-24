/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.security.KeyPairBytes;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenPrivateKeyProperty;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenPublicKeyProperty;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class DefaultAuthTokenTest {
  private static KeyPairBytes s_pair;
  private static List<IBean<?>> s_beans = new ArrayList<>();

  @Replace
  private static class AuthTokenPrivateKeyPropertyEx extends AuthTokenPrivateKeyProperty {
    @Override
    public byte[] getValue(String namespace) {
      return s_pair.getPrivateKey();
    }
  }

  @Replace
  private static class AuthTokenPublicKeyPropertyEx extends AuthTokenPublicKeyProperty {
    @Override
    public byte[] getValue(String namespace) {
      return s_pair.getPublicKey();
    }
  }

  @BeforeClass
  public static void beforeClass() {
    s_pair = SecurityUtility.generateKeyPair();
    s_beans.addAll(BeanTestingHelper.get().registerBeans(
        new BeanMetaData(AuthTokenPrivateKeyProperty.class).withApplicationScoped(true).withInitialInstance(new AuthTokenPrivateKeyPropertyEx()),
        new BeanMetaData(AuthTokenPublicKeyProperty.class).withApplicationScoped(true).withInitialInstance(new AuthTokenPublicKeyPropertyEx())));
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBeans(s_beans);
  }

  @Test
  public void testEnabled() throws Exception {
    assertTrue(BEANS.get(DefaultAuthTokenSigner.class).isEnabled());
    assertTrue(BEANS.get(DefaultAuthTokenVerifier.class).isEnabled());
  }

  @Test
  public void testSimple() {
    DefaultAuthTokenVerifier verifier = BEANS.get(DefaultAuthTokenVerifier.class);

    DefaultAuthToken t = BEANS.get(DefaultAuthTokenSigner.class).sign(BEANS.get(DefaultAuthToken.class).withUserId("foo"));
    Assert.assertEquals("foo", t.getUserId());
    Assert.assertTrue(CollectionUtility.isEmpty(t.getCustomArgs()));
    Assert.assertTrue(t.getValidUntil() - System.currentTimeMillis() > 0);
    Assert.assertNotNull(t.getSignature());
    Assert.assertTrue(verifier.verify(t));

    String encoded = t.toString();

    DefaultAuthToken t2 = BEANS.get(DefaultAuthToken.class).read(encoded);
    Assert.assertEquals(t.getUserId(), t2.getUserId());
    Assert.assertEquals(t.getValidUntil(), t2.getValidUntil());
    Assert.assertTrue(verifier.verify(t2));

    String encodedAndTampered = encoded.substring(0, encoded.length() - 3);
    DefaultAuthToken t3 = BEANS.get(DefaultAuthToken.class).read(encodedAndTampered);
    Assert.assertEquals(t.getUserId(), t3.getUserId());
    Assert.assertEquals(t.getValidUntil(), t3.getValidUntil());
    Assert.assertFalse(verifier.verify(t3));
  }

  @Test
  public void testWithCustomToken() {
    DefaultAuthTokenVerifier verifier = BEANS.get(DefaultAuthTokenVerifier.class);

    DefaultAuthToken t = BEANS.get(DefaultAuthTokenSigner.class).sign(BEANS.get(DefaultAuthToken.class).withUserId("foo").withCustomArgs("bar"));
    Assert.assertEquals("foo", t.getUserId());
    Assert.assertFalse(CollectionUtility.isEmpty(t.getCustomArgs()));
    Assert.assertEquals(1, t.getCustomArgs().size());
    Assert.assertEquals("bar", t.getCustomArgs().get(0));
    Assert.assertTrue(verifier.verify(t));

    String encoded = t.toString();

    DefaultAuthToken t2 = BEANS.get(DefaultAuthToken.class).read(encoded);
    Assert.assertEquals(t.getUserId(), t2.getUserId());
    Assert.assertEquals(t.getValidUntil(), t2.getValidUntil());
    Assert.assertEquals(t.getCustomArgs().get(0), t2.getCustomArgs().get(0));
    Assert.assertTrue(verifier.verify(t2));
  }
}
