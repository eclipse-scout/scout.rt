/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.platform.dataobject.DataObjectTestHelper;

/**
 * Helper dealing with JSON serialization/deserialization tests for data objects
 */
@ApplicationScoped
public class DataObjectSerializationTestHelper extends DataObjectTestHelper {

  protected static final String SINGLE_NEW_LINE = "\n";

  /**
   * Asserts equality of two JSON strings allowing different line ending formats.
   */
  public void assertJsonEquals(String expected, String actual) {
    expected = StringUtility.replaceNewLines(expected, SINGLE_NEW_LINE).trim();
    actual = StringUtility.replaceNewLines(actual, SINGLE_NEW_LINE).trim();
    assertEquals(expected, actual);
  }

  /**
   * Asserts equality of two JSON strings allowing different line ending formats.<br>
   * The expected JSON string is loaded from the specified {@code expectedJsonResource} URL.
   */
  public void assertJsonResourceEquals(URL expectedJsonResource, String actual) {
    try {
      String expected = readResourceAsString(expectedJsonResource);
      expected = StringUtility.replaceNewLines(expected, SINGLE_NEW_LINE).trim();
      actual = StringUtility.replaceNewLines(actual, SINGLE_NEW_LINE).trim();
      assertEquals("JSON mismatch", expected, actual);
    }
    catch (IOException e) {
      fail("failed to load resource, error message=" + e.getMessage() + " exception=" + e);
    }
  }

  /**
   * Reads string from the specified {@code URL} resource.
   */
  public String readResourceAsString(URL url) throws IOException {
    assertNotNull("Invalid expected resource URL", url);
    return IOUtility.readStringUTF8(url.openStream());
  }
}
