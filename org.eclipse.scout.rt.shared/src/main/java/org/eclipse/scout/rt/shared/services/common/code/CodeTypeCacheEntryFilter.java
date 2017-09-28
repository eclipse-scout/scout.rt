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
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.cache.ICacheEntryFilter;

/**
 * Filter to match according code type classes.
 * <p>
 * This class is immutable.
 * 
 * @since 5.2
 */
public class CodeTypeCacheEntryFilter implements ICacheEntryFilter<CodeTypeCacheKey, ICodeType<?, ?>> {
  private static final long serialVersionUID = 1L;
  private final Set<Class<? extends ICodeType<?, ?>>> m_codeTypeClasses;

  public CodeTypeCacheEntryFilter(Class<? extends ICodeType<?, ?>> codeTypeClass) {
    m_codeTypeClasses = CollectionUtility.hashSet(codeTypeClass);
  }

  public CodeTypeCacheEntryFilter(Collection<Class<? extends ICodeType<?, ?>>> codeTypeClasses) {
    m_codeTypeClasses = CollectionUtility.hashSetWithoutNullElements(codeTypeClasses);
  }

  public Set<Class<? extends ICodeType<?, ?>>> getCodeTypeClasses() {
    return Collections.unmodifiableSet(m_codeTypeClasses);
  }

  @Override
  public boolean accept(CodeTypeCacheKey key, ICodeType<?, ?> value) {
    return m_codeTypeClasses.contains(key.getCodeTypeClass());
  }

  @Override
  public ICacheEntryFilter<CodeTypeCacheKey, ICodeType<?, ?>> coalesce(ICacheEntryFilter<CodeTypeCacheKey, ICodeType<?, ?>> other) {
    if (other instanceof CodeTypeCacheEntryFilter) {
      HashSet<Class<? extends ICodeType<?, ?>>> newSet = new HashSet<>(m_codeTypeClasses);
      newSet.addAll(((CodeTypeCacheEntryFilter) other).m_codeTypeClasses);
      return new CodeTypeCacheEntryFilter(newSet);
    }
    return null;
  }
}
