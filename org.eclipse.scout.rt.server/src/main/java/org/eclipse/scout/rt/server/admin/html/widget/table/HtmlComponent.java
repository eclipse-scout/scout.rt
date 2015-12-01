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
package org.eclipse.scout.rt.server.admin.html.widget.table;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.admin.html.AbstractHtmlAction;

public class HtmlComponent {
  protected String m_baseURL;
  protected HttpServletRequest m_req;
  protected HttpServletResponse m_res;
  protected StringWriter m_writer;
  protected PrintWriter out;
  // action list
  protected Map<String, AbstractHtmlAction> m_actionMap;
  // action invokation feedback
  protected AbstractHtmlAction m_invokedAction;

  public HtmlComponent(HttpServletRequest req, HttpServletResponse res) {
    m_req = req;
    m_res = res;
    m_baseURL = m_req.getContextPath() + m_req.getServletPath();
    m_writer = new StringWriter();
    out = new PrintWriter(m_writer, true);
    m_actionMap = new HashMap<String, AbstractHtmlAction>();
  }

  public HtmlComponent(HtmlComponent other) {
    m_req = other.m_req;
    m_res = other.m_res;
    m_baseURL = other.m_baseURL;
    m_invokedAction = other.m_invokedAction;
    //
    m_writer = new StringWriter();
    out = new PrintWriter(m_writer, true);
    m_actionMap = new HashMap<String, AbstractHtmlAction>();
  }

  public void append(HtmlComponent other) {
    out.print(other.getProducedHtml());
    m_actionMap.putAll(other.m_actionMap);
  }

  public void setInvokedAction(AbstractHtmlAction invokedAction) {
    m_invokedAction = invokedAction;
  }

  public AbstractHtmlAction getInvokedAction() {
    return m_invokedAction;
  }

  private String buildActionId(AbstractHtmlAction action) {
    return action.getUid();
  }

  public HttpServletRequest getRequest() {
    return m_req;
  }

  public HttpServletResponse getResponse() {
    return m_res;
  }

  public Map<String, AbstractHtmlAction> getActionMap() {
    return m_actionMap;
  }

  public String getProducedHtml() {
    return m_writer.toString();
  }

  public void linkAction(String actionName, AbstractHtmlAction action) {
    startLinkAction(action);
    print(actionName);
    endLinkAction();
  }

  public void startLinkAction(AbstractHtmlAction action) {
    String actionId = buildActionId(action);
    m_actionMap.put(actionId, action);
    String u = m_baseURL + "?actionId=" + actionId + "#focus";
    out.print("<a href='" + u + "'>");
  }

  public void endLinkAction() {
    out.print("</a>");
  }

  public void startForm(AbstractHtmlAction action) {
    String actionId = buildActionId(action);
    m_actionMap.put(actionId, action);
    String u = m_baseURL;
    out.print("<form action='" + u + "' method='get'>");
    out.print("<input type='hidden' name='actionId' value='" + actionId + "'>");
  }

  public void formTextArea(String fieldName, String value) {
    if (value == null) {
      value = "";
    }
    int valueLen = value.length();
    int cols = 50;
    int rows = 1;
    rows = Math.max(rows, (valueLen + cols - 1) / cols);
    rows = Math.max(rows, StringUtility.getLineCount(value));
    out.print("<textarea rows=" + rows + " cols=" + cols + " name='" + fieldName + "'>");
    print(value);
    out.print("</textarea>");
  }

  public void startListBox(String fieldName, int size, boolean actionOnClick) {
    out.print("<select name=\"" + fieldName + "\" size=\"" + size + "\"");
    if (actionOnClick) {
      out.print(" onchange=\"javascript:window.location.href='" + m_baseURL + "?actionId='+this.value+'#focus';\"");
    }
    out.print(">");
  }

  public void radioBoxOption(String fieldName, String text, AbstractHtmlAction action, boolean selected) {
    String actionId = buildActionId(action);
    m_actionMap.put(actionId, action);
    out.print("<input type=\"radio\" name=\"" + fieldName + "\" value=\"" + actionId + "\" " + (selected ? " checked=\"checked\" " : "") + " onchange=\"javascript:window.location.href='" + m_baseURL + "?actionId='+this.value+'#focus';\">"
        + javaToHtml(text) + "</input>");
  }

  /**
   * normaly used with {@link #startListBox(int, FALSE)}
   */
  public void listBoxOption(String text, String value, boolean selected) {
    out.print("<option" + (selected ? " selected" : "") + " value=\"" + value + "\">" + javaToHtml(text) + "</option>");
  }

  /**
   * normaly used with {@link #startListBox(int, TRUE)}
   */
  public void listBoxOption(String text, AbstractHtmlAction action, boolean selected) {
    String actionId = buildActionId(action);
    m_actionMap.put(actionId, action);
    out.print("<option" + (selected ? " selected" : "") + " value=\"" + actionId + "\">" + javaToHtml(text) + "</option>");
  }

  public void endListBox() {
    out.print("</select>");
  }

  public void formSubmit(String value) {
    out.print("<input type=submit value='" + value + "'>");
  }

  public void endForm() {
    out.print("</form>");
  }

  public void print(String s) {
    out.print(javaToHtml(s));
  }

  public void printNoBreak(String s) {
    s = javaToHtml(s);
    s = s.replaceAll(" ", "&nbsp;");
    out.print(s);
  }

  public void br() {
    out.print("<br>");
  }

  public void focusAnchor() {
    out.print("<a name=\"focus\"></a>");
  }

  public void bold(String text) {
    out.print("<b>");
    print(text);
    out.print("</b>");
  }

  public void pBold(String text) {
    out.print("<p><b>");
    print(text);
    out.print("</b></p>");
  }

  public void pItalic(String text) {
    out.print("<p><i>");
    print(text);
    out.print("</i></p>");
  }

  public void p(String text) {
    out.print("<p>");
    print(text);
    out.print("</p>");
  }

  public void p() {
    out.print("<p>");
  }

  public void raw(String s) {
    out.print(s);
  }

  public void startTable() {
    startTable(0);
  }

  public void startTable(int border) {
    startTable(border, 0, 0);
  }

  public void startTable(int border, int cellspacing, int cellpadding) {
    startTable(border, cellspacing, cellpadding, null);
  }

  public void startTable(int border, int cellspacing, int cellpadding, String width) {
    out.print("<table");
    if (border >= 0) {
      out.print(" border=" + border);
    }
    if (cellspacing >= 0) {
      out.print(" cellspacing=" + cellspacing);
    }
    if (cellpadding >= 0) {
      out.print(" cellpadding=" + cellpadding);
    }
    if (width != null) {
      out.print(" width='" + width + "'");
    }
    out.print(">");
  }

  public void endTable() {
    out.print("</table>");
  }

  public void startTableRow() {
    out.print("<tr>");
  }

  public void endTableRow() {
    out.println("</tr>");
  }

  public void spacingRow(int columnCount) {
    out.print("<tr>");
    out.print("<td colspan=\"" + columnCount + "\">");
    out.print("&nbsp;");
    out.print("</td>");
    out.println("</tr>");
  }

  public void startTableCell() {
    startTableCell(1, 1);
  }

  public void startTableCell(int rows, int cols) {
    startTableCell(rows, cols, null);
  }

  public void startTableCell(int rows, int cols, String color) {
    out.print("<td");
    if (rows > 1) {
      out.print(" rowspan=" + rows);
    }
    if (cols > 1) {
      out.print(" colspan=" + cols);
    }
    if (color != null) {
      out.print(" bgcolor='#" + color + "'");
    }
    out.print(">");
  }

  public void endTableCell() {
    out.print("</td>");
  }

  public void tableCell(String content) {
    tableCell(content, 1, 1);
  }

  public void tableCell(String content, int rows, int cols) {
    tableCell(content, 1, 1, null);
  }

  public void tableCell(String content, int rows, int cols, String color) {
    startTableCell(rows, cols, color);
    if (!StringUtility.hasText(content)) {
      out.print("&nbsp;");
    }
    else {
      print(content);
    }
    endTableCell();
  }

  public void tableHeaderCell(String content) {
    out.print("<th>");
    if (!StringUtility.hasText(content)) {
      out.print("&nbsp;");
    }
    else {
      print(content);
    }
    out.print("</th>");
  }

  public String javaToHtml(String s) {
    if (s == null) {
      return "";
    }
    s = s.replaceAll("<", "&lt;");
    s = s.replaceAll(">", "&gt;");
    return s;
  }

}
