/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import static org.junit.Assert.assertTrue;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.security.JwtPrincipal;
import org.eclipse.scout.rt.platform.security.KeyPairBytes;
import org.eclipse.scout.rt.platform.security.SamlPrincipal;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
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
  public void testEnabled() {
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
  public void testWithCustomArgs() {
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

  @Test
  public void testWithCustomArgsOfSimplePrincipal() {
    DefaultAuthToken t1 = BEANS.get(DefaultAuthTokenSigner.class).sign(BEANS.get(DefaultAuthToken.class).withUserId("foo").withCustomArgs("bar"));
    DefaultAuthToken t2 = BEANS.get(DefaultAuthToken.class).read(t1.toString());
    Assert.assertTrue(BEANS.get(DefaultAuthTokenVerifier.class).verify(t2));
    Principal principal = BEANS.get(DefaultAuthTokenPrincipalProducer.class).produce(t2.getUserId(), t2.getCustomArgs());
    Assert.assertTrue(principal instanceof SimplePrincipal);
    SimplePrincipal s = (SimplePrincipal) principal;
    Assert.assertEquals("foo", s.getName());
  }

  @Test
  public void testTokenEncoding() {
    String userId = IntStream.range(0, 2048).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());

    DefaultAuthToken t1 = BEANS.get(DefaultAuthTokenSigner.class).sign(BEANS.get(DefaultAuthToken.class).withUserId(userId));
    String header = t1.toString();
    System.out.println("Token: " + header);
    DefaultAuthToken t2 = BEANS.get(DefaultAuthToken.class).read(header);
    Assert.assertTrue(BEANS.get(DefaultAuthTokenVerifier.class).verify(t2));
    Assert.assertEquals(userId, t2.getUserId());
  }

  @Test
  public void testWithCustomArgsOfJwtPrincipal() {
    String idToken = IntStream.range(0, 1227).mapToObj(i -> "a").collect(Collectors.joining());
    String accessToken = IntStream.range(0, 2100).mapToObj(i -> "b").collect(Collectors.joining());
    String refreshToken = IntStream.range(0, 1094).mapToObj(i -> "c").collect(Collectors.joining());

    DefaultAuthToken t1 = BEANS.get(DefaultAuthTokenSigner.class).sign(BEANS.get(DefaultAuthToken.class).withUserId("foo").withCustomArgs("jwt", idToken, accessToken, refreshToken));
    String header = t1.toString();
    System.out.println("Token: " + header);
    DefaultAuthToken t2 = BEANS.get(DefaultAuthToken.class).read(header);
    Assert.assertTrue(BEANS.get(DefaultAuthTokenVerifier.class).verify(t2));
    Principal principal = BEANS.get(DefaultAuthTokenPrincipalProducer.class).produce(t2.getUserId(), t2.getCustomArgs());
    Assert.assertTrue(principal instanceof JwtPrincipal);
    JwtPrincipal jwt = (JwtPrincipal) principal;
    Assert.assertEquals("foo", jwt.getName());
    Assert.assertEquals(idToken, jwt.getJwtTokenString());
    Assert.assertEquals(accessToken, jwt.getAccessToken());
    Assert.assertEquals(refreshToken, jwt.getRefreshToken());
    //check that token export is not larger than max http header 8192
    Assert.assertTrue(header.length() < 8192);
  }

  @Test
  public void testWithCustomArgsOfSamlPrincipal() {
    DefaultAuthToken t1 = BEANS.get(DefaultAuthTokenSigner.class).sign(BEANS.get(DefaultAuthToken.class).withUserId("foo").withCustomArgs("saml", "bar"));
    DefaultAuthToken t2 = BEANS.get(DefaultAuthToken.class).read(t1.toString());
    Assert.assertTrue(BEANS.get(DefaultAuthTokenVerifier.class).verify(t2));
    Principal principal = BEANS.get(DefaultAuthTokenPrincipalProducer.class).produce(t2.getUserId(), t2.getCustomArgs());
    Assert.assertTrue(principal instanceof SamlPrincipal);
    SamlPrincipal saml = (SamlPrincipal) principal;
    Assert.assertEquals("foo", saml.getName());
    Assert.assertEquals("bar", saml.getSessionIndex());
  }
}
