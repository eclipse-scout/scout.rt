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
package org.eclipse.scout.rt.spec.client.out.internal;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.spec.client.out.IDocSectionHeading;

/**
 * {@link IDocSectionHeading}
 */
public class DocSectionHeading implements IDocSectionHeading {
  private final String m_id;
  private String m_name;

  public DocSectionHeading(String id, String name) {
    m_id = id;
    m_name = name;
  }

  @Override
  public String getId() {
    return m_id;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public boolean isValid() {
    return !StringUtility.isNullOrEmpty(getId()) && !StringUtility.isNullOrEmpty(getName());
  }

}
