/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import java.io.Serializable;
import java.util.Locale;

import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * Key class used for the cache in the {@link ICodeService} implementation.
 *
 * @since 5.2
 */
public class CodeTypeCacheKey implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Locale m_locale;
  private final Class<? extends ICodeType<?, ?>> m_codeTypeClass;

  public CodeTypeCacheKey(Class<? extends ICodeType<?, ?>> codeTypeClass) {
    this(NlsLocale.get(), codeTypeClass);
  }

  public CodeTypeCacheKey(Locale locale, Class<? extends ICodeType<?, ?>> codeTypeClass) {
    super();
    m_locale = locale;
    m_codeTypeClass = codeTypeClass;
  }

  public Locale getLocale() {
    return m_locale;
  }

  public Class<? extends ICodeType<?, ?>> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("locale", getLocale());
    builder.attr("codeTypeClass", getCodeTypeClass());

    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_codeTypeClass == null) ? 0 : m_codeTypeClass.hashCode());
    result = prime * result + ((m_locale == null) ? 0 : m_locale.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CodeTypeCacheKey other = (CodeTypeCacheKey) obj;
    if (m_codeTypeClass == null) {
      if (other.m_codeTypeClass != null) {
        return false;
      }
    }
    else if (!m_codeTypeClass.equals(other.m_codeTypeClass)) {
      return false;
    }
    if (m_locale == null) {
      if (other.m_locale != null) {
        return false;
      }
    }
    else if (!m_locale.equals(other.m_locale)) {
      return false;
    }
    return true;
  }
}
