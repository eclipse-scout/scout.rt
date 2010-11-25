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
package org.eclipse.scout.rt.client;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;

public class ClientRule extends AbstractPropertyObserver implements ISchedulingRule {
  private final IClientSession m_session;
  private boolean m_enabled = true;

  public ClientRule(IClientSession session) {
    m_session = session;
  }

  public IClientSession getClientSession() {
    return m_session;
  }

  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  @Override
  public int hashCode() {
    return m_session.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this;
  }

  public boolean contains(ISchedulingRule rule) {
    if (this == rule) return true;
    //
    if (this.isEnabled() && rule instanceof ClientRule) {
      return (this.getClientSession() == ((ClientRule) rule).getClientSession());
    }
    //
    return false;
  }

  public boolean isConflicting(ISchedulingRule rule) {
    if (this == rule) return true;
    //
    if (this.isEnabled() && rule instanceof ClientRule) {
      return (this.getClientSession() == ((ClientRule) rule).getClientSession());
    }
    //
    return false;
  }

}
