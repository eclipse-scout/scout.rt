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

import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.NumberUtility;
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
  private static final long serialVersionUID = 4697127041507442841L;
  private SessionInspector m_selectedSession;
  private final SortInfo m_table1SortInfo;

  public SessionsView(AdminSession as) {
    super(as);
    m_table1SortInfo = new SortInfo();
  }

  public SessionInspector getSelectedSession() {
    return m_selectedSession;
  }

  @Override
  public boolean isVisible() {
    return ACCESS.check(new UpdateServiceConfigurationPermission()) && ProcessInspector.instance().isEnabled();
  }

  @Override
  public void produceTitle(HtmlComponent p) {
    p.print("Sessions");
  }

  @Override
  public void produceBody(HtmlComponent p) {
    p.linkAction("Clear sessions", new AbstractHtmlAction("clearSessions") {

      private static final long serialVersionUID = -4212688129915270659L;

      @Override
      public void run() {
        ProcessInspector.instance().clearSessionInspectors();
      }
    });
    p.p();
    //
    renderSessionTable(p);
  }

  protected void renderSessionTable(HtmlComponent p) {
    HtmlTable table1 = new HtmlTable(p, "table1", m_table1SortInfo);
    table1.startTable(1, 0, 3);

    renderSessionTableHeader(table1);

    SessionInspector validSelection = null;
    SessionInspector[] sorted = getSortedSessions();
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

  protected SessionInspector[] getSortedSessions() {
    SessionInspector[] sessionInspectors = ProcessInspector.instance().getSessionInspectors();
    TreeMap<CompositeObject, SessionInspector> userAndTimeToSessions = new TreeMap<CompositeObject, SessionInspector>();
    for (int i = 0; i < sessionInspectors.length; i++) {
      String user = sessionInspectors[i].getInfo().getUserId();
      long lastAccess = NumberUtility.nvl(sessionInspectors[i].getInfo().getLastAccessedTime(), 0L);
      userAndTimeToSessions.put(new CompositeObject(lastAccess, user, i), sessionInspectors[i]);
    }
    return userAndTimeToSessions.values().toArray(new SessionInspector[userAndTimeToSessions.size()]);
  }

  protected void renderSessionTableHeader(HtmlTable table) {
    table.startTableRow();
    table.tableHeaderCell("#");
    table.tableHeaderCell("User");
    table.tableHeaderCell("SessionID");
    table.tableHeaderCell("Details");
    table.tableHeaderCell("Created");
    table.tableHeaderCell("Last&nbsp;accessed");
    table.tableHeaderCell("JAAS");
    table.tableHeaderCell("UserAgent");
    table.endTableRow();
  }

  protected void renderSessionRow(HtmlComponent p, int index, SessionInspector session) {
    p.startTableRow();
    renderIndexCell(p, index);
    renderUserIdCell(p, session);
    renderSessionIdCell(p, session);
    renderSessionDetailsCell(p, session);
    renderCreatedCell(p, session);
    renderLastAccessedCell(p, session);
    renderJaasCell(p, session);
    renderUserAgentCell(p, session);
    p.endTableRow();
  }

  protected void renderUserAgentCell(HtmlComponent p, SessionInspector session) {
    p.startTableCell();
    p.printNoBreak(session.getInfo().getUserAgent().toString());
    p.endTableCell();
  }

  protected void renderJaasCell(HtmlComponent p, SessionInspector session) {
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
  }

  protected void renderLastAccessedCell(HtmlComponent p, SessionInspector session) {
    p.startTableCell();
    p.printNoBreak(formatTime(session.getInfo().getLastAccessedTime()));
    p.endTableCell();
  }

  protected void renderCreatedCell(HtmlComponent p, SessionInspector session) {
    p.startTableCell();
    p.printNoBreak(formatTime(session.getInfo().getCreationTime()));
    p.endTableCell();
  }

  protected void renderSessionDetailsCell(HtmlComponent p, final SessionInspector session) {
    p.startTableCell();
    p.startLinkAction(new AbstractHtmlAction("showServicesOf" + session.getInfo().getSessionId()) {
      private static final long serialVersionUID = 9010809462756614037L;

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
      private static final long serialVersionUID = -7595683661385397351L;

      @Override
      public void run() {
        m_selectedSession = session;
        getAdminSession().getTopView().showCalls();
      }
    });
    p.print("Calls");
    p.endLinkAction();
    p.endTableCell();
  }

  protected void renderSessionIdCell(HtmlComponent p, final SessionInspector session) {
    p.startTableCell();
    if (isSelectedSession(session)) {
      p.focusAnchor();
    }
    p.startLinkAction(new AbstractHtmlAction("selectSession" + session.getInfo().getSessionId()) {
      private static final long serialVersionUID = 898464700226491147L;

      @Override
      public void run() {
        m_selectedSession = session;
      }
    });
    p.print(session.getInfo().getSessionId());
    p.endLinkAction();
    p.endTableCell();
  }

  protected void renderUserIdCell(HtmlComponent p, SessionInspector session) {
    p.tableCell(session.getInfo().getUserId());
  }

  protected void renderIndexCell(HtmlComponent p, int index) {
    p.tableCell("" + index);
  }

  protected boolean isSelectedSession(SessionInspector session) {
    return m_selectedSession != null && m_selectedSession == session;
  }

  /**
   * Formats a timestamp according to the format "dd.MM.yyyy HH:mm:ss"
   *
   * @param timestamp
   *          possibly {@code null}
   * @return a formatted timestamp
   */
  protected String formatTime(Long timestamp) {
    if (timestamp != null) {
      SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
      return format.format(new Date(timestamp));
    }
    return "";
  }
}
