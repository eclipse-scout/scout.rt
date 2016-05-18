/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.ui.html.ResourceBase;
import org.junit.Test;

public class JsonUtilityTest {

  @Test
  public void testJsonWithComments() throws IOException {
    // null string
    assertEquals(null, JsonUtility.stripCommentsFromJson(null));
    // empty string
    assertEquals("", JsonUtility.stripCommentsFromJson(""));
    // plain string (not JSON)
    assertEquals("\"test\"", JsonUtility.stripCommentsFromJson("\"test\""));
    // empty object
    assertEquals("{}", JsonUtility.stripCommentsFromJson("{}"));
    // '//' inside string
    assertEquals("{\n  \"location\": \"Baden // Switzerland\"\n}\n", JsonUtility.stripCommentsFromJson("\n{\n\r\n  \"location\": \"Baden // Switzerland\" // Person's location\r}\n"));
    // mix of comment types
    assertEquals("{\n  \"location\":  \"Baden // Switzerland\"\n}\n", JsonUtility.stripCommentsFromJson("\n{\n\r\n  \"location\": /* just an example*/ \"Baden // Switzerland\" // Person's location\r}\n"));
    // comment sequences inside string
    assertEquals("{\n  \"info\": \"three /* four\"\n}\n", JsonUtility.stripCommentsFromJson("\n{\n\r\n  \"info\": /*\"one /* two\"*/\"three /* four\" // */\r}\n"));
    // escaped \" inside string
    assertEquals("{\n  \"info\": \"one /*\\\" two\"\n}\n", JsonUtility.stripCommentsFromJson("\n{\n\r\n  \"info\": \"one /*\\\" two\"\r}\n"));
    // block comment with no end
    assertEquals("", JsonUtility.stripCommentsFromJson("/*\n{\n\r\n  \"location\": \"Baden // Switzerland\" // Person's location\r}\n"));

    // load complex json file without comments -> verify that stripping does not change anything
    String json;
    try (InputStream in = ResourceBase.class.getResourceAsStream("json/DefaultValuesFilterTest_defaults_simple.json")) {
      json = IOUtility.readStringUTF8(in);
    }
    assertEquals(json, JsonUtility.stripCommentsFromJson(json));

    // another tes tfile
    try (InputStream in = ResourceBase.class.getResourceAsStream("json/DefaultValuesFilterTest_defaults_withComments.json")) {
      json = IOUtility.readStringUTF8(in);
    }
    assertEquals("{\n  \"defaults\": {\n    \"FormField\": {\n      \"enabled\": true\n    }\n  }\n}\n", JsonUtility.stripCommentsFromJson(json));
  }

}
