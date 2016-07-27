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
package org.eclipse.scout.rt.platform.html;

import java.io.Serializable;

/**
 * Bean representing an app link.
 */
public class AppLink implements Serializable {

  private static final long serialVersionUID = 1L;

  private String m_ref;
  private String m_name;

  public AppLink() {
    super();
  }

  /**
   * Creates an app link bean
   *
   * @param ref
   *          Reference
   * @param name
   *          Name of the app link
   */
  public AppLink(String ref, String name) {
    m_ref = ref;
    m_name = name;
  }

  public String getRef() {
    return m_ref;
  }

  public void setRef(String ref) {
    m_ref = ref;
  }

  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }
}
