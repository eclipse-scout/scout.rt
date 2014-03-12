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
package org.eclipse.scout.rt.spec.client.gen.extract;

import org.eclipse.scout.commons.StringUtility;

/**
 * Wrapper for two {@link IDocTextExtractor IDocTextExtractors}
 * <p>
 * Both {@link #getHeader()} and {@link #getText(Object)} delegate to the corresponding methods of the primary extractor
 * and to the methods of the fallback extractor, in case the primary extractor returns null. (This happens
 * independently: it is possible that {@link #getHeader()} returns the value of the primary extractor but
 * {@link #getText(Object)} returns the value of the fallback extractor.)
 * 
 * @param <T>
 */
public class FallbackTextExtractor<T> implements IDocTextExtractor<T> {

  protected IDocTextExtractor<T> m_primaryExtractor;
  protected IDocTextExtractor<T> m_fallbackExtractor;

  public FallbackTextExtractor(IDocTextExtractor<T> primaryExtractor, IDocTextExtractor<T> fallbackExtractor) {
    m_primaryExtractor = primaryExtractor;
    m_fallbackExtractor = fallbackExtractor;
  }

  @Override
  public String getHeader() {
    return StringUtility.nvl(m_primaryExtractor.getHeader(), m_fallbackExtractor.getHeader());
  }

  @Override
  public String getText(T object) {
    return StringUtility.nvl(m_primaryExtractor.getText(object), m_fallbackExtractor.getText(object));
  }

}
