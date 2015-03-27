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
package org.eclipse.scout.commons.html.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.html.HtmlBind;
import org.eclipse.scout.commons.html.IHtmlContent;

/**
 * Contains bind variables (bindName, value) <br>
 * not thread safe
 */
public class AbstractBinds {

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
  public AbstractBinds() {
    m_prefix = ":b__";
  }

  /**
   * Create binds map.
   */
  public AbstractBinds(String prefix) {
    m_prefix = prefix;
  }

  public IHtmlContent putString(String value) {
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
  public IHtmlContent put(Object value) {
    return putString(StringUtility.nvl(value, ""));
  }

  /**
   * @return a new bind name as <code>bindPrefix + sequenceId</code> for example <code>:b__0</code>
   */
  private String nextBindName() {
    long nextId = m_sequenceId++;
    String name = getBindName(nextId);
    return name;
  }

  /**
   * @return the bind name given a sequenceNr
   */
  private String getBindName(long sequenceNr) {
    return m_prefix + sequenceNr;
  }

  /**
   * @return a copy containing the binds
   */
  public Map<String, Object> getBinds() {
    return CollectionUtility.copyMap(m_bindMap);
  }

}
