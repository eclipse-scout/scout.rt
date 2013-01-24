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
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.scout.rt.shared.services.common.clientnotification.AbstractClientNotification;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

/**
 * Notification is sent from server to client to notify that the code type has
 * changed and the client should clear its cache
 */
public class CodeTypeChangedNotification extends AbstractClientNotification {
  private static final long serialVersionUID = 1L;
  private Class<? extends Serializable>[] m_codeTypes;

  public CodeTypeChangedNotification(Class<? extends Serializable>[] codeTypes) {
    m_codeTypes = codeTypes;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean coalesce(IClientNotification existingNotification) {
    CodeTypeChangedNotification n = (CodeTypeChangedNotification) existingNotification;
    HashSet<Class<? extends Serializable>> set = new HashSet<Class<? extends Serializable>>();
    set.addAll(Arrays.asList(this.m_codeTypes));
    set.addAll(Arrays.asList(n.m_codeTypes));
    m_codeTypes = set.toArray(new Class[set.size()]);
    if (this.getOriginNode() != existingNotification.getOriginNode()) {
      this.setOriginNode(0);
    }
    return true;
  }

  public Class<? extends Serializable>[] getCodeTypes() {
    return m_codeTypes;
  }

  @Override
  public String toString() {
    StringBuffer b = new StringBuffer(getClass().getSimpleName());
    b.append("[");
    for (int i = 0; i < m_codeTypes.length; i++) {
      if (i > 0) {
        b.append(", ");
      }
      b.append(m_codeTypes[i].getSimpleName());
    }
    b.append("]");
    return b.toString();
  }

}
