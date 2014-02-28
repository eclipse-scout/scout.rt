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
package org.eclipse.scout.http.servletfilter;

import javax.servlet.Filter;

/**
 * @deprecated use org.eclipse.scout.rt.server.commons.servletfilter.ServletFilterDelegate instead. Will be removed in
 *             the M-Release.
 */
@Deprecated
public class FilterConfigInjection extends org.eclipse.scout.rt.server.commons.servletfilter.FilterConfigInjection {

  public FilterConfigInjection(javax.servlet.FilterConfig config, Class<? extends Filter> filterType) {
    super(config, filterType);
  }
}
