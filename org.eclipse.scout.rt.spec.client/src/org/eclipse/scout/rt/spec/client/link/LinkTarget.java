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

import org.eclipse.scout.rt.spec.client.out.ILinkTarget;

/**
 * A link target pointing to a position in a file
 */
public class LinkTarget implements ILinkTarget {

  private final String m_fileName;
  private final String m_targetId;

  /**
   * @param targetId
   * @param displayName
   */
  public LinkTarget(String targetId, String fileName) {
    m_targetId = targetId;
    m_fileName = fileName;
  }

  @Override
  public String getTargetIdWithFileName() {
    return m_fileName + "#" + m_targetId;
  }

  @Override
  public String getTargetId() {
    return m_targetId;
  }

}
