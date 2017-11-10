/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.data.query;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;

public abstract class AbstractDataQueryWithInclude<I extends ResourceInclude> extends AbstractDataQuery implements IDataQueryWithInclude<I> {

  public static final String PROP_INCLUDE = "include";
  private static final long serialVersionUID = 1L;

  private I m_include;

  @Override
  public void setInclude(I include) {
    m_include = include;
  }

  @Override
  public I getInclude() {
    return m_include;
  }

  @Override
  public QueryBuilder addQueryParams(QueryBuilder builder) {
    return super.addQueryParams(builder)
        .queryParam(PROP_INCLUDE, getInclude().format());
  }

  @Override
  protected void interceptToStringBuilder(ToStringBuilder builder) {
    super.interceptToStringBuilder(builder);
    builder.attr(PROP_INCLUDE, getInclude());
  }

}
