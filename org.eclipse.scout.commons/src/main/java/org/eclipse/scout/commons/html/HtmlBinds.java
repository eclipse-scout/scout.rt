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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.html.internal.HtmlBind;

/**
 * HTML Binds <br>
 */
public class HtmlBinds {

  /**
   * Internal map to store bind variables.
   */
  private final Map<String, Object> m_bindMap = new HashMap<>();

  /**
   * Prefix for bind variable name.
   */
  private final String m_prefix;

  /**
   * sequence for bind names
   */
  private long m_sequenceId = 0L;

  /**
   * Create binds map.
   */
  public HtmlBinds() {
    m_prefix = ":b__";
  }

  /**
   * Create binds map.
   */
  public HtmlBinds(String prefix) {
    m_prefix = prefix;
  }

  public IHtmlBind putString(String value) {
    String qualifiedName = nextBindName();
    m_bindMap.put(qualifiedName, value);
    return new HtmlBind(qualifiedName);
  }

  /**
   * Puts a value with a generated key into the map.
   *
   * @param value
   * @return the key of the new bind
   */
  public IHtmlBind put(Object value) {
    return putString(StringUtility.nvl(value, ""));
  }

  /**
   * @return a new bind name as <code>bindPrefix + sequenceId</code> for example <code>:b__0</code>
   */
  protected String nextBindName() {
    String name = getBindName(m_sequenceId);
    m_sequenceId++;
    return name;
  }

  /**
   * @return the bind name given a sequenceNr
   */
  private String getBindName(long sequenceNr) {
    return m_prefix + sequenceNr;
  }

  /**
   * @return value for a given bind key.
   */
  public Object getBindValue(String key) {
    return m_bindMap.get(key);
  }

  /**
   * @return prefix of all binds
   */
  protected String getPrefix() {
    return m_prefix;
  }

  public Map<String, Object> getBindMap() {
    return CollectionUtility.copyMap(m_bindMap);
  }

  public void putAll(HtmlBinds binds) {
    for (Entry<String, Object> entry : binds.getBindMap().entrySet()) {
      String key = entry.getKey();
      long seq = getSequenceId(key);
      if (!m_bindMap.containsKey(key)) {
        m_bindMap.put(key, entry.getValue());
        if (m_sequenceId <= seq) {
          m_sequenceId = seq + 1;
        }
      }
    }
  }

  private long getSequenceId(String key) {
    String seq = key.substring(m_prefix.length());
    return Long.parseLong(seq);
  }

  public Map<String, String> getReplacements(HtmlBinds binds) {
    Map<String, String> replaceMap = new HashMap<>();
    for (Entry<String, Object> bindEntry : binds.getBindMap().entrySet()) {
      String existingBind = bindEntry.getKey();
      if (m_bindMap.containsKey(existingBind)) {
        String newBind = nextBindName();
        m_bindMap.put(newBind, bindEntry.getValue());
        replaceMap.put(existingBind, newBind);
      }
    }
    return replaceMap;
  }

  /**
   * Replace bind names with encoded values.
   *
   * @deprecated use {@link IHtmlElement#toEncodedHtml()} instead
   */
  @Deprecated
  public String applyBindParameters(IHtmlContent... htmls) {
    return applyBindParameters(Arrays.asList(htmls));
  }

  /**
   * Replace bind names with encoded values.
   *
   * @deprecated use {@link IHtmlElement#toEncodedHtml()} instead
   */
  @Deprecated
  public String applyBindParameters(List<? extends IHtmlContent> htmls) {
    StringBuilder sb = new StringBuilder();
    for (IHtmlContent html : htmls) {
      html.setBinds(this);
      sb.append(html.toEncodedHtml());
    }
    return sb.toString();
  }

  /**
   * @return all bind parameters (keys) in the given String
   */
  public List<String> getBindParameters(CharSequence s) {
    List<String> binds = new ArrayList<String>();
    Pattern p = Pattern.compile(getPrefix() + "(\\d+)", Pattern.MULTILINE);
    Matcher m = p.matcher(s);
    while (m.find()) {
      binds.add(m.group(0));
    }
    return binds;
  }

  @Override
  public String toString() {
    return "HtmlBinds [m_bindMap=" + m_bindMap + "]";
  }

  /**
   * @param bindMap
   */
  public void replaceBinds(Map<String, String> bindMap) {
    for (Entry<String, String> entry : bindMap.entrySet()) {
      m_bindMap.put(entry.getValue(), m_bindMap.get(entry.getKey()));
      m_bindMap.remove(entry.getKey());
    }

  }

}
