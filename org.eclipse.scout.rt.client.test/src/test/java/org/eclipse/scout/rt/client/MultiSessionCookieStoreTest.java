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
package org.eclipse.scout.rt.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.net.HttpCookie;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.servicetunnel.http.MultiSessionCookieStore;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@link MultiClientSessionCookieStore}
 */
public class MultiSessionCookieStoreTest {

  private static final HttpCookie COOKIE1 = new HttpCookie("Testname1", "Testvalue1");
  private static final HttpCookie COOKIE2 = new HttpCookie("Testname2", "Testvalue2");
  private static final IClientSession SESSION1 = mock(IClientSession.class);
  private static final IClientSession SESSION2 = mock(IClientSession.class);
  private static URI s_testuri1;
  private static URI s_testuri2;

  @BeforeClass
  public static void setup() throws Exception {
    // These URIs must be top-level - and http. Default cookie store of (Oracle) Java
    // just ignores path and changes https -> http when retrieving URIs.
    s_testuri1 = new URI("http://www.eclipse.org");
    s_testuri2 = new URI("http://www.bsiag.com");
  }

  @Before
  public void before() {
    ISession.CURRENT.remove();
  }

  @After
  public void after() {
    ISession.CURRENT.remove();
  }

  @Test
  public void testAddBasic() throws Exception {

    final IClientSession session1 = mock(IClientSession.class);

    MultiSessionCookieStore cookieStore = new MultiSessionCookieStore();
    // Basic storage test
    ISession.CURRENT.set(session1);
    cookieStore.add(s_testuri1, COOKIE1);
    cookieStore.add(s_testuri1, COOKIE2);
    List<HttpCookie> storedCookies = cookieStore.get(s_testuri1);
    assertNotNull(storedCookies);
    assertEquals(2, storedCookies.size());
    assertCookieEquals(COOKIE1, storedCookies.get(0));
    assertCookieEquals(COOKIE2, storedCookies.get(1));
  }

  /**
   * Tests basic operations without setting a client session.
   */
  @Test
  public void testNullSession() {
    MultiSessionCookieStore cookieStore = new MultiSessionCookieStore();
    // Dont set a client session. Default handling should happen.
    cookieStore.add(s_testuri1, COOKIE1);
    List<HttpCookie> storedCookies = cookieStore.get(s_testuri1);
    assertNotNull(storedCookies);
    assertEquals(1, storedCookies.size());
    assertCookieEquals(COOKIE1, storedCookies.get(0));

    // Now switch to a different client session. Should not have any cookies
    ISession.CURRENT.set(mock(IClientSession.class));
    assertTrue(CollectionUtility.isEmpty(cookieStore.getCookies()));

    // Switch back and clear
    ISession.CURRENT.remove();
    assertTrue("Cookie store should have contained cookie", cookieStore.remove(s_testuri1, COOKIE1));
    assertTrue(CollectionUtility.isEmpty(cookieStore.getCookies()));

  }

  /**
   * Simple test with two client sessions to ensure that adding works and the client sessions don't see each others'
   * cookies.
   */
  @Test
  public void testAddMultipleClients() throws Exception {
    // Simulate concurrency by changing the client session
    MultiSessionCookieStore cookieStore = new MultiSessionCookieStore();
    ISession.CURRENT.set(SESSION1);
    cookieStore.add(s_testuri1, COOKIE1);
    ISession.CURRENT.set(SESSION2);
    cookieStore.add(s_testuri2, COOKIE2);

    // Change back to first
    ISession.CURRENT.set(SESSION1);
    List<HttpCookie> storedCookies = cookieStore.get(s_testuri1);
    assertNotNull(storedCookies);
    assertEquals(1, storedCookies.size());
    assertCookieEquals(COOKIE1, CollectionUtility.firstElement(storedCookies));
    // Change back to second
    ISession.CURRENT.set(SESSION2);
    List<HttpCookie> storedCookies2 = cookieStore.get(s_testuri2);
    assertNotNull(storedCookies2);
    assertEquals(1, storedCookies2.size());
    assertCookieEquals(COOKIE2, CollectionUtility.firstElement(storedCookies2));
  }

  @Test
  public void testGetCookies() throws Exception {
    MultiSessionCookieStore cookieStore = new MultiSessionCookieStore();
    ISession.CURRENT.set(SESSION1);
    createCookies(cookieStore);

    List<HttpCookie> cookies = cookieStore.getCookies();
    assertNotNull(cookies);
    assertEquals(2, cookies.size());
    assertContainsCookies(Arrays.asList(COOKIE1, COOKIE2), cookies);
  }

  @Test
  public void testGetURIs() throws Exception {
    MultiSessionCookieStore cookieStore = new MultiSessionCookieStore();
    ISession.CURRENT.set(SESSION1);
    createCookies(cookieStore);

    List<URI> uris = cookieStore.getURIs();
    assertTrue(CollectionUtility.equalsCollection(Arrays.asList(s_testuri1, s_testuri2), uris));
  }

  @Test
  public void testRemoveAll() throws Exception {
    MultiSessionCookieStore cookieStore = new MultiSessionCookieStore();
    ISession.CURRENT.set(SESSION1);
    createCookies(cookieStore);
    assertFalse(CollectionUtility.isEmpty(cookieStore.getCookies()));
    assertFalse(CollectionUtility.isEmpty(cookieStore.getURIs()));

    // Remove all
    cookieStore.removeAll();

    assertTrue(CollectionUtility.isEmpty(cookieStore.getCookies()));
    assertTrue(CollectionUtility.isEmpty(cookieStore.getURIs()));
  }

  @Test
  public void testRemoveByUri() throws Exception {
    MultiSessionCookieStore cookieStore = new MultiSessionCookieStore();
    ISession.CURRENT.set(SESSION1);
    createCookies(cookieStore);
    assertEquals(2, CollectionUtility.size(cookieStore.getCookies()));

    // Remove cookie from URI1
    assertTrue("CookieStore should have contained cookie", cookieStore.remove(s_testuri1, COOKIE1));

    List<HttpCookie> retrievedCookies = cookieStore.getCookies();
    assertEquals(1, CollectionUtility.size(retrievedCookies));
    assertCookieEquals(COOKIE2, retrievedCookies.get(0));
  }

  @Test
  public void testSessionStopped() throws Exception {
    MultiSessionCookieStore cookieStore = new MultiSessionCookieStore();
    ISession.CURRENT.set(SESSION1);
    createCookies(cookieStore);
    assertFalse(CollectionUtility.isEmpty(cookieStore.getCookies()));
    assertFalse(CollectionUtility.isEmpty(cookieStore.getURIs()));

    cookieStore.sessionStopped(SESSION1);

    assertTrue(CollectionUtility.isEmpty(cookieStore.getCookies()));
    assertTrue(CollectionUtility.isEmpty(cookieStore.getURIs()));
  }

  /**
   * Checks if the expected cookie collection is a subset of the actual cookie collection.
   *
   * @param expectedCookies
   * @param actualCookies
   */
  private void assertContainsCookies(List<HttpCookie> expectedCookies, List<HttpCookie> actualCookies) {
    for (HttpCookie cookie : expectedCookies) {
      if (!actualCookies.contains(cookie)) {
        fail("Expected cookie not found! Expected: " + cookie + ". Actual cookies: " + actualCookies);
      }
      assertCookieEquals(cookie, actualCookies.get(actualCookies.indexOf(cookie)));
    }
  }

  private void createCookies(MultiSessionCookieStore cookieStore) {
    cookieStore.add(s_testuri1, COOKIE1);
    cookieStore.add(s_testuri2, COOKIE2);
  }

  /**
   * Checks for equality of the cookie (including its value).
   *
   * @param expected
   * @param actual
   */
  private void assertCookieEquals(HttpCookie expected, HttpCookie actual) {
    assertEquals(expected, actual);
    if (expected == null) {
      return;
    }
    // Now compare value
    assertEquals(expected.getValue(), actual.getValue());
  }
}
