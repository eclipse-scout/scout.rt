package org.eclipse.scout.rt.client.test.ui.desktop.navigation;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

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
   * Tests that Bookmark[] is converted to String[] when legacySupport is true
   */
  @Test
  public void testBookmarkArrayKeyLegacy() {
    Bookmark[] in = new Bookmark[]{new Bookmark()};
    Object out = BookmarkUtility.makeSerializableKey(in, true);
    assertTrue(out instanceof String[]);
  }

  /**
   * Tests that Bookmark[] is NOT converted to String[] when legacySupport is false
   */
  @Test
  public void testBookmarkArrayKey() {
    Bookmark[] in = new Bookmark[]{new Bookmark()};
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertTrue(out instanceof Bookmark[]);
  }

  /**
   * Tests that Bookmark is converted to String when legacySupport is true
   */
  @Test
  public void testBookmarkKeyLegacy() {
    Bookmark in = new Bookmark();
    Object out = BookmarkUtility.makeSerializableKey(in, true);
    assertTrue(out instanceof String);
  }

  /**
   * Tests that Bookmark is converted to String when legacySupport is false
   */
  @Test
  public void testBookmarkKey() {
    Bookmark in = new Bookmark();
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertTrue(out instanceof Bookmark);
  }

  @Test
  public void testMakeSerializableNullKey() {
    Object in = null;
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertNull(out);
  }

  @Test
  public void testEmptyStringArrayKey() {
    String[] in = new String[0];
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKey(in, out);
  }

  @Test
  public void testStringArrayKey() {
    String[] in = new String[1];
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKey(in, out);
  }

  @Test
  public void testStringMatrixKey() {
    String[][] in = new String[1][0];
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKey(in, out);
  }

  @Test
  public void testStringMatrix1Key() {
    String[][] in = new String[0][1];
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKey(in, out);
  }

  @Test
  public void testStringMatrixFullKey() {
    String[][] in = new String[][]{new String[]{"aaa", "bbb"}, new String[]{"ccc",}};
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKey(in, out);
  }

  @Test
  public void testIntArrayKey() {
    int[] in = new int[]{3, 2, 1};
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKey(in, out);
  }

  @Test
  public void testSerializablePrimaryKey() {
    SerializablePrimaryKey in = new SerializablePrimaryKey(1234L);
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKey(in, out);
  }

  @Test
  public void testUnSerializablePrimaryKey() {
    UnSerializablePrimaryKey in = new UnSerializablePrimaryKey(1234L);
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKeyWithDumpObject(in, out);
  }

  @Test
  public void testUnSerializablePrimaryKeyWithToString() {
    UnSerializablePrimaryKeyWithToString in = new UnSerializablePrimaryKeyWithToString(1234L);
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKeyWithDumpObject(in, out);
  }

  @Test
  public void testSerializablePrimaryArrayKey() {
    SerializablePrimaryKey[] in = new SerializablePrimaryKey[]{new SerializablePrimaryKey(1234L)};
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKey(in, out);
  }

  @Test
  public void testUnSerializablePrimaryArrayKey() {
    UnSerializablePrimaryKey[] in = new UnSerializablePrimaryKey[]{new UnSerializablePrimaryKey(1234L)};
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKeyWithDumpObject(in, out);
  }

  @Test
  public void testUnSerializablePrimaryArrayKeyWithToString() {
    UnSerializablePrimaryKeyWithToString[] in = new UnSerializablePrimaryKeyWithToString[]{new UnSerializablePrimaryKeyWithToString(1234L)};
    Object out = BookmarkUtility.makeSerializableKey(in, false);
    assertValidKeyWithDumpObject(in, out);
  }

  private void assertValidKey(Object in, Object out) {
    final String input = "Input:  " + (in != null ? in.getClass() : null) + " " + VerboseUtility.dumpObject(in);
    final String output = "Output: " + (out != null ? out.getClass() : null) + " " + VerboseUtility.dumpObject(out);
    if (LOG.isDebugEnabled()) {
      LOG.debug(input);
      LOG.debug(output);
    }
    else {
      System.out.println(input);
      System.out.println(output);
    }

    assertTrue(CompareUtility.equals(in, out));
    if (in != null && out != null) {
      assertTrue(in.getClass().equals(out.getClass()));
    }
  }

  private void assertValidKeyWithDumpObject(Object in, Object out) {
    final String input = "Input:  " + (in != null ? in.getClass() : null) + " " + VerboseUtility.dumpObject(in);
    final String output = "Output: " + (out != null ? out.getClass() : null) + " " + VerboseUtility.dumpObject(out);
    if (LOG.isDebugEnabled()) {
      LOG.debug(input);
      LOG.debug(output);
    }
    else {
      System.out.println(input);
      System.out.println(output);
    }

    assertTrue(CompareUtility.equals(VerboseUtility.dumpObject(in), VerboseUtility.dumpObject(out)));
  }

  private static class SerializablePrimaryKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long m_id;

    public SerializablePrimaryKey(long id) {
      m_id = id;
    }
  }

  private static class UnSerializablePrimaryKey {

    private final long m_id;

    public UnSerializablePrimaryKey(long id) {
      m_id = id;
    }
  }

  private static class UnSerializablePrimaryKeyWithToString {

    private final long m_id;

    public UnSerializablePrimaryKeyWithToString(long id) {
      m_id = id;
    }

    @Override
    public String toString() {
      return "UnSerializablePrimaryKeyWithToString [m_id=" + m_id + "]";
    }
  }
}
