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
package org.eclipse.scout.rt.shared.services.common.code;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notification is sent from server to client to notify that the code type has changed and the client should clear its
 * cache
 *
 * @deprecated replaced with {@link InvalidateCacheNotification}. Will be removed in Scout 6.1. See {@link ICache}
 */
@Deprecated
public class CodeTypeChangedNotification implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(CodeTypeChangedNotification.class);

  private Set<Class<? extends ICodeType<?, ?>>> m_codeTypes;

  public CodeTypeChangedNotification(Collection<Class<? extends ICodeType<?, ?>>> types) {
    Iterator<Class<? extends ICodeType<?, ?>>> codeTypeClassIt = types.iterator();
    while (codeTypeClassIt.hasNext()) {
      Class<? extends ICodeType<?, ?>> next = codeTypeClassIt.next();
      if (next != null && next.isAssignableFrom(Serializable.class)) {
        LOG.error("Code type '" + next.getName() + "' is not serializable!");
        codeTypeClassIt.remove();
      }
    }
    m_codeTypes = CollectionUtility.hashSet(types);
  }

  public Set<Class<? extends ICodeType<?, ?>>> getCodeTypes() {
    return CollectionUtility.hashSet(m_codeTypes);
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder(getClass().getSimpleName());
    b.append("[");
    if (CollectionUtility.hasElements(m_codeTypes)) {
      Iterator<Class<? extends ICodeType<?, ?>>> codeTypeIt = m_codeTypes.iterator();
      b.append(codeTypeIt.next().getSimpleName());
      while (codeTypeIt.hasNext()) {
        b.append(", ").append(codeTypeIt.next().getSimpleName());
      }
    }
    b.append("]");
    return b.toString();
  }

}
