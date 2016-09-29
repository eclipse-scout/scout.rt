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

import org.eclipse.scout.rt.server.admin.html.AbstractHtmlAction;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.admin.html.IView;
import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlComponent;

@SuppressWarnings("bsiRulesDefinition:htmlInString")
public class TopView extends DefaultView {

  private MessagesView m_messagesView;
  private GeneralView m_generalView;
  private ServicesView m_servicesView;
  private SessionsView m_sessionsView;
  private CallsView m_callsView;

  private IView m_activeView;

  public TopView(AdminSession as) {
    super(as);
    m_messagesView = new MessagesView(as);
    m_generalView = new GeneralView(as);
    m_servicesView = new ServicesView(as);
    m_sessionsView = new SessionsView(as);
    m_callsView = new CallsView(as);

    m_activeView = m_generalView;
  }

  public IView getActiveView() {
    return m_activeView;
  }

  public void showGeneral() {
    m_activeView = m_generalView;
  }

  public void showServices() {
    m_activeView = m_servicesView;
  }

  public void showUsers() {
    m_activeView = m_sessionsView;
  }

  public void showSessions() {
    m_activeView = m_sessionsView;
  }

  public void showCalls() {
    m_activeView = m_callsView;
  }

  public GeneralView getGeneralView() {
    return m_generalView;
  }

  public ServicesView getServicesView() {
    return m_servicesView;
  }

  public SessionsView getSessionsView() {
    return m_sessionsView;
  }

  public CallsView getCallsView() {
    return m_callsView;
  }

  @Override
  public void produceBody(HtmlComponent p) {
    m_messagesView.produceBody(p);
    p.raw("[ ");
    for (IView v : new IView[]{m_generalView, m_servicesView, m_sessionsView, m_callsView}) {
      if (v.isVisible()) {
        p.raw("&nbsp;");
        if (v == m_activeView) {
          p.raw("<b>");
          v.produceTitle(p);
          p.raw("</b>");
        }
        else {
          final IView finalV = v;
          p.startLinkAction(
              new AbstractHtmlAction("tab." + v.getClass().getSimpleName()) {

                @Override
                public void run() {
                  m_activeView = finalV;
                  if (m_activeView != null) {
                    m_activeView.activated();
                  }
                }
              });
          v.produceTitle(p);
          p.endLinkAction();
        }
        p.raw("&nbsp;");
        p.raw("&nbsp;");
      }
    }
    p.raw(" ]");
    p.p();
    if (m_activeView != null) {
      m_activeView.produceBody(p);
    }
  }
}
