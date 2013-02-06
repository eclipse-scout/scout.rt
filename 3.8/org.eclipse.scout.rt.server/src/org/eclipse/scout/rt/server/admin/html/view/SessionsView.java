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
package org.eclipse.scout.rt.server.admin.html.view;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.rt.server.admin.html.AbstractHtmlAction;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlComponent;
import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlTable;
import org.eclipse.scout.rt.server.admin.html.widget.table.SortInfo;
import org.eclipse.scout.rt.server.admin.html.widget.table.VirtualRow;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.rt.server.admin.inspector.SessionInspector;
import org.eclipse.scout.rt.shared.security.UpdateServiceConfigurationPermission;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;

public class SessionsView extends DefaultView {
  private SessionInspector m_selectedSession;
  private SortInfo m_table1SortInfo;

  public SessionsView(AdminSession as) {
    super(as);
    m_table1SortInfo = new SortInfo();
  }

  public SessionInspector getSelectedSession() {
    return m_selectedSession;
  }

  @Override
  public boolean isVisible() {
    return ACCESS.check(new UpdateServiceConfigurationPermission()) && ProcessInspector.getDefault().isEnabled();
  }

  @Override
  public void produceTitle(HtmlComponent p) {
    p.print("Sessions");
  }

  @Override
  public void produceBody(HtmlComponent p) {
    p.linkAction("Clear sessions", new AbstractHtmlAction("clearSessions") {
      @Override
      public void run() {
        ProcessInspector.getDefault().clearSessionInspectors();
      }
    });
    p.p();
    //
    renderSessionTable(p);
  }

  private void renderSessionTable(HtmlComponent p) {
    SessionInspector[] sessionInspectors = ProcessInspector.getDefault().getSessionInspectors();
    TreeMap<CompositeObject, SessionInspector> userAndTimeToSessions = new TreeMap<CompositeObject, SessionInspector>();
    for (int i = 0; i < sessionInspectors.length; i++) {
      String user = sessionInspectors[i].getInfo().getUserId();
      long lastAccess = sessionInspectors[i].getInfo().getLastAccessedTime();
      userAndTimeToSessions.put(new CompositeObject(lastAccess, user, i), sessionInspectors[i]);
    }
    SessionInspector[] sorted = userAndTimeToSessions.values().toArray(new SessionInspector[userAndTimeToSessions.size()]);
    // render
    HtmlTable table1 = new HtmlTable(p, "table1", m_table1SortInfo);
    table1.startTable(1, 0, 3);
    table1.startTableRow();
    table1.tableHeaderCell("#");
    table1.tableHeaderCell("User");
    table1.tableHeaderCell("SessionID");
    table1.tableHeaderCell("Details");
    table1.tableHeaderCell("Created");
    table1.tableHeaderCell("Last&nbsp;accessed");
    table1.tableHeaderCell("JAAS");
    table1.tableHeaderCell("UserAgent");
    table1.endTableRow();
    SessionInspector validSelection = null;
    for (int i = sorted.length - 1; i >= 0; i--) {
      if (sorted[i] == m_selectedSession) {
        validSelection = m_selectedSession;
      }
      VirtualRow vrow = table1.addVirtualRow();
      renderSessionRow(vrow, i + 1, sorted[i]);
    }
    table1.appendVirtualRows();
    m_selectedSession = validSelection;
    table1.endTable();
    p.append(table1);
  }

  private void renderSessionRow(HtmlComponent p, int index, final SessionInspector session) {
    boolean selected = m_selectedSession != null && (m_selectedSession == session);
    SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    //
    p.startTableRow();
    p.tableCell("" + index);
    p.tableCell(session.getInfo().getUserId());
    p.startTableCell();
    if (selected) {
      p.focusAnchor();
    }
    p.startLinkAction(new AbstractHtmlAction("selectSession" + session.getInfo().getSessionId()) {
      @Override
      public void run() {
        m_selectedSession = session;
      }
    });
    p.print(session.getInfo().getSessionId());
    p.endLinkAction();
    p.endTableCell();
    p.startTableCell();
    p.startLinkAction(new AbstractHtmlAction("showServicesOf" + session.getInfo().getSessionId()) {
      @Override
      public void run() {
        m_selectedSession = session;
        getAdminSession().getTopView().showServices();
      }
    });
    p.print("Services");
    p.endLinkAction();
    p.raw("&nbsp;");
    p.startLinkAction(new AbstractHtmlAction("showCallsOf" + session.getInfo().getSessionId()) {
      @Override
      public void run() {
        m_selectedSession = session;
        getAdminSession().getTopView().showCalls();
      }
    });
    p.print("Calls");
    p.endLinkAction();
    p.endTableCell();
    p.startTableCell();
    p.printNoBreak(fmt.format(new Date(session.getInfo().getCreationTime())));
    p.endTableCell();
    p.startTableCell();
    p.printNoBreak(fmt.format(new Date(session.getInfo().getLastAccessedTime())));
    p.endTableCell();
    p.startTableCell();
    // show jaas context
    try {
      Subject subject = session.getInfo().getSubject();
      if (subject != null) {
        int i = 0;
        for (Principal principal : subject.getPrincipals()) {
          if (principal != null) {
            if (i > 0) {
              p.br();
            }
            String s = "principal[" + i + "] name=" + principal.getName() + " toString=" + principal.toString() + " class=" + principal.getClass();
            s = s.replaceAll("[\\n\\r]+", " ");
            p.printNoBreak(s);
            i++;
          }
        }
      }
      else {
        p.printNoBreak("no subject available");
      }
    }
    catch (Exception e) {
      p.br();
      p.print("Exception: " + e);
    }
    p.endTableCell();
    p.startTableCell();
    p.printNoBreak(session.getInfo().getUserAgent().toString());
    p.endTableCell();
    p.endTableRow();
  }

}
