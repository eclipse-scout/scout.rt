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
package org.eclipse.scout.rt.client.services.common.bookmark.internal;

import java.io.Serializable;

import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

public final class TestBookmarkUtility {

  private TestBookmarkUtility() {
  }

  public static void main(String[] args) {
    test(null);
    test(new String[0]);
    test(new String[1]);
    test(new String[1][0]);
    test(new String[0][1]);
    test(new String[][]{new String[]{"aaa", "bbb"}, new String[]{"ccc",}});
    test(new int[]{3, 2, 1});
    test(new Bookmark[]{new Bookmark()});
    test(new SerializablePrimaryKey(1234L));
    test(new UnSerializablePrimaryKey(1234L));
    test(new UnSerializablePrimaryKeyWithToString(1234L));
    test(new SerializablePrimaryKey[]{new SerializablePrimaryKey(1234L)});
    test(new UnSerializablePrimaryKey[]{new UnSerializablePrimaryKey(1234L)});
    test(new UnSerializablePrimaryKeyWithToString[]{new UnSerializablePrimaryKeyWithToString(1234L)});
  }

  public static void test(Object a) {
    Object b = BookmarkUtility.makeSerializableKey(a, false);
    System.out.println("Input:  " + (a != null ? a.getClass() : null) + " " + VerboseUtility.dumpObject(a));
    System.out.println("Output: " + (b != null ? b.getClass() : null) + " " + VerboseUtility.dumpObject(b));
    System.out.println();
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
