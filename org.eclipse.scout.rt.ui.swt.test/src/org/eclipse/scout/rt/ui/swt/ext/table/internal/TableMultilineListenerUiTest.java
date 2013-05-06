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
package org.eclipse.scout.rt.ui.swt.ext.table.internal;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.util.ISwtIconLocator;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link TableMultilineListener}
 */
public class TableMultilineListenerUiTest {

  private static final String test3Lines = "1\n2\n3";

  @BeforeClass
  public static void before() throws Exception {
    // Bypass the m_iconLocator on the Activator:
    Field field = Activator.class.getDeclaredField("m_iconLocator");
    field.setAccessible(true);
    field.set(Activator.getDefault(), new ISwtIconLocator() {

      @Override
      public ImageDescriptor getImageDescriptor(String name) {
        return null;
      }

      @Override
      public Image getIcon(String name) {
        return null;
      }

      @Override
      public void dispose() {
      }
    });
  }

  @AfterClass
  public static void after() throws Exception {
    // Reset the m_iconLocator to default:
    Field field = Activator.class.getDeclaredField("m_iconLocator");
    field.setAccessible(true);
    field.set(Activator.getDefault(), null);
  }

  /**
   * Tests trimming row with a row height equal to the text height
   */
  @Test
  public void testTrimToRowHeightEqualRowHeight() {
    TestTableMultilineListener listener = new TestTableMultilineListener(30);
    String trimmed = listener.trimToRowHeight(test3Lines, 10);
    Assert.assertEquals(test3Lines, trimmed);
  }

  /**
   * Tests trimming row with a row height smaller than the text height
   */
  @Test
  public void testTrimToRowHeightLargerRowHeight() {
    TestTableMultilineListener listener = new TestTableMultilineListener(40);
    String trimmed = listener.trimToRowHeight(test3Lines, 10);
    Assert.assertEquals(test3Lines, trimmed);
  }

  /**
   * Tests trimming row with a row height smaller than the text height
   */
  @Test
  public void testTrimToRowHeightSmallerRowHeight() {
    TestTableMultilineListener listener = new TestTableMultilineListener(20);
    String trimmed = listener.trimToRowHeight(test3Lines, 10);
    Assert.assertEquals("1\n2", trimmed);
  }

  /**
   * Tests trimming row without a given row height (the whole text should be displayed)
   */
  @Test
  public void testTrimToRowHeightNoRowHeight() {
    TestTableMultilineListener listener = new TestTableMultilineListener(0);
    String trimmed = listener.trimToRowHeight(test3Lines, 10);
    Assert.assertEquals(test3Lines, trimmed);
  }

  /**
   * Tests trimming a null text should be null
   */
  @Test
  public void testTrimToRowHeightNullText() {
    TestTableMultilineListener listener = new TestTableMultilineListener(20);
    String trimmed = listener.trimToRowHeight(null, 10);
    Assert.assertEquals(null, trimmed);
  }

  /**
   * Tests trimming a empty text should be empty
   */
  @Test
  public void testTrimToRowHeightEmptyText() {
    TestTableMultilineListener listener = new TestTableMultilineListener(20);
    String trimmed = listener.trimToRowHeight("", 10);
    Assert.assertEquals("", trimmed);
  }

  /**
   * Tests trimming a single line
   */
  @Test
  public void testTrimToRowHeightSingleLineText() {
    TestTableMultilineListener listener = new TestTableMultilineListener(20);
    String trimmed = listener.trimToRowHeight("1", 10);
    Assert.assertEquals("1", trimmed);
  }

  /**
   * Tests trimming row without a given text height
   */
  @Test
  public void testTrimToRowHeightNoTextHeight() {
    TestTableMultilineListener listener = new TestTableMultilineListener(10);
    String trimmed = listener.trimToRowHeight(test3Lines, 0);
    Assert.assertEquals(test3Lines, trimmed);
  }

  /**
   * Test for @link{org.eclipse.scout.rt.ui.swt.ext.table.internal.TableMultilineListener#softWrapText}
   */
  @Test
  public void testSoftWrap() {
    TestTableMultilineListener listener = new TestTableMultilineListener(10);
    Rectangle bounds = new Rectangle(0, 0, 10, 100);
    String wrapped = listener.softWrapText(null, "1 2 3", bounds);
    Assert.assertEquals(test3Lines, wrapped);
  }

  /**
   * Test for @link{org.eclipse.scout.rt.ui.swt.ext.table.internal.TableMultilineListener#softWrapText} with null text.
   */
  @Test
  public void testSoftWrapNullText() {
    TestTableMultilineListener listener = new TestTableMultilineListener(10);
    Rectangle bounds = new Rectangle(0, 0, 10, 100);
    String wrapped = listener.softWrapText(null, null, bounds);
    Assert.assertEquals(null, wrapped);
  }

  private static class TestTableMultilineListener extends TableMultilineListener {
    /**
     * Initializes Listener with test values
     */
    public TestTableMultilineListener(int rowHeight) {
      super(true, rowHeight, new HashSet<Integer>(), 10, 10);
    }

    @Override
    public String softWrapText(GC gc, String text, Rectangle bounds) {
      return super.softWrapText(gc, text, bounds);
    }

    /**
     * Override for public access in test
     */
    @Override
    public String trimToRowHeight(String ptext, int fontHeight) {
      return super.trimToRowHeight(ptext, fontHeight);
    }

    /**
     * @return with assuming every character is 5 pixels wide (for testing purposes)
     */
    @Override
    protected int getTextWidth(GC gc, String line) {
      return line.toCharArray().length * 5;
    }
  }
}
