/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.admin.html;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.admin.html.view.TopView;
import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlComponent;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.rt.shared.OfficialVersion;

@SuppressWarnings("bsiRulesDefinition:htmlInString")
public class AdminSession {

  private final TopView m_topView;
  private Map<String, AbstractHtmlAction> m_actionMap;

  public AdminSession() {
    m_topView = new TopView(this);
    m_actionMap = new HashMap<>();
  }

  public void serviceRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
    HtmlComponent emitter = new HtmlComponent(req, res);
    //
    String actionId = req.getParameter("actionId");
    if (actionId != null) {
      AbstractHtmlAction action = getAction(actionId);
      if (action != null) {
        Map<String, String> paramMap = new HashMap<>();
        for (String n : req.getParameterMap().keySet()) {
          String v = StringUtility.valueOf(req.getParameter(n));
          paramMap.put(n, v);
        }
        // set parameters
        action.setFormParameters(paramMap);
        try {
          action.run();
        }
        catch (Exception e) {
          action.setException(e);
        }
        emitter.setInvokedAction(action);
      }
    }
    ProcessInspector.instance().update();
    m_topView.produceBody(emitter);
    setActionMap(emitter.getActionMap());
    // prepare output
    res.setContentType("text/html");
    res.setDateHeader("Expires", -1);
    res.setHeader("Cache-Control", "no-cache");
    res.setHeader("pragma", "no-cache");
    // output
    @SuppressWarnings("resource")
    ServletOutputStream out = res.getOutputStream();
    out.println("<html>");
    out.println("<head>");
    out.println("<title>Eclipse Scout</title>");
    out.println("<style>");
    out.println("body {font-family: sans-serif; font-size: 12; background-color : #F6F6F6;}");
    out.println("a,a:VISITED {color: #6666ff;text-decoration: none;}");
    out.println("table {font-size: 12; empty-cells: show;}");
    out.println("th {text-align: left;vertical-align: top; padding-left: 2; background-color : #cccccc;}");
    out.println("td {text-align: left;vertical-align: top; padding-left: 2;}");
    out.println("p {margin-top: 4; margin-bottom: 4; padding-top: 4; padding-bottom: 4;}");
    out.println(".copyright {font-size: 10;}");
    out.println("</style>");
    out.println("</head>");
    out.println("<body>");
    out.println("<h3>Eclipse Scout</h3>");
    out.print(emitter.getProducedHtml());
    out.println("<p class=\"copyright\">&copy; " + OfficialVersion.COPYRIGHT + "</p>");
    out.println("</body>");
    out.println("</html>");
  }

  private AbstractHtmlAction getAction(String actionId) {
    return m_actionMap.get(actionId);
  }

  private void setActionMap(Map<String, AbstractHtmlAction> newActionMap) {
    m_actionMap = CollectionUtility.copyMap(newActionMap);
  }

  public TopView getTopView() {
    return m_topView;
  }
}
