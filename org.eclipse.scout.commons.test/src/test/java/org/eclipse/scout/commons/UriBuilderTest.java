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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * JUnit test for {@link UriBuilder}
 *
 * @since 3.8.1
 */
public class UriBuilderTest {

  static final String SCHEME = "scheme";
  static final String HOST = "host";
  static final String PATH_TO_SCHEME = "/path/to";
  static final String ANCHOR = "anchor";

  @Test
  public void testScheme() throws Exception {
    UriBuilder builder = new UriBuilder();
    assertNull(builder.getScheme());
    //
    assertSame(builder, builder.scheme(null));
    //
    builder.scheme("ftp");
    assertEquals("ftp", builder.getScheme());
    //
    builder = new UriBuilder("http://acme.com:1234/scout");
    assertEquals("http", builder.getScheme());
  }

  @Test
  public void testHost() throws Exception {
    UriBuilder builder = new UriBuilder();
    assertNull(builder.getHost());
    //
    assertSame(builder, builder.host(null));
    //
    String host = "www.eclipse.org";
    builder.host(host);
    assertEquals(host, builder.getHost());
    //
    builder = new UriBuilder("http://acme.com:1234/scout");
    assertEquals("acme.com", builder.getHost());
  }

  @Test
  public void testPort() throws Exception {
    UriBuilder builder = new UriBuilder();
    assertEquals(-1, builder.getPort());
    //
    assertSame(builder, builder.port(-1));
    //
    builder.port(433);
    assertEquals(433, builder.getPort());
    //
    builder.port(1);
    assertEquals(1, builder.getPort());
    //
    builder.port(0);
    assertEquals(-1, builder.getPort());
    //
    builder.port(-15);
    assertEquals(-1, builder.getPort());
    //
    builder = new UriBuilder("http://www.ecipse.org:1234/scout");
    assertEquals(1234, builder.getPort());
    //
    builder.port(42);
    assertEquals(42, builder.getPort());
  }

  @Test
  public void testPath() throws Exception {
    UriBuilder builder = new UriBuilder();
    assertNull(builder.getPath());
    //
    assertSame(builder, builder.path(null));
    //
    String path = "scout/foo";
    builder.path(path);
    assertEquals(path, builder.getPath());
    //
    builder = new UriBuilder("http://acme.com:1234/scout/test/3");
    assertEquals("/scout/test/3", builder.getPath());
  }

  @Test
  public void testAddPath() throws Exception {
    UriBuilder builder = new UriBuilder();
    assertNull(builder.getPath());
    //
    builder.addPath(null);
    assertNull(builder.getPath());
    //
    assertSame(builder, builder.addPath(null));
    //
    String path = "scout/foo";
    builder.addPath(path);
    assertEquals(path, builder.getPath());
    //
    builder = new UriBuilder("http://acme.com:1234/scout/test/3");
    builder.addPath("test");
    assertEquals("/scout/test/3/test", builder.getPath());
    //
    builder = new UriBuilder("http://acme.com:1234");
    builder.addPath("test");
    assertEquals("test", builder.getPath());
  }

  @Test
  public void testFragment() throws Exception {
    UriBuilder builder = new UriBuilder();
    assertNull(builder.getFragment());
    //
    assertSame(builder, builder.fragment(null));
    //
    String fragment = ANCHOR;
    builder.fragment(fragment);
    assertEquals(fragment, builder.getFragment());
    //
    builder = new UriBuilder("http://acme.com:1234/scout/test/3#bottomPart");
    assertEquals("bottomPart", builder.getFragment());
  }

  static final String NAME1 = "name1";
  static final String NAME2 = "name2";
  static final String VALUE1 = "value1";
  static final String VALUE2 = "value2";

  @Test
  public void testParamenter() throws Exception {
    UriBuilder builder = new UriBuilder();
    assertNotNull(builder.getParameters());
    assertTrue(builder.getParameters().isEmpty());
    //
    builder.parameter(null, null);
    builder.parameter(NAME1, null);
    builder.parameter(null, VALUE1);
    builder.parameter(NAME2, VALUE2);
    assertEquals(1, builder.getParameters().size());
    assertEquals(VALUE2, builder.getParameters().get(NAME2));
    builder.parameter(NAME2, null);
    assertTrue(builder.getParameters().isEmpty());
    //
    assertSame(builder, builder.parameter(null, null));
    //
    builder = new UriBuilder("http://acme.com:1234/scout?name1=value1&name2=value2");
    assertEquals(2, builder.getParameters().size());
    assertEquals(VALUE1, builder.getParameters().get(NAME1));
    assertEquals(VALUE2, builder.getParameters().get(NAME2));
  }

  @Test
  public void testCreateUri() throws Exception {
    UriBuilder builder = new UriBuilder();
    assertEquals(URI.create(""), builder.createURI());
    //
    builder = new UriBuilder();
    builder.scheme(SCHEME).host(HOST);
    assertEquals(URI.create("scheme://host"), builder.createURI());
    //
    builder = new UriBuilder();
    builder.scheme(SCHEME).path(PATH_TO_SCHEME);
    assertEquals(URI.create("scheme:/path/to"), builder.createURI());
    //
    builder = new UriBuilder();
    builder.scheme(SCHEME).host(HOST).path(PATH_TO_SCHEME);
    assertEquals(URI.create("scheme://host/path/to"), builder.createURI());
    //
    builder = new UriBuilder();
    builder.scheme(SCHEME).host(HOST).path(PATH_TO_SCHEME).fragment(ANCHOR);
    assertEquals(URI.create("scheme://host/path/to#anchor"), builder.createURI());
    //
    builder = new UriBuilder();
    builder.scheme(SCHEME).host(HOST).path(PATH_TO_SCHEME).fragment(ANCHOR).parameter("key", "value");
    assertEquals(URI.create("scheme://host/path/to?key=value#anchor"), builder.createURI());
    //
    builder = new UriBuilder();
    builder.scheme(SCHEME).host(HOST).path(PATH_TO_SCHEME).fragment(ANCHOR).parameter("key", "äöü");
    assertEquals("scheme://host/path/to?key=%25E4%25F6%25FC#anchor", builder.createURI().toASCIIString());
    //
    builder = new UriBuilder();
    builder.scheme(SCHEME).host(HOST).path(PATH_TO_SCHEME).fragment(ANCHOR).parameter("key", "äöü");
    assertEquals("scheme://host/path/to?key=%25C3%25A4%25C3%25B6%25C3%25BC#anchor", builder.createURI(StandardCharsets.UTF_8.name()).toASCIIString());
    //
    URI baseUri = new URI("http://www.eclipse.org/scout");
    builder = new UriBuilder(baseUri);
    assertEquals(baseUri, builder.createURI());
  }
}
