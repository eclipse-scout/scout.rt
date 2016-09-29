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
package org.eclipse.scout.rt.server.admin.html.view;

import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.admin.html.IView;
import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlComponent;

public class DefaultView implements IView {

  private AdminSession m_as;

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
