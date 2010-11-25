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

import java.security.AccessController;
import java.security.Principal;
import java.util.Date;
import java.util.Iterator;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.rt.server.admin.html.AbstractHtmlAction;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlComponent;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;

public class GeneralView extends DefaultView {

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
    final ProcessInspector inst = ProcessInspector.getDefault();
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
          public void run() {
            inst.getIgnoredCallSet().clear();
            inst.getIgnoredCallSet().add(".*\\.IPingService\\.ping");
          }
        });
      }
      else {
        p.startLinkAction(new AbstractHtmlAction("IPingService.accept") {
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

    // infos
    if (Platform.getProduct() != null) {
      p.print("Product: id=" + Platform.getProduct().getId() + ", name=" + Platform.getProduct().getName() + ", app=" + Platform.getProduct().getApplication() + ", bundle=" + Platform.getProduct().getDefiningBundle());
      p.br();
    }
    else {
      p.print("No product available");
    }
    p.br();
    p.print("Date: " + new Date());
    p.br();
    p.print("You connect from: " + p.getRequest().getRemoteAddr() + " / " + p.getRequest().getRemoteHost());
    p.p();
    // show jaas context
    p.print("Session ID: " + p.getRequest().getSession().getId());
    p.br();
    p.print("Session Created: " + new Date(p.getRequest().getSession().getCreationTime()));
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
    if (inst.isEnabled()) {
      p.raw("<p><b>Note: Session Activity Monitor is enabled; this might affect performance and memory due to higher resource consumption during analysis.</b><p>");
    }
  }

  private class P_EnableAction extends AbstractHtmlAction {
    private boolean m_enabled;

    public P_EnableAction(boolean b) {
      super("mon.enabled." + b);
      m_enabled = b;
    }

    public void run() {
      ProcessInspector.getDefault().setEnabled(m_enabled);
    }
  }// end private class

  private class P_SetTimeoutAction extends AbstractHtmlAction {
    private long m_minutes;

    public P_SetTimeoutAction(long minutes) {
      super("mon.cache." + minutes);
      m_minutes = minutes;
    }

    public void run() {
      ProcessInspector.getDefault().setTimeout(m_minutes * 60000L);
    }
  }// end private class

}
