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
package org.eclipse.scout.rt.client.ui.desktop.bookmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkVisitor;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

public class KeyStrokeLookupCall extends LocalLookupCall {
  private static final long serialVersionUID = 1L;

  private String m_currentKeyStroke;

  public String getCurrentKeyStroke() {
    return m_currentKeyStroke;
  }

  public void setCurrentKeyStroke(String s) {
    m_currentKeyStroke = s;
  }

  @Override
  protected List<LookupRow> execCreateLookupRows() throws ProcessingException {
    final HashMap<String, Integer> keyStrokesUpper = new HashMap<String, Integer>();
    //build possible keyStrokes
    for (int i = 1; i <= 12; i++) {
      keyStrokesUpper.put("SHIFT-F" + i, new Integer(i));
    }
    //remove used keyStrokes
    IBookmarkService service = SERVICES.getService(IBookmarkService.class);
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
    ArrayList<LookupRow> resultList = new ArrayList<LookupRow>();
    for (Integer i : availableNumbers) {
      String key = "Shift-F" + i;
      String text = "Shift-F" + i;
      LookupRow row = new LookupRow(key, text);
      resultList.add(row);
    }
    return resultList;
  }
}
