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
package org.eclipse.scout.rt.client.mobile.navigation;

import java.util.EventObject;

/**
 * @since 3.9.0
 */
public class BreadCrumbsEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private final int m_type;
  public static final int TYPE_CHANGED = 10;

  public BreadCrumbsEvent(IBreadCrumbsNavigation source, int type) {
    super(source);

    m_type = type;
  }

  public IBreadCrumbsNavigation getBreadCrumbsNavigation() {
    return (IBreadCrumbsNavigation) getSource();
  }

  public int getType() {
    return m_type;
  }
}
