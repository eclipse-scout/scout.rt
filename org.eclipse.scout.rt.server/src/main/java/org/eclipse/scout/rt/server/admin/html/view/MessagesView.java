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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.scout.rt.server.admin.html.AbstractHtmlAction;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlComponent;
import org.eclipse.scout.rt.shared.security.UpdateServiceConfigurationPermission;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;

@SuppressWarnings("bsiRulesDefinition:htmlInString")
public class MessagesView extends DefaultView {

  private static final long serialVersionUID = -2617599437449773944L;

  public MessagesView(AdminSession as) {
    super(as);
  }

  @Override
  public boolean isVisible() {
    return ACCESS.check(new UpdateServiceConfigurationPermission());
  }

  @Override
  public void produceTitle(HtmlComponent p) {
    p.pBold("Information");
  }

  @Override
  public void produceBody(HtmlComponent p) {
    if (!isEmpty(p)) {
      p.startTable(1);
      p.startTableRow();
      p.startTableCell(1, 1, "ffff00");
      renderMessages(p);
      p.endTableCell();
      p.endTableRow();
      p.endTable();
      p.p("");
    }
  }

  private void renderMessages(HtmlComponent p) {
    AbstractHtmlAction a = p.getInvokedAction();
    if (a != null) {
      if (a.getException() != null) {
        StringWriter sw = new StringWriter();
        a.getException().printStackTrace(new PrintWriter(sw, true));
        p.raw("<pre>");
        p.print(sw.toString());
        p.raw("</pre>");
        p.br();
      }
      if (a.getPlainText() != null) {
        p.raw("<pre>");
        p.print(a.getPlainText());
        p.raw("</pre>");
        p.br();
      }
    }
  }

  public boolean isEmpty(HtmlComponent p) {
    AbstractHtmlAction a = p.getInvokedAction();
    if (a != null) {
      if (a.getException() != null) {
        return false;
      }
      if (a.getPlainText() != null) {
        return false;
      }
    }
    return true;
  }
}
