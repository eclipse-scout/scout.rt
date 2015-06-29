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
package org.eclipse.scout.rt.ui.html.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.IServletRequestInterceptor;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.json.JSONObject;
import org.slf4j.MDC;

/**
 * This interceptor contributes to the {@link UiServlet} as the POST handler for /upload
 */
@Order(30)
public class UploadRequestInterceptor extends AbstractJsonRequestInterceptor implements IServletRequestInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(UploadRequestInterceptor.class);

  private static final String PROP_UI_SESSION_ID = "uiSessionId";
  private static final String PROP_TARGET = "target";

  @Override
  public boolean interceptGet(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return false;
  }

  @Override
  public boolean interceptPost(UiServlet servlet, HttpServletRequest httpReq, HttpServletResponse httpResp) throws ServletException, IOException {
    //serve only /upload
    String pathInfo = httpReq.getPathInfo();
    if (CompareUtility.notEquals(pathInfo, "/upload")) {
      return false;
    }

    // Check if is really a file upload
    if (!ServletFileUpload.isMultipartContent(httpReq)) {
      return false;
    }

    try {
      long start = System.nanoTime();
      String oldScoutSessionId = MDC.get(MDC_SCOUT_SESSION_ID);
      String oldScoutUiSessionId = MDC.get(MDC_SCOUT_UI_SESSION_ID);
      if (LOG.isDebugEnabled()) {
        LOG.debug("started");
      }

      // Read uploaded data
      Map<String, String> uploadProperties = new HashMap<String, String>();
      List<BinaryResource> uploadResources = new ArrayList<>();
      readUploadData(httpReq, uploadProperties, uploadResources);

      // Resolve session
      String uiSessionId = uploadProperties.get(PROP_UI_SESSION_ID);
      MDC.put(MDC_SCOUT_UI_SESSION_ID, uiSessionId);
      IUiSession uiSession = resolveUiSession(httpReq, uiSessionId);

      try {
        MDC.put(MDC_SCOUT_UI_SESSION_ID, uiSession.getUiSessionId());
        MDC.put(MDC_SCOUT_SESSION_ID, uiSession.getClientSessionId());

        // GUI requests for the same session must be processed consecutively
        uiSession.uiSessionLock().lock();
        try {
          if (uiSession.isDisposed() || uiSession.currentJsonResponse() == null) {
            writeResponse(httpResp, createSessionTerminatedResponse());
            return true;
          }
          JSONObject jsonResp = uiSession.processFileUpload(httpReq, uploadProperties.get(PROP_TARGET), uploadResources, uploadProperties);
          if (jsonResp == null) {
            jsonResp = createEmptyResponse();
          }
          writeResponse(httpResp, jsonResp);
        }
        finally {
          uiSession.uiSessionLock().unlock();
        }
        if (LOG.isDebugEnabled()) {
          LOG.debug("completed in " + StringUtility.formatNanos(System.nanoTime() - start) + " ms");
        }
      }
      finally {
        if (oldScoutSessionId != null) {
          MDC.put(MDC_SCOUT_SESSION_ID, oldScoutSessionId);
        }
        else {
          MDC.remove(MDC_SCOUT_SESSION_ID);
        }
        if (oldScoutUiSessionId != null) {
          MDC.put(MDC_SCOUT_UI_SESSION_ID, oldScoutUiSessionId);
        }
        else {
          MDC.remove(MDC_SCOUT_UI_SESSION_ID);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Unexpected error while handling multipart upload request", e);
      writeResponse(httpResp, createUnrecoverableFailureResponse());
    }
    return true;
  }

  protected void readUploadData(HttpServletRequest httpReq, Map<String, String> uploadProperties, List<BinaryResource> uploadResources) throws FileUploadException, IOException, ProcessingException {
    ServletFileUpload upload = new ServletFileUpload();
    for (FileItemIterator it = upload.getItemIterator(httpReq); it.hasNext();) {
      FileItemStream item = it.next();
      String name = item.getFieldName();
      InputStream stream = item.openStream();

      if (item.isFormField()) {
        // Handle non-file fields (interpreted as properties)
        uploadProperties.put(name, Streams.asString(stream));
      }
      else {
        // Handle files
        String filename = item.getName();
        String contentType = item.getContentType();
        byte[] content = IOUtility.getContent(stream);
        uploadResources.add(new BinaryResource(filename, contentType, content));
      }
    }
  }

  protected IUiSession resolveUiSession(HttpServletRequest httpReq, String uiSessionId) {
    if (!StringUtility.hasText(uiSessionId)) {
      throw new IllegalArgumentException("Missing property '" + PROP_UI_SESSION_ID + "'");
    }
    HttpSession httpSession = httpReq.getSession();
    IUiSession uiSession = (IUiSession) httpSession.getAttribute(IUiSession.HTTP_SESSION_ATTRIBUTE_PREFIX + uiSessionId);
    if (uiSession == null) {
      throw new IllegalStateException("Could not resolve UI session with ID " + uiSessionId);
    }
    uiSession.touch();
    return uiSession;
  }
}
