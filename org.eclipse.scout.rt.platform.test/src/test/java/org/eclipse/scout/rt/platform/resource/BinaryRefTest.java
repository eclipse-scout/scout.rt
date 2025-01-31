/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.resource;

import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BinaryRefTest {

  protected final List<IBean<?>> m_beans = new ArrayList<>();

  private static final URI NO_BINREF_URI = URI.create("foobar:/test/existing");

  private static final URI NO_HANDLER_BINREF_URI = URI.create("binref:/nohandler/some");

  private static final URI UNKNOWN_BINREF_URI = URI.create("binref:/test/unknown");
  private static final URI RELATIVE_BINREF_URI = URI.create("binref:test/existing1.txt");
  private static final URI EXISTING_1_BINREF_URI = URI.create("binref:/test/existing1.txt");
  private static final URI EXISTING_2_BINREF_URI = URI.create("binref:/test/existing2.png");

  @Before
  public void before() {
    m_beans.add(BeanTestingHelper.get().registerBean(new BeanMetaData(TestingBinaryRefHandler.class, new TestingBinaryRefHandler(EXISTING_1_BINREF_URI, EXISTING_2_BINREF_URI))));
    BEANS.get(BinaryRefSupport.class).reload();
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
    m_beans.clear();
    BEANS.get(BinaryRefSupport.class).reload();
  }

  protected Set<URI> uris(URI... uris) {
    return new HashSet<>(Arrays.asList(uris));
  }

  @Test
  public void testGetDisplayTexts() {
    assertTrue(BinaryRefs.getDisplayTexts(null).isEmpty());

    Map<URI, String> displayTexts = BinaryRefs.getDisplayTexts(uris(null, NO_BINREF_URI, NO_HANDLER_BINREF_URI, UNKNOWN_BINREF_URI, RELATIVE_BINREF_URI, EXISTING_1_BINREF_URI, EXISTING_2_BINREF_URI));
    assertEquals(uris(NO_HANDLER_BINREF_URI, UNKNOWN_BINREF_URI, RELATIVE_BINREF_URI, EXISTING_1_BINREF_URI, EXISTING_2_BINREF_URI), displayTexts.keySet());
    assertEquals("Unknown URI [" + NO_HANDLER_BINREF_URI + "]", displayTexts.get(NO_HANDLER_BINREF_URI));
    assertEquals("Unknown URI [" + UNKNOWN_BINREF_URI + "]", displayTexts.get(UNKNOWN_BINREF_URI));
    assertEquals("Unknown URI [" + RELATIVE_BINREF_URI + "]", displayTexts.get(RELATIVE_BINREF_URI));
    assertEquals("File: existing1.txt", displayTexts.get(EXISTING_1_BINREF_URI));
    assertEquals("File: existing2.png", displayTexts.get(EXISTING_2_BINREF_URI));
  }

  @Test
  public void testGetDisplayTextsFallback() {
    assertTrue(BinaryRefs.getDisplayTexts(null, URI::toString).isEmpty());

    Map<URI, String> displayTexts = BinaryRefs.getDisplayTexts(uris(null, NO_BINREF_URI, NO_HANDLER_BINREF_URI, UNKNOWN_BINREF_URI, RELATIVE_BINREF_URI, EXISTING_1_BINREF_URI, EXISTING_2_BINREF_URI), URI::toString);
    assertEquals(uris(NO_HANDLER_BINREF_URI, UNKNOWN_BINREF_URI, RELATIVE_BINREF_URI, EXISTING_1_BINREF_URI, EXISTING_2_BINREF_URI), displayTexts.keySet());
    assertEquals(NO_HANDLER_BINREF_URI.toString(), displayTexts.get(NO_HANDLER_BINREF_URI));
    assertEquals(UNKNOWN_BINREF_URI.toString(), displayTexts.get(UNKNOWN_BINREF_URI));
    assertEquals(RELATIVE_BINREF_URI.toString(), displayTexts.get(RELATIVE_BINREF_URI));
    assertEquals("File: existing1.txt", displayTexts.get(EXISTING_1_BINREF_URI));
    assertEquals("File: existing2.png", displayTexts.get(EXISTING_2_BINREF_URI));
  }

  @Test
  public void testGetDisplayText() {
    assertNull(BinaryRefs.getDisplayText(null));
    assertNull(BinaryRefs.getDisplayText(NO_BINREF_URI));

    assertEquals("Unknown URI [" + NO_HANDLER_BINREF_URI + "]", BinaryRefs.getDisplayText(NO_HANDLER_BINREF_URI));
    assertEquals("Unknown URI [" + UNKNOWN_BINREF_URI + "]", BinaryRefs.getDisplayText(UNKNOWN_BINREF_URI));
    assertEquals("Unknown URI [" + RELATIVE_BINREF_URI + "]", BinaryRefs.getDisplayText(RELATIVE_BINREF_URI));
    assertEquals("File: existing1.txt", BinaryRefs.getDisplayText(EXISTING_1_BINREF_URI));
    assertEquals("File: existing2.png", BinaryRefs.getDisplayText(EXISTING_2_BINREF_URI));
  }

  @Test
  public void testGetFilenames() {
    assertTrue(BinaryRefs.getFilenames(null).isEmpty());

    Map<URI, String> filenames = BinaryRefs.getFilenames(uris(null, NO_BINREF_URI, NO_HANDLER_BINREF_URI, UNKNOWN_BINREF_URI, RELATIVE_BINREF_URI, EXISTING_1_BINREF_URI, EXISTING_2_BINREF_URI));
    assertEquals(uris(EXISTING_1_BINREF_URI, EXISTING_2_BINREF_URI), filenames.keySet());
    assertEquals("existing1.txt", filenames.get(EXISTING_1_BINREF_URI));
    assertEquals("existing2.png", filenames.get(EXISTING_2_BINREF_URI));
  }

  @Test
  public void testGetFilename() {
    assertNull(BinaryRefs.getFilename(null));
    assertNull(BinaryRefs.getFilename(NO_BINREF_URI));
    assertNull(BinaryRefs.getFilename(NO_HANDLER_BINREF_URI));
    assertNull(BinaryRefs.getFilename(UNKNOWN_BINREF_URI));
    assertNull(BinaryRefs.getFilename(RELATIVE_BINREF_URI));
    assertEquals("existing1.txt", BinaryRefs.getFilename(EXISTING_1_BINREF_URI));
    assertEquals("existing2.png", BinaryRefs.getFilename(EXISTING_2_BINREF_URI));
  }

  @Test
  public void testGetContentTypes() {
    assertTrue(BinaryRefs.getContentTypes(null).isEmpty());

    Map<URI, String> contentTypes = BinaryRefs.getContentTypes(uris(null, NO_BINREF_URI, NO_HANDLER_BINREF_URI, UNKNOWN_BINREF_URI, RELATIVE_BINREF_URI, EXISTING_1_BINREF_URI, EXISTING_2_BINREF_URI));
    assertEquals(uris(EXISTING_1_BINREF_URI, EXISTING_2_BINREF_URI), contentTypes.keySet());
    assertEquals("text/plain", contentTypes.get(EXISTING_1_BINREF_URI));
    assertEquals("image/png", contentTypes.get(EXISTING_2_BINREF_URI));
  }

  @Test
  public void testGetContentType() {
    assertNull(BinaryRefs.getContentType(null));
    assertNull(BinaryRefs.getContentType(NO_BINREF_URI));
    assertNull(BinaryRefs.getContentType(NO_HANDLER_BINREF_URI));
    assertNull(BinaryRefs.getContentType(UNKNOWN_BINREF_URI));
    assertNull(BinaryRefs.getContentType(RELATIVE_BINREF_URI));
    assertEquals("text/plain", BinaryRefs.getContentType(EXISTING_1_BINREF_URI));
    assertEquals("image/png", BinaryRefs.getContentType(EXISTING_2_BINREF_URI));
  }

  @Test
  public void testLoadBinaryResource() {
    assertNull(BinaryRefs.loadBinaryResource(null));
    assertNull(BinaryRefs.loadBinaryResource(NO_BINREF_URI));
    Assert.assertThrows(IllegalStateException.class, () -> BinaryRefs.loadBinaryResource(NO_HANDLER_BINREF_URI));
    Assert.assertThrows(IllegalStateException.class, () -> BinaryRefs.loadBinaryResource(UNKNOWN_BINREF_URI));
    Assert.assertThrows(IllegalStateException.class, () -> BinaryRefs.loadBinaryResource(RELATIVE_BINREF_URI));
    assertEquals(new BinaryResource("existing1.txt", new byte[0]), BinaryRefs.loadBinaryResource(EXISTING_1_BINREF_URI));
    assertEquals(new BinaryResource("existing2.png", new byte[0]), BinaryRefs.loadBinaryResource(EXISTING_2_BINREF_URI));
  }

  @Test
  public void testLoadBinaryResourceOrNull() {
    assertNull(BinaryRefs.loadBinaryResourceOrNull(null));
    assertNull(BinaryRefs.loadBinaryResourceOrNull(NO_BINREF_URI));
    assertNull(BinaryRefs.loadBinaryResourceOrNull(NO_HANDLER_BINREF_URI));
    assertNull(BinaryRefs.loadBinaryResourceOrNull(UNKNOWN_BINREF_URI));
    assertNull(BinaryRefs.loadBinaryResourceOrNull(RELATIVE_BINREF_URI));
    assertEquals(new BinaryResource("existing1.txt", new byte[0]), BinaryRefs.loadBinaryResourceOrNull(EXISTING_1_BINREF_URI));
    assertEquals(new BinaryResource("existing2.png", new byte[0]), BinaryRefs.loadBinaryResourceOrNull(EXISTING_2_BINREF_URI));
  }

  protected static class TestingBinaryRefHandler implements IBinaryRefHandler {

    private final Collection<URI> m_supportedUris;

    public TestingBinaryRefHandler(URI... supportedUris) {
      m_supportedUris = new ArrayList<>(Arrays.asList(supportedUris));
    }

    @Override
    public String getRegistrationPath() {
      return "/test";
    }

    @Override
    public BinaryResource loadBinaryResource(URI uri) {
      if (m_supportedUris.contains(uri)) {
        return new BinaryResource(getFilename(uri), new byte[0]);
      }
      return null;
    }

    @Override
    public void getDisplayTexts(Map<URI, String> resultCollector, Collection<URI> uris) {
      uris.stream().filter(m_supportedUris::contains).forEach(uri -> resultCollector.put(uri, "File: " + getFilename(uri)));
    }

    @Override
    public void getFilenames(Map<URI, String> resultCollector, Collection<URI> uris) {
      uris.stream().filter(m_supportedUris::contains).forEach(uri -> resultCollector.put(uri, getFilename(uri)));
    }

    protected String getFilename(URI uri) {
      return Paths.get(uri.getPath()).getFileName().toString();
    }
  }
}
