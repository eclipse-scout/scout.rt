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

import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;

/**
 * Utilities for mediawiki
 */
public final class MediawikiUtility {
  private static final String[] ESCAPE_LIST = new String[]{"[", "]", "=", "*", "----", "#", "{", "--~"};

  private MediawikiUtility() {
  }

  /**
   * Escape character sequences that are interpreted by mediawiki
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

  /**
   * Create a mediawiki link
   * 
   * @param id
   * @param name
   * @return
   */
  public static String createLink(String id, String name) {
    StringBuilder sb = new StringBuilder();
    sb.append("[[").append(id).append("|").append(name).append("]]");
    return sb.toString();
  }

  /**
   * create an anchor
   * 
   * @param id
   * @return
   */
  public static String createAnchor(String id) {
    return "{{a:" + id + "}}";
  }

  /**
   * Creates a new String with all anchors removed and all links replaced by their display name.
   * 
   * @param input
   * @return
   */
  public static String removeAnchorsAndLinks(String input) {
    input = input.replaceAll("\\{\\{[^}]+}}", "");
    input = input.replaceAll("\\[\\[([A-Za-z][A-Za-z0-9_\\$\\.-]+)\\|(.*?)]]", "$2");
    return input;
  }

  /**
   * Creates a link to an image.
   * 
   * @param imagePath
   *          the path including filename of the image relative to the {@link SpecFileConfig#getMediawikiDir()}
   * @return
   */
  public static String createImageLink(String imagePath) {
    return "[[Image:" + imagePath + "]]";
  }

  /**
   * Creates a link to an image which will be scaled in the output.
   * 
   * @param imagePath
   *          the path including filename of the image relative to the {@link SpecFileConfig#getMediawikiDir()}
   * @param scale
   *          width in pixel
   * @return
   */
  public static String createImageLink(String imagePath, int scale) {
    return "[[Image:" + imagePath + "|" + scale + "px]]";
  }
}
