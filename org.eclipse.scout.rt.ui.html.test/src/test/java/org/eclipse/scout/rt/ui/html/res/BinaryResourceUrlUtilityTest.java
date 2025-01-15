/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResourceUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.junit.Test;
import org.mockito.Mockito;

public class BinaryResourceUrlUtilityTest {

  // Required because utility makes an instanceof IBinaryResourceProvider check
  interface IMockJsonAdapter extends IJsonAdapter<Long>, IBinaryResourceProvider {

  }

  private final BinaryResource m_binaryResource = new BinaryResource("foo.txt", new byte[]{1, 2, 3});
  private final BinaryResource m_binaryResourceSpecialFilename = new BinaryResource("fäé.txt", new byte[]{1, 2, 3});

  // Since we're not interested how exactly the fingerprint is calculated we don't expect a hardcoded value in our tests
  private final long m_fingerprint = m_binaryResource.getFingerprint();
  private final long m_fingerprintSpecialFilename = m_binaryResourceSpecialFilename.getFingerprint();

  @Test
  public void testCreateDynamicAdapterResourceUrl_BinaryResource() {
    IJsonAdapter<Long> jsonAdapter = createJsonAdapterMock("123", "abc");

    String expectedUrl = "dynamic/abc/123/" + m_fingerprint + "/foo.txt";
    assertEquals(expectedUrl, BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(jsonAdapter, m_binaryResource));
  }

  @Test
  public void testCreateDynamicAdapterResourceUrl_BinaryResource_NonAsciiFilename() {
    IJsonAdapter<Long> jsonAdapter = createJsonAdapterMock("123", "abc");

    // URL encoded once
    String expectedUrl = "dynamic/abc/123/" + m_fingerprintSpecialFilename + "/f%C3%A4%C3%A9.txt";
    assertEquals(expectedUrl, BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(jsonAdapter, m_binaryResourceSpecialFilename));
  }

  @Test
  public void testGetFilenameWithFingerprint_DynamicAdapterResourceUrl() {
    IJsonAdapter<Long> jsonAdapter = createJsonAdapterMock("123", "abc");

    String url = "dynamic/abc/123/" + m_fingerprint + "/foo.txt";
    String expectedFilename = m_fingerprint + "/foo.txt";
    assertEquals(expectedFilename, BinaryResourceUrlUtility.getFilenameWithFingerprint(jsonAdapter, IOUtility.urlDecode(url)));
  }

  @Test
  public void testGetFilenameWithFingerprint_DynamicAdapterResourceUrl_NonAsciiFilename() {
    IJsonAdapter<Long> jsonAdapter = createJsonAdapterMock("123", "abc");

    String url = "dynamic/abc/123/" + m_fingerprintSpecialFilename + "/f%C3%A4%C3%A9.txt";
    String expectedFilename = m_fingerprint + "/fäé.txt";
    assertEquals(expectedFilename, BinaryResourceUrlUtility.getFilenameWithFingerprint(jsonAdapter, IOUtility.urlDecode(url)));
  }

  @Test
  public void testExtractFilenameWithFingerprint() {
    assertPairEquals("foo.txt", 0L, BinaryResourceUtility.extractFilenameWithFingerprint("foo.txt"));
    assertPairEquals("foo.txt", 1234L, BinaryResourceUtility.extractFilenameWithFingerprint("1234/foo.txt"));
    assertPairEquals("20190125_072740.jpg", 0L, BinaryResourceUtility.extractFilenameWithFingerprint("20190125_072740.jpg"));
    assertPairEquals("fäé.txt", 0L, BinaryResourceUtility.extractFilenameWithFingerprint("fäé.txt"));
  }

  @Test
  public void testGetFilenameWithFingerprint_WithContent() {
    String expectedFilename = m_fingerprint + "/foo.txt";
    assertEquals(expectedFilename, BinaryResourceUrlUtility.getFilenameWithFingerprint(m_binaryResource));
  }

  @Test
  public void testGetFilenameWithFingerprint_WithContent_NonAsciiFilename() {
    String expectedFilename = m_fingerprintSpecialFilename + "/fäé.txt";
    assertEquals(expectedFilename, BinaryResourceUrlUtility.getFilenameWithFingerprint(m_binaryResourceSpecialFilename));
  }

  @Test
  public void testGetFilenameWithFingerprint_WithoutContent() {
    BinaryResource binaryResource = new BinaryResource("foo.txt", null);
    assertEquals("foo.txt", BinaryResourceUrlUtility.getFilenameWithFingerprint(binaryResource));
  }

  @Test
  public void testGetFilenameWithFingerprint_WithoutContent_NonAsciiFilename() {
    BinaryResource binaryResource = new BinaryResource("fäé.txt", null);
    assertEquals("fäé.txt", BinaryResourceUrlUtility.getFilenameWithFingerprint(binaryResource));
  }

  @Test
  public void testReplaceBinaryRefHandlerWithUrl() {
    assertEquals("\"binref/test/some/123\"", BinaryResourceUrlUtility.replaceBinaryRefHandlerWithUrl("\"binref:/test/some/123\""));
    assertEquals("\"binref/test/some/123\"", BinaryResourceUrlUtility.replaceBinaryRefHandlerWithUrl("\"binref:test/some/123\""));
    assertEquals("\"binref/test/f%C3%A4%C3%A9/123\"", BinaryResourceUrlUtility.replaceBinaryRefHandlerWithUrl("\"binref:/test/fäé/123\""));
  }

  protected IJsonAdapter<Long> createJsonAdapterMock(String adapterId, String uiSessionId) {
    IJsonAdapter<Long> jsonAdapter = Mockito.mock(IMockJsonAdapter.class);
    IUiSession uiSession = Mockito.mock(IUiSession.class);
    Mockito.when(jsonAdapter.getId()).thenReturn(adapterId);
    Mockito.when(jsonAdapter.getUiSession()).thenReturn(uiSession);
    Mockito.when(uiSession.getUiSessionId()).thenReturn(uiSessionId);
    return jsonAdapter;
  }

  private void assertPairEquals(String filename, Long fingerprint, Pair<String, Long> filenameAndFingerprint) {
    assertEquals(filename, filenameAndFingerprint.getLeft());
    assertEquals(fingerprint, filenameAndFingerprint.getRight());
  }
}
