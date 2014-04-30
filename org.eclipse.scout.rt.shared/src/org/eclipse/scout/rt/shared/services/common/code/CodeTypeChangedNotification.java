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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.clientnotification.AbstractClientNotification;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

/**
 * Notification is sent from server to client to notify that the code type has
 * changed and the client should clear its cache
 */
public class CodeTypeChangedNotification extends AbstractClientNotification {
  private static final long serialVersionUID = 1L;
  private List<Class<? extends ICodeType<?, ?>>> m_codeTypes;

  public CodeTypeChangedNotification(List<Class<? extends ICodeType<?, ?>>> types) throws ProcessingException {
    for (Class<? extends ICodeType<?, ?>> codeTypeClazz : types) {
      if (codeTypeClazz != null && codeTypeClazz.isAssignableFrom(Serializable.class)) {
        throw new ProcessingException("Code type '" + codeTypeClazz.getName() + "' is not serializable!");
      }
    }
    m_codeTypes = CollectionUtility.arrayList(types);
  }

  @Override
  public boolean coalesce(IClientNotification existingNotification) {
    CodeTypeChangedNotification n = (CodeTypeChangedNotification) existingNotification;
    Set<Class<? extends ICodeType<?, ?>>> set = new HashSet<Class<? extends ICodeType<?, ?>>>();
    set.addAll(this.m_codeTypes);
    set.addAll(n.m_codeTypes);
    m_codeTypes = new ArrayList<Class<? extends ICodeType<?, ?>>>(set);
    if (this.getOriginNode() != existingNotification.getOriginNode()) {
      this.setOriginNode(0);
    }
    return true;
  }

  public List<Class<? extends ICodeType<?, ?>>> getCodeTypes() {
    return CollectionUtility.arrayList(m_codeTypes);
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
