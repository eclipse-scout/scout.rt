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

import java.security.AccessController;
import java.security.Principal;
import java.util.Date;
import java.util.Iterator;

import javax.security.auth.Subject;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationNameProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationVersionProperty;
import org.eclipse.scout.rt.platform.util.VerboseUtility;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.admin.html.AbstractHtmlAction;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlComponent;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.security.UpdateServiceConfigurationPermission;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;

@SuppressWarnings("bsiRulesDefinition:htmlInString")
public class GeneralView extends DefaultView {

  private static final long serialVersionUID = -5324529296371616980L;

  public GeneralView(AdminSession as) {
    super(as);
  }

  @Override
  public void produceTitle(HtmlComponent p) {
    p.print("General");
  }

  @Override
  public void produceBody(HtmlComponent p) {
    // menu
    String monitoringStatusMessage = createMonitoringQuickLink(p);

    // infos
    String title = CONFIG.getPropertyValue(ApplicationNameProperty.class);
    String version = CONFIG.getPropertyValue(ApplicationVersionProperty.class);

    p.print("Product: name=" + title + ", version=" + version);
    p.br();
    p.br();
    p.print("Date: " + new Date());
    p.br();
    p.print("You connect from: " + p.getRequest().getRemoteAddr() + " / " + p.getRequest().getRemoteHost());
    p.p();

    HttpSession session = p.getRequest().getSession(false);
    if (session != null) {
      p.print("Session ID: " + session.getId());
      p.br();
      p.print("Session Created: " + new Date(session.getCreationTime()));
    }
    else {
      p.print("There is no HTTP-Session needed ");
    }
    p.br();
    IServerSession serverSession = ServerSessionProvider.currentSession();
    if (serverSession != null) {
      p.print("Session ID (ThreadContext): " + serverSession.getId());
    }
    else {
      p.print("There is no Session found");
    }

    p.br();
    p.print("JAAS Context");
    p.br();
    p.print("&nbsp;&nbsp;remoteUser: " + p.getRequest().getRemoteUser());
    p.br();
    Principal remotePrincipal = p.getRequest().getUserPrincipal();
    if (remotePrincipal != null) {
      p.print("&nbsp;&nbsp;userPrincipal: " + remotePrincipal.getName() + " [" + remotePrincipal.getClass().getSimpleName() + "]");
    }
    else {
      p.print("&nbsp;&nbsp;userPrincipal: null");
    }
    p.br();
    try {
      SecurityManager sm = System.getSecurityManager();
      p.print("&nbsp;&nbsp;SecurityManager: " + sm);
      p.br();
      Subject subject = Subject.getSubject(AccessController.getContext());
      p.print("&nbsp;&nbsp;Subject: " + VerboseUtility.dumpObject(subject));
      p.br();
      if (subject != null) {
        int i1 = 0;
        for (Iterator it1 = subject.getPrincipals().iterator(); it1.hasNext();) {
          Principal principal = (Principal) it1.next();
          if (principal != null) {
            p.print("&nbsp;&nbsp;&nbsp;&nbsp;principal[" + i1 + "]=" + principal.getName() + " [" + principal.getClass().getName() + "]");
            p.br();
          }
          i1++;
        }
      }
    }
    catch (Exception e) {
      p.print("Exception: " + e);
      p.br();
    }
    p.br();
    if (monitoringStatusMessage != null) {
      p.raw(monitoringStatusMessage);
    }
  }

  private String createMonitoringQuickLink(HtmlComponent p) {
    if (!ACCESS.check(new UpdateServiceConfigurationPermission())) {
      return null;
    }

    final ProcessInspector inst = ProcessInspector.instance();
    if (inst.isEnabled()) {
      p.print("Monitor is active with maximum caching of " + (inst.getTimeout() / 1000 / 60) + " minutes [ ");
      p.linkAction("cache 2 min", new P_SetTimeoutAction(2));
      p.print(" | ");
      p.linkAction("cache 15 min", new P_SetTimeoutAction(15));
      p.print(" | ");
      p.linkAction("cache 60 min", new P_SetTimeoutAction(60));
      p.print(" | ");
      p.linkAction("deactivate", new P_EnableAction(false));
      p.print(" ]");
      p.br();
      if (inst.acceptCall(IPingService.class.getName(), "ping")) {
        p.linkAction("IPingService.ping (click to toggle)", new AbstractHtmlAction("IPingService.ignore") {
          private static final long serialVersionUID = -7667956603352457067L;

          @Override
          public void run() {
            inst.getIgnoredCallSet().clear();
            inst.getIgnoredCallSet().add(".*\\.IPingService\\.ping");
          }
        });
      }
      else {
        p.startLinkAction(new AbstractHtmlAction("IPingService.accept") {
          private static final long serialVersionUID = 8429237856017595619L;

          @Override
          public void run() {
            inst.getIgnoredCallSet().clear();
          }
        });
        p.raw("<s>");
        p.printNoBreak("IPingService.ping");
        p.raw("</s>");
        p.printNoBreak(" (click to toggle)");
        p.endLinkAction();
      }
    }
    else {
      p.print("Monitor is inactive [ ");
      p.linkAction("activate", new P_EnableAction(true));
      p.print(" ]");
    }
    p.p();

    if (inst.isEnabled()) {
      return "<p><b>Note: Session Activity Monitor is enabled; this might affect performance and memory due to higher resource consumption during analysis.</b><p>";
    }
    return null;
  }

  private class P_EnableAction extends AbstractHtmlAction {
    private static final long serialVersionUID = 3594131240310244266L;
    private boolean m_enabled;

    public P_EnableAction(boolean b) {
      super("mon.enabled." + b);
      m_enabled = b;
    }

    @Override
    public void run() {
      ProcessInspector.instance().setEnabled(m_enabled);
    }
  }

  private class P_SetTimeoutAction extends AbstractHtmlAction {
    private static final long serialVersionUID = -2870869345515125996L;
    private long m_minutes;

    public P_SetTimeoutAction(long minutes) {
      super("mon.cache." + minutes);
      m_minutes = minutes;
    }

    @Override
    public void run() {
      ProcessInspector.instance().setTimeout(m_minutes * 60000L);
    }
  }
}
