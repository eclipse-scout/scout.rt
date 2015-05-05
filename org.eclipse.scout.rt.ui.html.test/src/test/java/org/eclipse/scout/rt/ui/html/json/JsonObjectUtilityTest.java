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
}
