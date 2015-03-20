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
package org.eclipse.scout.rt.ui.swing.window.filechooser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.scout.rt.ui.swing.window.filechooser.SwingScoutFileChooser.ExtensionFileFilter;
import org.junit.Test;

public class ExtensionFileFilterTest {

  @Test
  public void testNoExtension() throws Exception {
    ExtensionFileFilter filter = new ExtensionFileFilter(Collections.<String> emptyList());
    assertEquals("*.*", filter.getDescription());
    assertTrue(filter.getExtensions().isEmpty());

    assertTrue(filter.accept(new File("abc")));
    assertTrue(filter.accept(new File("abc.txt")));
    assertTrue(filter.accept(new File("abc.jpg")));
  }

  @Test
  public void testSingleExtension() throws Exception {
    ExtensionFileFilter filter = new ExtensionFileFilter(Arrays.asList("txt"));
    assertEquals("*.txt", filter.getDescription());
    assertEquals(Arrays.asList("txt"), filter.getExtensions());

    assertFalse(filter.accept(new File("abc")));
    assertTrue(filter.accept(new File("abc.txt")));
    assertFalse(filter.accept(new File("abc.jpg")));
  }

  @Test
  public void testMultipleExtension() throws Exception {
    ExtensionFileFilter filter = new ExtensionFileFilter(Arrays.asList("txt", "JPG"));
    assertEquals("*.txt;*.jpg", filter.getDescription());
    assertEquals(Arrays.asList("txt", "jpg"), filter.getExtensions());

    assertFalse(filter.accept(new File("abc")));
    assertTrue(filter.accept(new File("abc.txt")));
    assertTrue(filter.accept(new File("abc.jpg")));
    assertTrue(filter.accept(new File("abc.JPG")));
  }
}
