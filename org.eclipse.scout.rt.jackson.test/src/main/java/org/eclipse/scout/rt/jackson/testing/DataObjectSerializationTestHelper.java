/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.testing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.junit.Assert;

/**
 * Helper dealing with JSON serialization/deserialization tests for data objects
 */
@ApplicationScoped
public class DataObjectSerializationTestHelper {

  protected static final LazyValue<IPrettyPrintDataObjectMapper> s_dataObjectMapper = new LazyValue<>(IPrettyPrintDataObjectMapper.class);

  protected static final LazyValue<DataObjectHelper> s_dataObjectHelper = new LazyValue<>(DataObjectHelper.class);

  /**
   * Checks if the given data objects, represented as JSON strings, are equal. <br>
   * Line endings and leading and trailing white spaces are ignored.
   */
  public void assertJsonEquals(String expected, String actual) {
    expected = toUnixLineEndingsAndTrim(expected);
    actual = toUnixLineEndingsAndTrim(actual);
    Assert.assertEquals(expected, actual);
  }

  /**
   * Converts the given actual {@link IDoEntity} to a JSON string and invokes {@link #assertJsonEquals(String, String)}.
   */
  public void assertJsonEquals(String expected, IDoEntity actual) {
    assertJsonEquals(expected, stringify(actual));
  }

  /**
   * Converts the given expected {@link IDoEntity} to a string and invokes {@link #assertJsonEquals(String, String)}.
   */
  public void assertJsonEquals(IDoEntity expected, String actual) {
    assertJsonEquals(stringify(expected), actual);
  }

  /**
   * Converts the given actual and expected {@link IDoEntity} to strings and invokes
   * {@link #assertJsonEquals(String, String)}.
   */
  public void assertJsonEquals(IDoEntity expected, IDoEntity actual) {
    assertJsonEquals(stringify(expected), stringify(actual));
  }

  /**
   * Loads the expected JSON string from the specified {@code expectedJsonResource} URL and invokes
   * {@link #assertJsonEquals(String, String)}.
   */
  public void assertJsonEquals(URL expectedJsonResource, String actual) {
    try {
      String expected = readResourceAsString(expectedJsonResource);
      assertJsonEquals(expected, actual);
    }
    catch (IOException e) {
      fail("failed to load resource, error message=" + e.getMessage() + " exception=" + e);
    }
  }

  /**
   * Loads the expected JSON string from the specified {@code expectedJsonResource} URL, converts the given actual
   * {@link IDoEntity} to a JSON string and invokes {@link #assertJsonEquals(String, String)}.
   */
  public void assertJsonEquals(URL expectedJsonResource, IDoEntity actual) {
    assertJsonEquals(expectedJsonResource, stringify(actual));
  }

  /**
   * Reads a {@link String} from the specified {@code URL} resource.
   */
  public String readResourceAsString(URL url) throws IOException {
    assertNotNull("Invalid expected resource URL", url);
    return IOUtility.readStringUTF8(url.openStream());
  }

  /**
   * Converts the specified {@code dataObject} to a JSON string
   */
  public String stringify(IDoEntity dataObject) {
    return s_dataObjectMapper.get().writeValue(dataObject);
  }

  /**
   * Parses the specified JSON string to a {@link IDoEntity}.
   */
  public <T extends IDoEntity> T parse(String json, Class<T> valueType) {
    return s_dataObjectMapper.get().readValue(json, valueType);
  }

  /**
   * Clones the given data object using data object serialization and deserialization.
   */
  public <T extends IDoEntity> T clone(T dataObject) {
    return s_dataObjectHelper.get().clone(dataObject);
  }

  protected String toUnixLineEndingsAndTrim(String s) {
    if (s == null) {
      return s;
    }
    return s.replaceAll("\\r\\n", "\\\n").trim();
  }
}
