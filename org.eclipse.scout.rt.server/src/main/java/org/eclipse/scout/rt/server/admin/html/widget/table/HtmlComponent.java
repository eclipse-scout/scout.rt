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
package org.eclipse.scout.rt.server.admin.html.widget.table;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.admin.html.AbstractHtmlAction;

@SuppressWarnings("bsiRulesDefinition:htmlInString")
public class HtmlComponent {
  protected String m_baseURL;
  protected HttpServletRequest m_req;
  protected HttpServletResponse m_res;
  protected StringWriter m_writer;
  protected PrintWriter m_out;
  // action list
  protected Map<String, AbstractHtmlAction> m_actionMap;
  // action invokation feedback
  protected AbstractHtmlAction m_invokedAction;

  public HtmlComponent(HttpServletRequest req, HttpServletResponse res) {
    m_req = req;
    m_res = res;
    m_baseURL = m_req.getContextPath() + m_req.getServletPath();
    m_writer = new StringWriter();
    m_out = new PrintWriter(m_writer, true);
    m_actionMap = new HashMap<String, AbstractHtmlAction>();
  }

  public HtmlComponent(HtmlComponent other) {
    m_req = other.m_req;
    m_res = other.m_res;
    m_baseURL = other.m_baseURL;
    m_invokedAction = other.m_invokedAction;
    //
    m_writer = new StringWriter();
    m_out = new PrintWriter(m_writer, true);
    m_actionMap = new HashMap<String, AbstractHtmlAction>();
  }

  public void append(HtmlComponent other) {
    m_out.print(other.getProducedHtml());
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
    m_out.print("<a href='" + u + "'>");
  }

  public void endLinkAction() {
    m_out.print("</a>");
  }

  public void startForm(AbstractHtmlAction action) {
    String actionId = buildActionId(action);
    m_actionMap.put(actionId, action);
    String u = m_baseURL;
    m_out.print("<form action='" + u + "' method='get'>");
    m_out.print("<input type='hidden' name='actionId' value='" + actionId + "'>");
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
    m_out.print("<textarea rows=" + rows + " cols=" + cols + " name='" + fieldName + "'>");
    print(value);
    m_out.print("</textarea>");
  }

  public void startListBox(String fieldName, int size, boolean actionOnClick) {
    m_out.print("<select name=\"" + fieldName + "\" size=\"" + size + "\"");
    if (actionOnClick) {
      m_out.print(" onchange=\"javascript:window.location.href='" + m_baseURL + "?actionId='+this.value+'#focus';\"");
    }
    m_out.print(">");
  }

  public void radioBoxOption(String fieldName, String text, AbstractHtmlAction action, boolean selected) {
    String actionId = buildActionId(action);
    m_actionMap.put(actionId, action);
    m_out.print("<input type=\"radio\" name=\"" + fieldName + "\" value=\"" + actionId + "\" " + (selected ? " checked=\"checked\" " : "") + " onchange=\"javascript:window.location.href='" + m_baseURL + "?actionId='+this.value+'#focus';\">"
        + javaToHtml(text) + "</input>");
  }

  /**
   * normaly used with {@link #startListBox(int, FALSE)}
   */
  public void listBoxOption(String text, String value, boolean selected) {
    m_out.print("<option" + (selected ? " selected" : "") + " value=\"" + value + "\">" + javaToHtml(text) + "</option>");
  }

  /**
   * normaly used with {@link #startListBox(int, TRUE)}
   */
  public void listBoxOption(String text, AbstractHtmlAction action, boolean selected) {
    String actionId = buildActionId(action);
    m_actionMap.put(actionId, action);
    m_out.print("<option" + (selected ? " selected" : "") + " value=\"" + actionId + "\">" + javaToHtml(text) + "</option>");
  }

  public void endListBox() {
    m_out.print("</select>");
  }

  public void formSubmit(String value) {
    m_out.print("<input type=submit value='" + value + "'>");
  }

  public void endForm() {
    m_out.print("</form>");
  }

  public void print(String s) {
    m_out.print(javaToHtml(s));
  }

  public void printNoBreak(String s) {
    s = javaToHtml(s);
    s = s.replaceAll(" ", "&nbsp;");
    m_out.print(s);
  }

  public void br() {
    m_out.print("<br>");
  }

  public void focusAnchor() {
    m_out.print("<a name=\"focus\"></a>");
  }

  public void bold(String text) {
    m_out.print("<b>");
    print(text);
    m_out.print("</b>");
  }

  public void pBold(String text) {
    m_out.print("<p><b>");
    print(text);
    m_out.print("</b></p>");
  }

  public void pItalic(String text) {
    m_out.print("<p><i>");
    print(text);
    m_out.print("</i></p>");
  }

  public void p(String text) {
    m_out.print("<p>");
    print(text);
    m_out.print("</p>");
  }

  public void p() {
    m_out.print("<p>");
  }

  public void raw(String s) {
    m_out.print(s);
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
    m_out.print("<table");
    if (border >= 0) {
      m_out.print(" border=" + border);
    }
    if (cellspacing >= 0) {
      m_out.print(" cellspacing=" + cellspacing);
    }
    if (cellpadding >= 0) {
      m_out.print(" cellpadding=" + cellpadding);
    }
    if (width != null) {
      m_out.print(" width='" + width + "'");
    }
    m_out.print(">");
  }

  public void endTable() {
    m_out.print("</table>");
  }

  public void startTableRow() {
    m_out.print("<tr>");
  }

  public void endTableRow() {
    m_out.println("</tr>");
  }

  public void spacingRow(int columnCount) {
    m_out.print("<tr>");
    m_out.print("<td colspan=\"" + columnCount + "\">");
    m_out.print("&nbsp;");
    m_out.print("</td>");
    m_out.println("</tr>");
  }

  public void startTableCell() {
    startTableCell(1, 1);
  }

  public void startTableCell(int rows, int cols) {
    startTableCell(rows, cols, null);
  }

  public void startTableCell(int rows, int cols, String color) {
    m_out.print("<td");
    if (rows > 1) {
      m_out.print(" rowspan=" + rows);
    }
    if (cols > 1) {
      m_out.print(" colspan=" + cols);
    }
    if (color != null) {
      m_out.print(" bgcolor='#" + color + "'");
    }
    m_out.print(">");
  }

  public void endTableCell() {
    m_out.print("</td>");
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
      m_out.print("&nbsp;");
    }
    else {
      print(content);
    }
    endTableCell();
  }

  public void tableHeaderCell(String content) {
    m_out.print("<th>");
    if (!StringUtility.hasText(content)) {
      m_out.print("&nbsp;");
    }
    else {
      print(content);
    }
    m_out.print("</th>");
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
