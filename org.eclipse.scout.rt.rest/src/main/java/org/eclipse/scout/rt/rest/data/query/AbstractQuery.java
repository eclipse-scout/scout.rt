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

import java.io.Serializable;

import org.eclipse.scout.rt.jackson.databind.JandexTypeNameIdResolver;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.rest.data.JsonConstants;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = JsonConstants.JSON_TYPE_PROPERTY)
@JsonTypeIdResolver(JandexTypeNameIdResolver.class)
public abstract class AbstractQuery implements Serializable {
  private static final long serialVersionUID = 1L;

  private String m_queryId;

  public String getQueryId() {
    return m_queryId;
  }

  public void setQueryId(String queryId) {
    m_queryId = queryId;
  }

  public void assignQueryId() {
    setQueryId(BEANS.get(QueryIdGenerator.class).generate());
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    interceptToStringBuilder(builder);
    return builder.toString();
  }

  /**
   * Allows the contribution of <code>toString</code> tokens.
   */
  protected void interceptToStringBuilder(ToStringBuilder builder) {
    builder
        .attr("queryId", m_queryId);
  }
}
