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
package org.eclipse.scout.rt.ui.html.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class CustomHtmlRenderer implements ICustomHtmlRenderer {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CustomHtmlRenderer.class);

  //non-capturing pattern!
  private static final Pattern HTML_PATTERN = Pattern.compile("\\s*<html.*</html>\\s*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern HTML_SPLIT_PATTERN = Pattern.compile("((?<=[>])|(?=[<]))", Pattern.CASE_INSENSITIVE);
  private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<(/)?(\\w+)(\\s+[^>]+)?(/)?>", Pattern.CASE_INSENSITIVE);//1=prefix, 2=name, 3=attributes, 4=suffix
  private static final Pattern HTML_ATTRIBUTE_PATTERN = Pattern.compile("([-\\w]+)\\s*=\\s*(?:\"([^\"]+)\"|'([^']+)')", Pattern.CASE_INSENSITIVE);//1=prefix, 2=name, 3=attributes, 4=suffix

  @Override
  public boolean isHtml(String text) {
    if (text != null) {
      return HTML_PATTERN.matcher(text).matches();
    }
    return false;
  }

  @Override
  public String convert(String customTextOrHtml, boolean allowHtmlTags) {
    if (customTextOrHtml == null) {
      return null;
    }
    if (allowHtmlTags && isHtml(customTextOrHtml)) {
      return filterHtml(customTextOrHtml);
    }
    return escapeHtml(customTextOrHtml);
  }

  protected String escapeHtml(String text) {
    return text.replace("<", "&lt;").replace(">", "&gt;");
  }

  protected String filterHtml(String pseudoHtml) {
    List<Object> tokens = tokenize(HTML_SPLIT_PATTERN.split(pseudoHtml));
    process(tokens);
    String result = CollectionUtility.format(tokens, "");
    // Replace multiple linebreaks by a single line break, remove leading and trailing newlines
    result = result.replaceAll("(?s)\\s*(\\n\\s*?)", "$1").replaceAll("^\\n*|\\n*$", "");
    return result;
  }

  /**
   * @return list with Strings and {@link HtmlTag}s
   */
  protected List<Object> tokenize(String[] parts) {
    List<Object> list = new ArrayList<Object>(parts.length);
    for (String s : parts) {
      HtmlTag tag = HtmlTag.parse(s);
      list.add(tag != null ? tag : s);
    }
    return list;
  }

  protected void process(List<Object> tokens) {
    Set<String> blacklistContext = new HashSet<String>();
    for (int i = 0; i < tokens.size(); i++) {
      Object token = tokens.get(i);
      if (token instanceof HtmlTag) {
        HtmlTag tag = (HtmlTag) tokens.get(i);

        if (blacklistContext.isEmpty()) {
          // --- Whitelist ---
          if ("a".equals(tag.getName())) {
            //convert to span element
            tag.filterAttributes("class", "href");
            tag.setName("span");
            tag.attributes.put("class", "hyperlink");
            if (tag.attributes.containsKey("href")) {
              tag.attributes.put("data-hyperlink", tag.attributes.remove("href"));
            }
            continue;
          }
          if ("div".equals(tag.getName())) {
            tag.filterAttributes("class");
            continue;
          }
          if ("b".equals(tag.getName())) {
            tag.filterAttributes("class");
            continue;
          }
          if ("i".equals(tag.getName())) {
            tag.filterAttributes("class");
            continue;
          }
          if ("p".equals(tag.getName())) {
            tag.filterAttributes("class");
            continue;
          }
          if ("br".equals(tag.getName())) {
            tag.filterAttributes("class");
            continue;
          }
          if ("span".equals(tag.getName())) {
            tag.filterAttributes("class", "data-hyperlink");
            continue;
          }
          if ("table".equals(tag.getName())) {
            tag.filterAttributes("class", "border", "width");
            continue;
          }
          if ("tr".equals(tag.getName())) {
            tag.filterAttributes("class", "width", "align");
            continue;
          }
          if ("td".equals(tag.getName())) {
            tag.filterAttributes("class", "width", "align", "colspan", "valign");
            continue;
          }
          if ("small".equals(tag.getName())) {
            tag.filterAttributes("class");
            continue;
          }
          if ("font".equals(tag.getName())) {
            tag.filterAttributes("class", "color");
            continue;
          }
          if (CompareUtility.isOneOf(tag.getName(), "h1", "h2", "h3", "h4", "h5")) {
            tag.filterAttributes("class");
            continue;
          }
        }

        // --- Blacklist ---
        updateBlacklistContext(blacklistContext, "script", tag);
        updateBlacklistContext(blacklistContext, "style", tag);
        updateBlacklistContext(blacklistContext, "iframe", tag);
      }
      // Text without tags
      else if (token instanceof String) {
        if (blacklistContext.isEmpty()) {
          continue;
        }
      }
      // Unknown token type
      else {
        LOG.warn("Unknown token type: " + token);
      }

      // If we get here, the current token is not valid. Therefore, its content is cleared.
      tokens.set(i, "");
    }
  }

  protected void updateBlacklistContext(Set<String> blacklistContext, String tagName, HtmlTag tag) {
    if (tagName.equals(tag.getName())) {
      if (tag.isEndTag()) {
        blacklistContext.remove(tagName);
      }
      else {
        blacklistContext.add(tagName);
      }
    }
  }

  protected static class HtmlTag {

    private String m_prefix;
    private String m_suffix;
    private String m_name;
    public Map<String, String> attributes = new LinkedHashMap<String, String>();

    public static HtmlTag parse(String token) {
      Matcher m = HTML_TAG_PATTERN.matcher(token);
      if (!m.matches()) {
        return null;
      }
      HtmlTag tag = new HtmlTag();
      tag.m_prefix = m.group(1);
      tag.m_name = m.group(2);
      if (m.group(3) != null) {
        Matcher a = HTML_ATTRIBUTE_PATTERN.matcher(m.group(3));
        while (a.find()) {
          tag.attributes.put(a.group(1).toLowerCase(), a.group(2) != null ? a.group(2) : a.group(3));
        }
      }
      tag.m_suffix = m.group(4);
      return tag;
    }

    private HtmlTag() {
    }

    public String getName() {
      return m_name;
    }

    public void setName(String name) {
      m_name = name;
    }

    public String getPrefix() {
      return m_prefix;
    }

    public String getSuffix() {
      return m_suffix;
    }

    public boolean isEndTag() {
      return "/".equals(m_prefix) || "/".equals(m_suffix);
    }

    public void filterAttributes(String... validAttributeNames) {
      Map<String, String> tmp = attributes;
      attributes = new LinkedHashMap<String, String>();
      for (String key : validAttributeNames) {
        if (tmp.containsKey(key)) {
          attributes.put(key, tmp.get(key));
        }
      }
    }

    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append("<");
      if (m_prefix != null) {
        buf.append(m_prefix);
      }
      buf.append(m_name);
      for (Map.Entry<String, String> e : attributes.entrySet()) {
        buf.append(" ");
        buf.append(e.getKey());
        buf.append("=");
        buf.append("\"");
        buf.append(e.getValue().replace("\"", "'"));
        buf.append("\"");
      }
      if (m_suffix != null) {
        buf.append(m_suffix);
      }
      buf.append(">");
      return buf.toString();
    }
  }
}
