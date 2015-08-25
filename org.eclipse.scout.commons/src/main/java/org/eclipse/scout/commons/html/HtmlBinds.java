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
import java.util.Collections;
import java.util.Comparator;
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
  private final String m_prefix = ":b__";

  /**
   * sequence for bind names
   */
  private long m_sequenceId = 0L;

  public IHtmlBind putString(String value) {
    String qualifiedName = nextBindName();
    m_bindMap.put(qualifiedName, value);
    return new HtmlBind(qualifiedName);
  }

  /**
   * Generates a unique bind variable for a given value and puts key and value into the bind map.<br>
   * Puts a value with a generated key into the map.
   *
   * @return {@link IHtmlBind} key
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
    Map<String, Object> bindMap = binds.getBindMap();
    ArrayList<String> keys = new ArrayList<>(bindMap.keySet());
    Collections.sort(keys, new BindIdComparator());
    for (String existingBind : keys) {
      if (m_bindMap.containsKey(existingBind)) {
        String newBind = nextBindName();
        m_bindMap.put(newBind, bindMap.get(existingBind));
        replaceMap.put(existingBind, newBind);
      }
    }
    return replaceMap;
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

  class BindIdComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
      return (int) (Long.parseLong(o1.substring(getPrefix().length())) - Long.parseLong(o2.substring(getPrefix().length())));
    }

  }

}
