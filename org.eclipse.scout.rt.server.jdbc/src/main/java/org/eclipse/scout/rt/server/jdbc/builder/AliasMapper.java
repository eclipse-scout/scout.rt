/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jdbc.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * Alias management for form data statement builder
 */
public class AliasMapper {
  private static final Pattern CLEAN_ENTITY_NAME = Pattern.compile("[@]?(parent\\.)?([a-zA-Z0-9_]+)[@]?");
  private static final Pattern ENTITY_NAME = Pattern.compile("[@](parent\\.)?([a-zA-Z0-9_]+)[@]");

  /**
   * Pattern to recognize entity definitions. The pattern does not recognize an entity definition in case the string is
   * terminated after the ending @. Thus when using the entity definition, append a space to the string before applying
   * the pattern.
   */
  private static final Pattern ENTITY_DEFINITION = Pattern.compile("[@]([a-zA-Z0-9_]+)[@]([^.])");

  private AtomicInteger m_sequenceProvider;
  private final Map<String, String> m_rootAliases;
  private final Map<Object, Map<String, String>> m_nodeAliases;

  public AliasMapper() {
    m_rootAliases = new HashMap<>();
    m_nodeAliases = new HashMap<>();
    m_sequenceProvider = new AtomicInteger(0);
  }

  /**
   * use another sequence provider (counts 0,1,2... for aliases)
   */
  public void setSequenceProvider(AtomicInteger sequenceProvider) {
    m_sequenceProvider = sequenceProvider;
  }

  /**
   * cleans an entity name
   * <ul>
   * <li>@PERSON@ =&gt; PERSON</li>
   * <li>@person@ =&gt; PERSON</li>
   * <li>@parent.PERSON@ =&gt; PERSON</li>
   * <li>Person =&gt; PERSON</li>
   * </ul>
   */
  public static String cleanEntityName(String e) {
    if (e == null) {
      return e;
    }
    Matcher m = CLEAN_ENTITY_NAME.matcher(e);
    if (m.matches()) {
      return m.group(2).toUpperCase();
    }
    else {
      return e.toUpperCase();
    }
  }

  /**
   * generates next unique alias name such as p1, p2, p3 etc.
   */
  public String nextAlias() {
    int seq = m_sequenceProvider.incrementAndGet();
    return "a" + (("" + (10000 + seq)).substring(1));
  }

  /**
   * @return the live map with the effective aliases per entity for example {PERSON = p, TASK = t} never returns null
   */
  public Map<String, String> getRootAliases() {
    return m_rootAliases;
  }

  /**
   * @param entityName
   *          with or without the @ delimiters (lenient), for example
   *          <ul>
   *          <li>PERSON</li>
   *          <li>TASK</li>
   *          <li>@PERSON@</li>
   *          <li>@TASK@</li>
   *          </ul>
   * @return the alias for an entity, for example p, t etc.
   */
  public String getRootAlias(String entityName) {
    return m_rootAliases.get(cleanEntityName(entityName));
  }

  /**
   * @param entityName
   *          with or without the @ delimiters (lenient), for example
   *          <ul>
   *          <li>PERSON</li>
   *          <li>TASK</li>
   *          <li>@PERSON@</li>
   *          <li>@TASK@</li>
   *          </ul>
   * @param alias
   *          for example p, t etc.
   */
  public void setRootAlias(String entityName, String alias) {
    m_rootAliases.put(cleanEntityName(entityName), alias);
  }

  /**
   * @return the live map with the effective aliases per entity for example {PERSON = p, TASK = t} never returns null
   */
  public Map<String, String> getNodeAliases(Object node) {
    Map<String, String> map = m_nodeAliases.computeIfAbsent(node, k -> new HashMap<>());
    return map;
  }

  /**
   * @param entityName
   *          with or without the @ delimiters (lenient), for example
   *          <ul>
   *          <li>PERSON</li>
   *          <li>TASK</li>
   *          <li>@PERSON@</li>
   *          <li>@TASK@</li>
   *          </ul>
   * @return the alias for an entity, for example p, t etc.
   */
  public String getNodeAlias(Object node, String entityName) {
    Map<String, String> map = m_nodeAliases.get(node);
    if (map != null) {
      return map.get(cleanEntityName(entityName));
    }
    else {
      return null;
    }
  }

  /**
   * @param entityName
   *          with or without the @ delimiters (lenient), for example
   *          <ul>
   *          <li>PERSON</li>
   *          <li>TASK</li>
   *          <li>@PERSON@</li>
   *          <li>@TASK@</li>
   *          </ul>
   * @param alias
   *          for example p, t etc.
   */
  public void setNodeAlias(Object node, String entityName, String alias) {
    Map<String, String> map = m_nodeAliases.computeIfAbsent(node, k -> new HashMap<>());
    map.put(cleanEntityName(entityName), alias);
  }

  /**
   * Parse all entity definitions in the statement part and define a new (local) alias that is associate with the node.
   */
  public void addAllNodeEntitiesFrom(Object node, String statementPart) {
    Matcher m = ENTITY_DEFINITION.matcher(statementPart + " ");
    while (m.find()) {
      setNodeAlias(node, m.group(1), nextAlias());
    }
  }

  public void addMissingNodeEntitiesFrom(Object node, String statementPart) {
    Matcher m = ENTITY_DEFINITION.matcher(statementPart + " ");
    while (m.find()) {
      if (getNodeAlias(node, m.group(1)) == null) {
        setNodeAlias(node, m.group(1), nextAlias());
      }
    }
  }

  /**
   * Parse all entity definitions in the statement part and define in the root map if there is not yet a definition for
   * it.
   */
  public void addMissingRootEntitiesFrom(String statementPart) {
    Matcher m = ENTITY_DEFINITION.matcher(statementPart + " ");
    while (m.find()) {
      if (getRootAlias(m.group(1)) == null) {
        setRootAlias(m.group(1), nextAlias());
      }
    }
  }

  /**
   * Resolve and replace all entity definitions and references inthe statement part.
   * <p>
   * Example entity: <code>SELECT 1 FROM Person @Person@ where @Person@.personNr=@parent.Person@.personNr</code> -&gt;
   * <code>SELECT 1 FROM Person a002 where a002.personNr=a001.personNr</code>
   * <p>
   * Example attribute: <code>lastName</code> -&gt; <code>lastName</code>
   * <p>
   * Example attribute: <code>@Person@.lastName</code> -&gt; <code>a002.lastName</code>
   * <p>
   * Example attribute: <code>@parent.Person@.lastName</code> -&gt; <code>a002.lastName</code>
   * <p>
   * <b>Note that on attributes @parent.Person@ and @Person@ is the same and is both supported since an attribute is
   * attached to an entity. Therefore both declarations are equally "correct" and mean the same entitie's alias.
   */
  public String replaceMarkersByAliases(String statementPart, Map<String, String> aliasMap, Map<String, String> parentAliasMap) {
    String s = statementPart;
    Matcher m = ENTITY_NAME.matcher(s);
    while (m.find()) {
      boolean parent = m.group(1) != null && !m.group(1).isEmpty();
      String name = cleanEntityName(m.group(2));
      String replacement = null;
      if (parent) {
        replacement = parentAliasMap.get(name);
      }
      else {
        replacement = aliasMap.get(name);
      }
      if (replacement == null) {
        throw new ProcessingException("missing alias '" + name + "' for entity '" + m.group() + "' in statement part: " + statementPart + "; map=" + aliasMap + " parentMap=" + parentAliasMap);
      }
      s = s.substring(0, m.start()) + replacement + s.substring(m.end());
      //next
      m = ENTITY_NAME.matcher(s);
    }
    return s;
  }

}
