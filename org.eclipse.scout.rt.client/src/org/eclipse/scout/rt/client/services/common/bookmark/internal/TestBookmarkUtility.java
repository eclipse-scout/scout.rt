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
  }

  public static void test(Object a) {
    Object b = BookmarkUtility.makeSerializableKey(a);
    System.out.println("Input:  " + (a != null ? a.getClass() : null) + " " + VerboseUtility.dumpObject(a));
    System.out.println("Output: " + (b != null ? b.getClass() : null) + " " + VerboseUtility.dumpObject(b));
  }

}
