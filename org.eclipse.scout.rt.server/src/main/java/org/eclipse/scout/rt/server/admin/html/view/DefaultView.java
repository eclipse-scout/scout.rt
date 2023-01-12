/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.admin.html.view;

import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.admin.html.IView;
import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlComponent;

public class DefaultView implements IView {

  private final AdminSession m_as;

  public DefaultView(AdminSession as) {
    m_as = as;
  }

  public AdminSession getAdminSession() {
    return m_as;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  @Override
  public void produceTitle(HtmlComponent p) {
    // empty default implementation
  }

  @Override
  public void produceBody(HtmlComponent p) {
    // empty default implementation
  }

  @Override
  public void activated() {
    // empty default implementation
  }
}
