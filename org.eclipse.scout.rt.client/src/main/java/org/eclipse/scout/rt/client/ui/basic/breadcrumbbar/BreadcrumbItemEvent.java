/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.breadcrumbbar;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

public class BreadcrumbItemEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_BREADCRUMB_ITEM_ACTION = 10;

  private final int m_type;

  public BreadcrumbItemEvent(IBreadcrumbItem source, int type) {
    super(source);
    m_type = type;
  }

  @Override
  public int getType() {
    return m_type;
  }
}
