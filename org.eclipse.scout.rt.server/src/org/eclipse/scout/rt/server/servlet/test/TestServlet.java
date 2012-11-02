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
package org.eclipse.scout.rt.server.servlet.test;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.security.auth.Subject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.http.servletfilter.HttpServletEx;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.rt.shared.services.common.test.BasicTestContext;
import org.eclipse.scout.rt.shared.services.common.test.ITest;
import org.eclipse.scout.rt.shared.services.common.test.TestStatus;
import org.eclipse.scout.rt.shared.services.common.test.TestUtility;
import org.eclipse.scout.service.SERVICES;

/**
 * Typically this servlet is registered in the
 * org.eclipse.scout.commons.servlets extension in your project. It must provide
 * the init-parameters
 * <ul>
 * <li>session</li>
 * <li>runAs</li>
 * </ul>
 */
public class TestServlet extends HttpServletEx {
  private static final long serialVersionUID = 1L;

  private Class<? extends IServerSession> m_serverSessionClass;
  private String m_runAs;

  @SuppressWarnings("unchecked")
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    String qname = config.getInitParameter("session");
    if (qname == null) {
      throw new ServletException("Expected init-param \"session\"");
    }
    String runAs = config.getInitParameter("runAs");
    if (runAs == null) {
      throw new ServletException("Expected init-param \"runAs\"");
    }
    //
    int i = qname.lastIndexOf('.');
    try {
      m_serverSessionClass = (Class<? extends IServerSession>) Platform.getBundle(qname.substring(0, i)).loadClass(qname);
    }
    catch (ClassNotFoundException e) {
      throw new ServletException("Loading class " + qname, e);
    }
    m_runAs = runAs;
  }

  @Override
  public void destroy() {
    m_serverSessionClass = null;
    m_runAs = null;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/html");
    final BasicTestContext ctx = new BasicTestContext();
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(m_runAs));
    IServerSession serverSession;
    try {
      serverSession = SERVICES.getService(IServerSessionRegistryService.class).newServerSession(m_serverSessionClass, subject);
    }
    catch (ProcessingException e) {
      e.printStackTrace(new PrintStream(res.getOutputStream(), true));
      return;
    }
    IStatus status = new ServerJob("Test", serverSession, subject) {
      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        ITest[] tests = SERVICES.getServices(ITest.class);
        TestUtility.runTests(ctx, tests);
        return Status.OK_STATUS;
      }
    }.runNow(new NullProgressMonitor());
    if (!status.isOK()) {
      status.getException().printStackTrace(new PrintStream(res.getOutputStream(), true));
    }
    //
    StringBuffer records = new StringBuffer();
    for (TestStatus s : ctx.getStatusList()) {
      getTestRecordTemplate();
      String severityColor;
      switch (s.getSeverity()) {
        case IProcessingStatus.INFO: {
          severityColor = "00ff00";
          break;
        }
        case IProcessingStatus.WARNING: {
          severityColor = "ffff00";
          break;
        }
        case IProcessingStatus.ERROR: {
          severityColor = "ff0000";
          break;
        }
        case IProcessingStatus.FATAL: {
          severityColor = "880000";
          break;
        }
        default: {
          severityColor = "ffffff";
        }
      }
      String product = StringUtility.htmlEncode(s.getProduct(), true);
      String title = StringUtility.htmlEncode(s.getTitle(), true);
      String subTitle = StringUtility.htmlEncode(s.getSubTitle(), true);
      String message = StringUtility.htmlEncode(s.getMessage(), true);
      String detail;
      if (s.getCause() != null) {
        StringWriter w = new StringWriter();
        s.getCause().printStackTrace(new PrintWriter(w, true));
        detail = "<textarea rows=\"4\" cols=\"80\">" + StringUtility.htmlEncode(w.toString(), true) + "</textarea>";
        message += "<br>" + detail;
      }
      records.append(MessageFormat.format(
          getTestRecordTemplate(),
          severityColor,
          product,
          title,
          subTitle,
          message
          ));
    }
    //
    String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
    String page = MessageFormat.format(
        getPageTemplate(),
        date,
        ctx.getSeverityCount(TestStatus.INFO),
        ctx.getSeverityCount(TestStatus.WARNING),
        ctx.getSeverityCount(TestStatus.ERROR),
        ctx.getSeverityCount(TestStatus.FATAL),
        records.toString()
        );
    //
    res.getOutputStream().print(page);
  }

  /**
   * @return the template for the html page containing the variables {0} =
   *         date/time of test {1} = ok count {2} = warning count {3} = error
   *         count {4} = fatal count {5} = list of all test statis
   */
  protected String getPageTemplate() {
    return "<html><body>\n" +
        "<h1>Automated Test Run</h1>\n" +
        "<h3>{0}</h3>\n" +
        "<table border=\"0\">\n" +
        "<tr><td>{1}</td><td>Success</td></tr>\n" +
        "<tr><td>{2}</td><td>Warnings</td></tr>\n" +
        "<tr><td>{3}</td><td>Errors</td></tr>\n" +
        "<tr><td>{4}</td><td>Fatals</td></tr>\n" +
        "</table>\n" +
        "<hr>\n" +
        "<table border=\"1\">\n" +
        "{5}" +
        "</table>\n" +
        "</body></html>";
  }

  /**
   * @return the template of a test status record {0} = severity color
   *         (Ok=00ff00 (green), Warning=ffff00 (yellow), Error=ff0000 (red),
   *         Fatal=880000 (dark red)) {1} = product {2} = title {3} = subTitle
   *         {4} = message
   */
  protected String getTestRecordTemplate() {
    return "<tr>\n" +
        "<td bgcolor=\"{0}\" valign=\"top\">\n" +
        "<b>{1}</b>\n" +
        "</td>\n" +
        "<td valign=\"top\">\n" +
        "<b>{2}</b>\n" +
        "</td>\n" +
        "<td valign=\"top\">\n" +
        "{3}\n" +
        "</td>\n" +
        "<td valign=\"top\">\n" +
        "{4}\n" +
        "</td>\n" +
        "</tr>\n";
  }

}
