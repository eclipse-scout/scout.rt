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
package org.eclipse.scout.rt.client.services.common.icon;

public class IconSpec {
  private String m_name;
  private byte[] m_content;

  public IconSpec() {
    this(null, null);
  }

  public IconSpec(byte[] content) {
    this(null, content);
  }

  public IconSpec(String name, byte[] content) {
    m_name = name;
    m_content = content;
  }

  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  public byte[] getContent() {
    return m_content;
  }

  public void setContent(byte[] content) {
    m_content = content;
  }

}
