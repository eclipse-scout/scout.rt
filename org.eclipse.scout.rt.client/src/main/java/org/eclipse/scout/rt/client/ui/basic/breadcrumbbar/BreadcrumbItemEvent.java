/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
