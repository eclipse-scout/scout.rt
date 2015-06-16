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
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

public class JsonObjectUtilityTest {

  @Test
  public void testJsonWithHtmlCharacters() throws Exception {
    assertEquals("foo", toJson("foo"));
    assertEquals("<foo>", toJson("<foo>"));
    assertEquals("foo & bar", toJson("foo & bar"));
    assertEquals("<!-- foo -->", toJson("<!-- foo -->"));
    assertEquals("foo < bar", toJson("foo < bar"));
  }

  private String toJson(String s) {
    return MainJsonObjectFactory.get().createJsonObject(s).toJson().toString();
  }

  @Test
  public void testPutProperties() {
    JsonObjectUtility.putProperties(null, null); // should not throw exception
    JsonObjectUtility.putProperties(new JSONObject(), null); // should not throw exception
    JsonObjectUtility.putProperties(null, new JSONObject()); // should not throw exception

    JSONObject a = new JSONObject();
    a.put("test", "Test-String");
    a.put("date", false);
    a.put("value", 12345);

    JSONObject b = new JSONObject();
    b.put("existing", true);
    b.put("test", false);

    JsonObjectUtility.putProperties(b, a);

    // check a
    assertEquals("Test-String", a.opt("test"));
    assertEquals(Boolean.FALSE, a.opt("date"));
    assertEquals(12345, a.opt("value"));
    assertEquals(null, a.opt("existing"));
    assertEquals(null, a.opt("undefined"));
    // check b
    assertEquals("Test-String", b.opt("test"));
    assertEquals(Boolean.FALSE, b.opt("date"));
    assertEquals(12345, b.opt("value"));
    assertEquals(Boolean.TRUE, b.opt("existing"));
    assertEquals(null, b.opt("undefined"));
  }
}
