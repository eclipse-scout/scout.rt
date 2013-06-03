/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Locale;
import java.util.Map;

import org.junit.Test;

/**
 * JUnit tests for {@link ScoutTexts} and {@link TEXTS}
 */
public class TextsTest {

  @Test
  public void testGet() {
    assertEquals("{undefined text anyKey}", TEXTS.get("anyKey"));
  }

  @Test
  public void testGetTextMap() {
    Map<String, String> textMap = ScoutTexts.getInstance().getTextMap(Locale.ENGLISH);
    assertNotNull(textMap);
  }
}
