/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.html.internal.AbstractBinds;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * HTML Binds <br>
 */
public class HtmlBinds extends AbstractBinds {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HtmlBinds.class);

  /**
   * Replace bind names with encoded values.
   */
  public String applyBindParameters(IHtmlElement... htmls) {
    return applyBindParameters(Arrays.asList(htmls));
  }

  /**
   * Replace bind names with encoded values.
   */
  public String applyBindParameters(List<? extends IHtmlElement> htmls) {
    StringBuilder sb = new StringBuilder();
    for (IHtmlElement html : htmls) {
      sb.append(applyBindParameters(html));
    }
    return sb.toString();
  }

  /**
   * Replace bind names with encoded values. Loggs an error, if no bind is found.
   */
  private String applyBindParameters(IHtmlElement html) {
    return applyBindParameters(html.toString());
  }

  protected String applyBindParameters(String html) {
    String res = html;
    List<String> binds = getBindParameters(res);
    Collections.sort(binds, new ReversedLengthComparator());
    for (String b : binds) {
      Object value = getBindValue(b);
      if (value == null) {
        LOG.error("No bind value found for ", b);
      }
      else {
        final String encode = encode(value);
        res = res.replaceAll(b, encode);
      }
    }
    return res;
  }

  /**
   * @return all bind parameters (keys) in the given String
   */
  protected List<String> getBindParameters(String s) {
    List<String> binds = new ArrayList<String>();
    Pattern p = Pattern.compile(getPrefix() + "(\\d+)", Pattern.MULTILINE);
    Matcher m = p.matcher(s);
    while (m.find()) {
      binds.add(m.group(0));
    }
    return binds;
  }

  /**
   * @return the encoded bind value.
   */
  protected String encode(Object value) {
    return StringUtility.htmlEncode(StringUtility.emptyIfNull(value).toString(), false);
  }

  private static class ReversedLengthComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
      return o2.length() - o1.length();
    }

  }

}
