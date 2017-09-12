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
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.server.admin.html.AbstractHtmlAction;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlComponent;
import org.eclipse.scout.rt.server.admin.inspector.CallInspector;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.rt.server.admin.inspector.SessionInspector;
import org.eclipse.scout.rt.server.admin.inspector.info.CallInfo;

@SuppressWarnings("bsiRulesDefinition:htmlInString")
public class CallsView extends DefaultView {

  private CallInspector m_selectedCall;

  public CallsView(AdminSession as) {
    super(as);
  }

  public CallInspector getSelectedCall() {
    return m_selectedCall;
  }

  @Override
  public boolean isVisible() {
    return ProcessInspector.instance().isEnabled() && getAdminSession().getTopView().getSessionsView().getSelectedSession() != null;
  }

  @Override
  public void produceTitle(HtmlComponent p) {
    final SessionInspector session = getAdminSession().getTopView().getSessionsView().getSelectedSession();
    p.print("Calls of " + (session != null ? session.getInfo().getUserId() + "/" + session.getInfo().getSessionId() : "?"));
  }

  @Override
  public void produceBody(HtmlComponent p) {
    final SessionInspector session = getAdminSession().getTopView().getSessionsView().getSelectedSession();
    p.linkAction("Clear calls", new AbstractHtmlAction("clearCalls") {

      @Override
      public void run() {
        session.clearCallInspectors();
      }
    });
    p.p();
    //
    p.startTable(0, 5, 5);
    p.startTableRow();
    p.startTableCell();
    renderCallTable(p, session);
    p.startTableCell();
    // selected call
    if (m_selectedCall != null) {
      renderCallDetail(p, m_selectedCall);
    }
    p.endTableCell();
    p.endTableRow();
    p.endTable();
  }

  private void renderCallTable(HtmlComponent p, SessionInspector session) {
    TreeMap<CompositeObject, CallInspector> timeToCalls = new TreeMap<>();
    CallInspector[] callInspectors = session.getCallInspectors();
    for (int i = 0; i < callInspectors.length; i++) {
      long startTime = callInspectors[i].getInfo().getStartTime();
      timeToCalls.put(new CompositeObject(startTime, i), callInspectors[i]);
    }
    CallInspector[] sorted = timeToCalls.values().toArray(new CallInspector[timeToCalls.size()]);
    p.bold("Calls");
    p.startTable(1, 0, 3);
    p.startTableRow();
    p.tableHeaderCell("#");
    p.tableHeaderCell("Operation");
    p.tableHeaderCell("Started");
    p.tableHeaderCell("Duration");
    p.tableHeaderCell("Status");
    p.endTableRow();
    CallInspector validSelection = null;
    for (int i = sorted.length - 1; i >= 0; i--) {
      if (sorted[i] == m_selectedCall) {
        validSelection = m_selectedCall;
      }
      renderCallRow(p, i + 1, sorted[i]);
    }
    m_selectedCall = validSelection;
    p.endTable();
  }

  private void renderCallRow(HtmlComponent p, int index, final CallInspector call) {
    boolean selected = m_selectedCall != null && (m_selectedCall == call);
    SimpleDateFormat startFmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    String serviceShortName = call.getInfo().getService();
    int i = Math.max(serviceShortName.lastIndexOf('.'), serviceShortName.lastIndexOf('$'));
    if (i >= 0) {
      serviceShortName = serviceShortName.substring(i + 1);
    }
    CallInfo info = call.getInfo();
    //
    p.startTableRow();
    p.tableCell("" + index);
    p.startTableCell();
    String callId = serviceShortName + "." + info.getOperation();
    String callKey = serviceShortName + "." + info.getOperation() + "." + info.getStartTime();
    if (selected) {
      p.focusAnchor();
    }
    p.startLinkAction(new AbstractHtmlAction("selectCall." + callKey) {

      @Override
      public void run() {
        m_selectedCall = call;
      }
    });
    if (selected) {
      p.bold(callId);
    }
    else {
      p.print(callId);
    }
    p.endLinkAction();
    p.endTableCell();
    p.startTableCell();
    p.printNoBreak(startFmt.format(new Date(info.getStartTime())));
    p.endTableCell();
    p.startTableCell();
    p.printNoBreak("" + info.getDuration() + " ms");
    p.endTableCell();
    p.startTableCell();
    if (info.isActive()) {
      p.printNoBreak("RUNNING");
    }
    else {
      if (info.getReturnException() == null) {
        p.raw("<font color='008800'>");
        p.printNoBreak("COMPLETED OK");
        p.raw("</font>");
      }
      else {
        p.raw("<font color='880000'>");
        p.printNoBreak("COMPLETED WITH ERROR");
        p.raw("</font>");
      }
    }
    p.endTableCell();
    p.endTableRow();
  }

  private void renderCallDetail(HtmlComponent p, CallInspector call) {
    SimpleDateFormat startFmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    CallInfo info = call.getInfo();
    //
    p.startTable(0);
    //
    p.startTableRow();
    p.tableCell("Service");
    p.tableCell(info.getService());
    p.endTableRow();
    //
    p.spacingRow(2);
    //
    p.startTableRow();
    p.tableCell("Operation");
    p.tableCell(info.getOperation());
    p.endTableRow();
    //
    p.spacingRow(2);
    //
    p.startTableRow();
    p.tableCell("Start");
    p.tableCell(startFmt.format(new Date(info.getStartTime())));
    p.endTableRow();
    //
    p.spacingRow(2);
    //
    p.startTableRow();
    p.tableCell("End");
    if (info.isActive()) {
      p.tableCell("PENDING");
    }
    else {
      p.tableCell(startFmt.format(new Date(info.getEndTime())));
    }
    p.endTableRow();
    //
    p.spacingRow(2);
    //
    p.startTableRow();
    p.tableCell("Duration");
    p.tableCell(info.getDuration() + " ms");
    p.endTableRow();
    //
    p.spacingRow(2);
    //
    p.startTableRow();
    p.tableCell("XA&nbsp;resources");
    p.startTableCell();
    ITransactionMember[] xaresources = info.getXaResources();
    renderValueTable(p, xaresources);
    p.endTableCell();
    p.endTableRow();
    //
    if (!info.isActive()) {
      //
      p.spacingRow(2);
      //
      p.startTableRow();
      p.tableCell("Arguments");
      p.startTableCell();
      renderValueTable(p, info.getArguments());
      p.endTableCell();
      p.endTableRow();
      if (info.getReturnException() == null) {
        //
        p.spacingRow(2);
        //
        p.startTableRow();
        p.tableCell("Out&nbsp;variables");
        p.startTableCell();
        renderValueTable(p, info.getOutVariables());
        p.endTableCell();
        p.endTableRow();
        //
        p.spacingRow(2);
        //
        p.startTableRow();
        p.tableCell("Return&nbsp;data");
        p.startTableCell();
        renderValueTable(p, new Object[]{info.getReturnData()});
        p.endTableCell();
        p.endTableRow();
      }
      else {
        StringWriter sw = new StringWriter();
        info.getReturnException().printStackTrace(new PrintWriter(sw, true));
        //
        p.spacingRow(2);
        //
        p.startTableRow();
        p.tableCell("Return&nbsp;exception");
        p.tableCell(sw.toString());
        p.endTableRow();
      }
    }
    p.endTable();
  }

  protected void renderValueTable(HtmlComponent p, Object o) {
    boolean singleValue;
    if (o == null) {
      singleValue = true;
    }
    else if (o.getClass().isArray()) {
      singleValue = false;
    }
    else if (o instanceof Map) {
      singleValue = false;
    }
    else if (o instanceof Collection) {
      singleValue = false;
    }
    else {
      // bean introspection
      singleValue = true;
    }
    //
    if (o == null || singleValue) {
      p.print("" + o);
    }
    else {
      p.startTable(1);
      p.startTableRow();
      p.tableHeaderCell("Name");
      p.tableHeaderCell("Type");
      p.tableHeaderCell("Value");
      p.endTableRow();
      if (o.getClass().isArray()) {
        int len = Array.getLength(o);
        for (int i = 0; i < len; i++) {
          renderValueRow(p, "[" + i + "]", Array.get(o, i));
        }
      }
      else if (o instanceof Map) {
        for (Object o1 : ((Map) o).entrySet()) {
          Entry e = (Entry) o1;
          renderValueRow(p, "{" + "" + e.getKey() + "}", e.getValue());
        }
      }
      else if (o instanceof Collection) {
        int index = 0;
        for (Object value : ((Iterable) o)) {
          renderValueRow(p, "" + index, value);
          index++;
        }
      }
      else {
        // only reached when doing further inspection
        renderValueRow(p, "", o);
      }
      p.endTable();
    }
  }

  protected void renderValueRow(HtmlComponent p, String name, Object value) {
    p.startTableRow();
    p.tableCell(name);
    p.tableCell((value != null ? value.getClass().getName() : ""));
    p.startTableCell();
    renderValueTable(p, value);
    p.endTableCell();
    p.endTableRow();
  }

}
