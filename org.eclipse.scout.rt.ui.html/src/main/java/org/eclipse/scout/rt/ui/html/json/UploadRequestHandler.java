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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.eclipse.scout.commons.Encoding;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.json.JSONObject;
import org.slf4j.MDC;

/**
 * This handler contributes to the {@link UiServlet} as the POST handler for /upload
 */
@Order(30)
public class UploadRequestHandler extends AbstractJsonRequestHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(UploadRequestHandler.class);

  private static final Pattern PATTERN_UPLOAD_ADAPTER_RESOURCE_PATH = Pattern.compile("^/upload/([^/]*)/([^/]*)$");

  @Override
  public boolean handlePost(UiServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    //serve only /upload
    String pathInfo = req.getPathInfo();
    Matcher matcher = PATTERN_UPLOAD_ADAPTER_RESOURCE_PATH.matcher(pathInfo);
    if (!matcher.matches()) {
      return false;
    }
    String uiSessionId = matcher.group(1);
    String targetAdapterId = matcher.group(2);

    // Check if is really a file upload
    if (!ServletFileUpload.isMultipartContent(req)) {
      return false;
    }

    try {
      long start = System.nanoTime();
      String oldScoutSessionId = MDC.get(MDC_SCOUT_SESSION_ID);
      String oldScoutUiSessionId = MDC.get(MDC_SCOUT_UI_SESSION_ID);
      if (LOG.isDebugEnabled()) {
        LOG.debug("started");
      }

      // Resolve session
      MDC.put(MDC_SCOUT_UI_SESSION_ID, uiSessionId);
      IUiSession uiSession = resolveUiSession(req, uiSessionId);
      IBinaryResourceConsumer binaryResourceConsumer = resolveJsonAdapter(uiSession, targetAdapterId);

      // Read uploaded data
      Map<String, String> uploadProperties = new HashMap<String, String>();
      List<BinaryResource> uploadResources = new ArrayList<>();
      readUploadData(req, binaryResourceConsumer.getMaximumBinaryResourceUploadSize(), uploadProperties, uploadResources);

      if (uploadProperties.containsKey("legacyFormTextPlainAnswer")) {
        resp.setContentType("text/plain");
      }

      try {
        MDC.put(MDC_SCOUT_UI_SESSION_ID, uiSession.getUiSessionId());
        MDC.put(MDC_SCOUT_SESSION_ID, uiSession.getClientSessionId());

        // GUI requests for the same session must be processed consecutively
        uiSession.uiSessionLock().lock();
        try {
          if (uiSession.isDisposed() || uiSession.currentJsonResponse() == null) {
            writeResponse(resp, createSessionTimeoutResponse());
            return true;
          }
          JSONObject jsonResp = uiSession.processFileUpload(req, resp, binaryResourceConsumer, uploadResources, uploadProperties);
          if (jsonResp == null) {
            jsonResp = createEmptyResponse();
          }
          writeResponse(resp, jsonResp);
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
      writeResponse(resp, createUnrecoverableFailureResponse());
    }
    return true;
  }

  protected void readUploadData(HttpServletRequest httpReq, long maxSize, Map<String, String> uploadProperties, List<BinaryResource> uploadResources) throws FileUploadException, IOException, ProcessingException {
    ServletFileUpload upload = new ServletFileUpload();
    upload.setHeaderEncoding(Encoding.UTF_8);
    upload.setSizeMax(maxSize);
    for (FileItemIterator it = upload.getItemIterator(httpReq); it.hasNext();) {
      FileItemStream item = it.next();
      String name = item.getFieldName();
      InputStream stream = item.openStream();

      if (item.isFormField()) {
        // Handle non-file fields (interpreted as properties)
        uploadProperties.put(name, Streams.asString(stream, Encoding.UTF_8));
      }
      else {
        // Handle files
        String filename = item.getName();
        if (StringUtility.hasText(filename)) {
          String[] parts = StringUtility.split(filename, "[/\\\\]");
          filename = parts[parts.length - 1];
        }
        String contentType = item.getContentType();
        byte[] content = IOUtility.getContent(stream);
        uploadResources.add(new BinaryResource(filename, contentType, content));
      }
    }
  }

  protected IUiSession resolveUiSession(HttpServletRequest httpReq, String uiSessionId) {
    if (!StringUtility.hasText(uiSessionId)) {
      throw new IllegalArgumentException("Missing UI session ID.");
    }
    HttpSession httpSession = httpReq.getSession();
    IUiSession uiSession = (IUiSession) httpSession.getAttribute(IUiSession.HTTP_SESSION_ATTRIBUTE_PREFIX + uiSessionId);
    if (uiSession == null) {
      throw new IllegalStateException("Could not resolve UI session with ID " + uiSessionId);
    }
    uiSession.touch();
    return uiSession;
  }

  /**
   * Returns the {@link IBinaryResourceConsumer} that is registered to the specified
   * session under the given adapter ID. If the adapter could not be found, or the
   * adapter is not a {@link IBinaryResourceConsumer}, a runtime exception is thrown.
   */
  protected IBinaryResourceConsumer resolveJsonAdapter(IUiSession uiSession, String targetAdapterId) {
    // Resolve adapter
    if (!StringUtility.hasText(targetAdapterId)) {
      throw new IllegalArgumentException("Missing target adapter ID");
    }
    IJsonAdapter<?> jsonAdapter = uiSession.getJsonAdapter(targetAdapterId);
    if (!(jsonAdapter instanceof IBinaryResourceConsumer)) {
      throw new IllegalStateException("Invalid adapter for ID " + targetAdapterId + (jsonAdapter == null ? "" : " (unexpected type)"));
    }
    return (IBinaryResourceConsumer) jsonAdapter;
  }
}
