/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.bookmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkVisitor;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

@ClassId("786fb2d7-5d48-4239-b267-bec20cca8a88")
public class KeyStrokeLookupCall extends LocalLookupCall<String> {
  private static final long serialVersionUID = 1L;

  private String m_currentKeyStroke;

  public String getCurrentKeyStroke() {
    return m_currentKeyStroke;
  }

  public void setCurrentKeyStroke(String s) {
    m_currentKeyStroke = s;
  }

  @Override
  protected List<ILookupRow<String>> execCreateLookupRows() {
    final HashMap<String, Integer> keyStrokesUpper = new HashMap<String, Integer>();
    //build possible keyStrokes
    for (int i = 1; i <= 12; i++) {
      keyStrokesUpper.put("SHIFT-F" + i, Integer.valueOf(i));
    }
    //remove used keyStrokes
    IBookmarkService service = BEANS.get(IBookmarkService.class);
    IBookmarkVisitor v = new IBookmarkVisitor() {
      @Override
      public boolean visitFolder(List<BookmarkFolder> path) {
        return true;
      }

      @Override
      public boolean visitBookmark(List<BookmarkFolder> path, Bookmark b) {
        String keyStroke = b.getKeyStroke();
        if (keyStroke != null) {
          if (m_currentKeyStroke != null && m_currentKeyStroke.equalsIgnoreCase(keyStroke)) {
            //keep it
          }
          else {
            keyStrokesUpper.remove(keyStroke.toUpperCase(Locale.ENGLISH));
          }
        }
        return true;
      }
    };
    service.getBookmarkData().getGlobalBookmarks().visit(v);
    service.getBookmarkData().getUserBookmarks().visit(v);
    //
    TreeSet<Integer> availableNumbers = new TreeSet<Integer>(keyStrokesUpper.values());
    ArrayList<ILookupRow<String>> resultList = new ArrayList<ILookupRow<String>>();
    for (Integer i : availableNumbers) {
      String key = "Shift-F" + i;
      String text = "Shift-F" + i;
      ILookupRow<String> row = new LookupRow<String>(key, text);
      resultList.add(row);
    }
    return resultList;
  }
}
