package org.eclipse.scout.rt.client.test.ui.desktop.navigation;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.bookmark.internal.BookmarkUtility;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.junit.Test;

/**
 * Test for {@link org.eclipse.scout.rt.client.services.common.bookmark.internal.BookmarkUtility#makeSerializableKey
 * BookmarkUtility#makeSerializableKey}
 */
public class BookmarkUtilityTest {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BookmarkUtilityTest.class);

  /**
   * Tests that Bookmark[] is converted to String[]
   */
  @Test
  public void testBookmarkArrayKey() {
    Bookmark[] in = new Bookmark[]{new Bookmark()};
    Object out = BookmarkUtility.makeSerializableKey(in);
    assertTrue(out instanceof String[]);
  }

  /**
   * Tests that Bookmark is converted to String
   */
  @Test
  public void testBookmarkKey() {
    Bookmark in = new Bookmark();
    Object out = BookmarkUtility.makeSerializableKey(in);
    assertTrue(out instanceof String);
  }

  @Test
  public void testMakeSerializableNullKey() {
    Object in = null;
    Object out = BookmarkUtility.makeSerializableKey(in);
    assertNull(out);
  }

  @Test
  public void testEmptyStringArrayKey() {
    String[] in = new String[0];
    Object out = BookmarkUtility.makeSerializableKey(in);
    assertValidKey(in, out);
  }

  @Test
  public void testStringArrayKey() {
    String[] in = new String[1];
    Object out = BookmarkUtility.makeSerializableKey(in);
    assertValidKey(in, out);
  }

  @Test
  public void testStringMatrixKey() {
    String[][] in = new String[1][0];
    Object out = BookmarkUtility.makeSerializableKey(in);
    assertValidKey(in, out);
  }

  @Test
  public void testStringMatrix1Key() {
    String[][] in = new String[0][1];
    Object out = BookmarkUtility.makeSerializableKey(in);
    assertValidKey(in, out);
  }

  @Test
  public void testStringMatrixFullKey() {
    String[][] in = new String[][]{new String[]{"aaa", "bbb"}, new String[]{"ccc",}};
    Object out = BookmarkUtility.makeSerializableKey(in);
    assertValidKey(in, out);
  }

  @Test
  public void testIntArrayKey() {
    int[] in = new int[]{3, 2, 1};
    Object out = BookmarkUtility.makeSerializableKey(in);
    assertValidKey(in, out);
  }

  private void assertValidKey(Object in, Object out) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Input:  " + (in != null ? in.getClass() : null) + " " + VerboseUtility.dumpObject(in));
      LOG.debug("Output: " + (out != null ? out.getClass() : null) + " " + VerboseUtility.dumpObject(out));
    }
    assertTrue(CompareUtility.equals(in, out));
    if (in != null && out != null) {
      assertTrue(in.getClass().equals(out.getClass()));
    }
  }

}
