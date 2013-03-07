/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.net.URI;
import java.net.URL;
import java.util.Collections;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link UriUtility}
 * 
 * @since 3.8.1
 */
public class UriUtilityTest {

  static final String UTF8_ENCODING = "UTF-8";

  @Test
  public void testGetQueryParametersNull() throws Exception {
    Assert.assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URI) null));
    Assert.assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URI) null, null));
    Assert.assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URI) null, UTF8_ENCODING));
    Assert.assertEquals(Collections.singletonMap("value", "1"), UriUtility.getQueryParameters(URI.create("scheme://test.com/path?value=1"), null));
    Assert.assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URL) null));
    Assert.assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URL) null, null));
    Assert.assertEquals(Collections.emptyMap(), UriUtility.getQueryParameters((URL) null, UTF8_ENCODING));
    Assert.assertEquals(Collections.singletonMap("value", "1"), UriUtility.getQueryParameters(new URL("http://test.com/path?value=1"), null));
  }

  @Test
  public void testGetPath() throws Exception {
    Assert.assertArrayEquals(new String[0], UriUtility.getPath(null));
    Assert.assertArrayEquals(new String[0], UriUtility.getPath(new URI("")));
    Assert.assertArrayEquals(new String[0], UriUtility.getPath(new URI("scheme://host#anchor")));
    Assert.assertArrayEquals(new String[]{"path", "to"}, UriUtility.getPath(new URI("scheme://host/path/to#anchor")));
  }

  @Test
  public void testGetQueryparameters() throws Exception {
    UriBuilder builder = new UriBuilder("scheme://host/path/to#anchor").parameter("key", "äöü");
    URI uri = builder.createURI();
    Assert.assertEquals(Collections.singletonMap("key", "äöü"), UriUtility.getQueryParameters(uri));
    //
    URI utfUri = builder.createURI(UTF8_ENCODING);
    Assert.assertEquals(Collections.singletonMap("key", "äöü"), UriUtility.getQueryParameters(utfUri, UTF8_ENCODING));
  }

  @Test
  public void testUrlToUri() throws Exception {
    Assert.assertNull(UriUtility.urlToUri(null));
    String s = "http://host/path/to?test=foo#anchor";
    Assert.assertEquals(new URI(s), UriUtility.urlToUri(new URL(s)));
  }

  @Test
  public void testUriToUrl() throws Exception {
    Assert.assertNull(UriUtility.uriToUrl(null));
    String s = "http://host/path/to?test=foo#anchor";
    Assert.assertEquals(new URL(s), UriUtility.uriToUrl(new URI(s)));
    //
    try {
      UriUtility.uriToUrl(new URI("scheme://host"));
      Assert.fail("scheme is not supported by java.lang.URL");
    }
    catch (ProcessingException e) {
      // ok
    }
  }
}
