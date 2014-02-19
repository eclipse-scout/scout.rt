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
package org.eclipse.scout.rt.spec.client.link;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.spec.client.out.IDocLink;

public class DocLink implements IDocLink {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DocLink.class);

  private final String m_targetId;
  private final String m_displayName;

  public DocLink(String targetId, String displayName) {
    m_targetId = targetId;
    m_displayName = displayName;
  }

  @Override
  public String getTargetId() {
    return m_targetId;
  }

  @Override
  public String getDisplayName() {
    return m_displayName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_displayName == null) ? 0 : m_displayName.hashCode());
    result = prime * result + ((m_targetId == null) ? 0 : m_targetId.hashCode());
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
    DocLink other = (DocLink) obj;
    if (m_displayName == null) {
      if (other.m_displayName != null) {
        return false;
      }
    }
    else if (!m_displayName.equals(other.m_displayName)) {
      return false;
    }
    if (m_targetId == null) {
      if (other.m_targetId != null) {
        return false;
      }
    }
    else if (!m_targetId.equals(other.m_targetId)) {
      return false;
    }
    return true;
  }

}
