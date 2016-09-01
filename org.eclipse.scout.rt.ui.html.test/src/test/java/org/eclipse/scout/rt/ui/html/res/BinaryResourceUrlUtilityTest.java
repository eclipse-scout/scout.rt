package org.eclipse.scout.rt.ui.html.res;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.junit.Test;
import org.mockito.Mockito;

public class BinaryResourceUrlUtilityTest {

  // Required because utility makes an instanceof IBinaryResourceProvider check
  interface IMockJsonAdapter extends IJsonAdapter<Long>, IBinaryResourceProvider {

  }

  private BinaryResource m_binaryResource = new BinaryResource("foo.txt", new byte[]{1, 2, 3});

  // Since we're not interested how exactly the fingerprint is calculated we don't expect a hardcoded value in our tests
  private long m_fingerprint = m_binaryResource.getFingerprint();

  @Test
  public void testCreateDynamicAdapterResourceUrl_BinaryResource() {
    IJsonAdapter<Long> jsonAdapter = Mockito.mock(IMockJsonAdapter.class);
    IUiSession uiSession = Mockito.mock(IUiSession.class);
    Mockito.when(jsonAdapter.getId()).thenReturn("123");
    Mockito.when(jsonAdapter.getUiSession()).thenReturn(uiSession);
    Mockito.when(uiSession.getUiSessionId()).thenReturn("abc");

    String expectedUrl = "dynamic/abc/123/" + m_fingerprint + "/foo.txt";
    assertEquals(expectedUrl, BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(jsonAdapter, m_binaryResource));
  }

  @Test
  public void testExtractFilenameWithFingerprint() {
    assertPairEquals("foo.txt", 0L, BinaryResourceUrlUtility.extractFilenameWithFingerprint("foo.txt"));
    assertPairEquals("foo.txt", 1234L, BinaryResourceUrlUtility.extractFilenameWithFingerprint("1234/foo.txt"));
  }

  @Test
  public void testGetFilenameWithFingerprint_WithContent() {
    String expectedFilename = m_fingerprint + "/foo.txt";
    assertEquals(expectedFilename, BinaryResourceUrlUtility.getFilenameWithFingerprint(m_binaryResource));
  }

  @Test
  public void testGetFilenameWithFingerprint_WithoutContent() {
    BinaryResource binaryResource = new BinaryResource("foo.txt", null);
    assertEquals("foo.txt", BinaryResourceUrlUtility.getFilenameWithFingerprint(binaryResource));
  }

  private void assertPairEquals(String filename, Long fingerprint, Pair<String, Long> filenameAndFingerprint) {
    assertEquals(filename, filenameAndFingerprint.getLeft());
    assertEquals(fingerprint, filenameAndFingerprint.getRight());
  }

}
