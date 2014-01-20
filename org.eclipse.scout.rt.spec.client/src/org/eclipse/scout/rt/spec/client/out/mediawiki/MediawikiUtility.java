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
package org.eclipse.scout.rt.spec.client.out.mediawiki;

/**
 * Utilitis for mediawiki
 */
public final class MediawikiUtility {
  private static final String[] ESCAPE_LIST = new String[]{"[", "]"};

  private MediawikiUtility() {
  }

  /**
   * Escape characters that are interpreted by mediawiki
   * 
   * @param text
   * @return
   */
  public static String transformToWiki(Object text) {
    if (text == null) {
      return "";
    }
    return escapeWiki(text.toString());
  }

  private static String escapeWiki(String input) {
    String res = input;
    for (String s : ESCAPE_LIST) {
      res = res.replace(s, "<nowiki>" + s + "</nowiki>");
    }
    return res;
  }
}
