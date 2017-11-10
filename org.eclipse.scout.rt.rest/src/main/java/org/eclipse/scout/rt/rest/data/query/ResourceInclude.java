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
package org.eclipse.scout.rt.rest.data.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ResourceInclude")
public class ResourceInclude extends Include {
  private static final long serialVersionUID = 1L;

  @JsonProperty
  private Map<String, Include> m_includes;
  @JsonProperty
  private Map<String, Class<? extends Include>> m_validIncludes;

  public ResourceInclude() {
    m_includes = new HashMap<>();
    m_validIncludes = new HashMap<>();
  }

  public void addValidInclude(String name, Class<? extends Include> includeType) {
    m_validIncludes.put(name, includeType);
  }

  public void addValidInclude(String name) {
    addValidInclude(name, Include.class);
  }

  public boolean isValidInclude(String name) {
    return m_validIncludes.containsKey(name);
  }

  public ResourceInclude include(String name, Include include) {
    if (!isValidInclude(name)) {
      throw new IllegalArgumentException("Include not valid");
    }
    m_includes.put(name, include);
    return this;
  }

  public ResourceInclude include(String name) {
    include(name, new Include());
    return this;
  }

  public boolean isIncluded(String field) {
    return m_includes.containsKey(field);
  }

  public Include getInclude(String field) {
    return m_includes.get(field);
  }

  public ResourceInclude parse(String includeParam) {
    List<String> includes = BEANS.get(QueryHelper.class).split(includeParam);
    for (String include : includes) {
      parseSingleInclude(include);
    }
    return this;
  }

  /**
   * Returns the current state of the Include as string (reverse operation of parse). returns an empty string if nothing
   * is included.
   */
  public String format() {
    if (m_includes.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder(); // FIXME [awe] implement more sophisticated (sub-)includes
    m_includes.entrySet().stream().forEach(entry -> {
      sb.append(entry.getKey()).append(",");
    });
    sb.deleteCharAt(sb.length() - 1); // delete last comma
    return sb.toString();
  }

  protected void parseSingleInclude(String includeParam) {
    String subInclude = null;
    String includeName = includeParam;
    int indexOfOpeningBracket = includeParam.indexOf('(');
    int indexOfClosingBracket = includeParam.indexOf(')');
    if (indexOfOpeningBracket > -1) {
      if (indexOfClosingBracket < 0) {
        throw new IllegalArgumentException("Missing closing bracket");
      }
      subInclude = includeParam.substring(indexOfOpeningBracket + 1, indexOfClosingBracket);
      includeName = includeParam.substring(0, indexOfOpeningBracket);
    }

    if (!isValidInclude(includeName)) {
      throw new IllegalArgumentException("Include " + includeName + " not valid");
    }

    Class<? extends Include> includeClass = m_validIncludes.get(includeName);
    Include include;
    try {
      include = includeClass.newInstance();
    }
    catch (InstantiationException | IllegalAccessException e) {
      throw new IllegalArgumentException("Could not instanciate include " + includeName, e);
    }
    include(includeName, include);
    if (include instanceof ResourceInclude) {
      ((ResourceInclude) include).parse(subInclude);
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("includes", m_includes);
    return builder.toString();
  }
}
