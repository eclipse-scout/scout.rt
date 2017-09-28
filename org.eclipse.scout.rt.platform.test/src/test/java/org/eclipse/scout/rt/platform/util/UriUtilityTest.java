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
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.junit.Test;

/**
 * JUnit tests for {@link UriUtility}
 *
 * @since 3.8.1
 */
public class UriUtilityTest {

  @Test
  public void testGetQueryParametersNull() {
    assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URI) null));
    assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URI) null, null));
    assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URI) null, StandardCharsets.UTF_8.name()));
    assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URL) null));
    assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URL) null, null));
    assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URL) null, StandardCharsets.UTF_8.name()));
  }

  @Test
  public void testQueryParameters() throws MalformedURLException {
    assertEquals(Collections.singletonMap("value", "1"), UriUtility.getQueryParameters(URI.create("scheme://test.com/path?value=1")));
    assertEquals(Collections.singletonMap("value", "1"), UriUtility.getQueryParameters(new URL("http://test.com/path?value=1")));
    assertEquals(Collections.singletonMap("value", "1"), UriUtility.getQueryParameters(URI.create("http://test.com/path?value=1#fragment")));
    assertEquals(Collections.singletonMap("x", "20D"), UriUtility.getQueryParameters(URI.create("http://localhost/?x=20D")));
    assertTrue(UriUtility.getQueryParameters(URI.create("scheme://test.com/path/path2")).isEmpty());
  }

  @Test
  public void testGetPath() throws Exception {
    assertArrayEquals(new String[0], UriUtility.getPath(null));
    assertArrayEquals(new String[0], UriUtility.getPath(new URI("")));
    assertArrayEquals(new String[0], UriUtility.getPath(new URI("scheme://host#anchor")));
    assertArrayEquals(new String[]{"path", "to"}, UriUtility.getPath(new URI("scheme://host/path/to#anchor")));
  }

  @Test
  public void testGetQueryparameters() throws Exception {
    UriBuilder builder = new UriBuilder("scheme://host/path/to#anchor").parameter("key", "äöü");
    URI uri = builder.createURI();
    assertEquals(Collections.singletonMap("key", "äöü"), UriUtility.getQueryParameters(uri));
    //
    URI utfUri = builder.createURI(StandardCharsets.UTF_8.name());
    assertEquals(Collections.singletonMap("key", "äöü"), UriUtility.getQueryParameters(utfUri, StandardCharsets.UTF_8.name()));
  }

  @Test
  public void testUrlToUri() throws Exception {
    assertNull(UriUtility.urlToUri(null));
    String s = "http://host/path/to?test=foo#anchor";
    assertEquals(new URI(s), UriUtility.urlToUri(new URL(s)));
  }

  @Test
  public void testUriToUrl() throws Exception {
    assertNull(UriUtility.uriToUrl(null));
    String s = "http://host/path/to?test=foo#anchor";
    assertEquals(new URL(s), UriUtility.uriToUrl(new URI(s)));
    //
    try {
      UriUtility.uriToUrl(new URI("scheme://host"));
      fail("scheme is not supported by java.lang.URL");
    }
    catch (ProcessingException e) {
      // ok
    }
  }

}
