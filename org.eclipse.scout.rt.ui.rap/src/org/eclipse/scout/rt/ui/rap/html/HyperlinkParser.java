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
package org.eclipse.scout.rt.ui.rap.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;

/**
 * Simple hyperlink parser which considers &lt;a&gt; and &lt;area&gt; tags.
 */
public class HyperlinkParser {

  private static final Pattern LINK_PATTERN = Pattern.compile("((?:<a|<area)[ \r\n]+[^>]*href=)([\"'](?!javascript)[^>'\"]*['\"])([^>]*>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern TARGET_PATTERN = Pattern.compile("[ \r\n'\"]+target=['\"]([^\"']*)['\"]", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  public String parse(String html, IHyperlinkProcessor processor) {
    Pattern pattern = LINK_PATTERN;
    Matcher m = pattern.matcher(html);
    StringBuilder buf = new StringBuilder();
    int lastPos = 0;
    while (m.find()) {
      buf.append(html.substring(lastPos, m.start()));

      String url = html.substring(m.start(2) + 1, m.end(2) - 1);
      boolean local = url.contains(ITable.LOCAL_URL_PREFIX);
      String newLink = html.substring(m.start(1), m.end(1));
      newLink += "\"" + processor.processUrl(url, local) + "\"";
      newLink += html.substring(m.start(3), m.end(3));
      newLink = parseTarget(newLink, processor, local);

      buf.append(newLink);
      lastPos = m.end();
    }
    if (lastPos < html.length()) {
      buf.append(html.substring(lastPos));
    }
    return buf.toString();
  }

  protected String parseTarget(String link, IHyperlinkProcessor processor, boolean local) {
    Matcher targetMatcher = TARGET_PATTERN.matcher(link);
    String newLink = link;
    if (targetMatcher.find()) {
      String target = link.substring(targetMatcher.start(1), targetMatcher.end(1));
      String newTarget = processor.processTarget(target, local);
      if (newTarget != null) {
        newLink = link.substring(0, targetMatcher.start(1));
        newLink += newTarget;
        newLink += link.substring(targetMatcher.end(1), link.length());
      }
    }
    else {
      String newTarget = processor.processTarget(null, local);
      if (newTarget != null) {
        newLink = link.substring(0, link.length() - 1);
        newLink += " target=" + "\"" + newTarget + "\"";
        newLink += link.substring(link.length() - 1, link.length());
      }
    }
    return newLink;
  }
}
