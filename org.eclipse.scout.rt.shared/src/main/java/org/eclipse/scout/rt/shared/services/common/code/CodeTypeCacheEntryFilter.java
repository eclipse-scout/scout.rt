/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

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

  @Override
  public String toString() {
    return "CodeTypeCacheEntryFilter [m_codeTypeClasses=" + m_codeTypeClasses + ']';
  }
}
