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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * tests the {@link ListUtility}
 */
public class ListUtilityTest extends Assert {

  @Test
  public void testLength() throws Exception {
    assertEquals(-1, ListUtility.length(null));
    assertEquals(3, ListUtility.length(new int[3]));
    assertEquals(3, ListUtility.length(new int[3][4]));
    assertEquals(3, ListUtility.length(new String[3]));
    assertEquals(3, ListUtility.length(new String[3][4]));
    assertEquals(3, ListUtility.length(new ArrayList<String>(Arrays.asList(new String[]{"a", "b", "c"}))));
    assertEquals(0, ListUtility.length(new HashMap<String, String>()));
  }
}
