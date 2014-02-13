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
package org.eclipse.scout.rt.ui.json;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonServlet extends HttpServlet {

  private static final String SESSION_ATTR = IJsonEnvironment.class.getName();

  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonServlet.class);
  private static Class<? extends IJsonEnvironment> s_environmentClass;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("doPost");
    try {
      HttpSession session = req.getSession();
      IJsonEnvironment env = (IJsonEnvironment) session.getAttribute(SESSION_ATTR);
      if (env == null) {
        env = createEnvironment();
        session.setAttribute(SESSION_ATTR, env);
      }
      JSONObject jsonReq = toJSON(req);
      JSONObject jsonResp = env.processRequest(jsonReq);
      String data = jsonResp.toString();
      resp.setContentLength(data.length());
      resp.setContentType("application/json");
      resp.getOutputStream().print(data);
    }
    catch (ProcessingException e) {
      resp.getWriter().print("ERROR: " + e.getMessage());
    }
  }

  private JSONObject toJSON(HttpServletRequest req) throws ProcessingException {
    try {
      String jsonData = IOUtility.getContent(req.getReader());
      if (StringUtility.isNullOrEmpty(jsonData)) {
        jsonData = "{}"; // TODO
      }
      return new JSONObject(jsonData);
    }
    catch (IOException | JSONException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  private IJsonEnvironment createEnvironment() throws ProcessingException {
    IJsonEnvironment env = null;
    try {
      env = s_environmentClass.newInstance();
      env.init();
      return env;
    }
    catch (InstantiationException | IllegalAccessException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("doGet");
    doPost(req, resp);
  }

  public static void setEnvironmentClass(Class<? extends IJsonEnvironment> environmentClass) {
    s_environmentClass = environmentClass;
  }

}
