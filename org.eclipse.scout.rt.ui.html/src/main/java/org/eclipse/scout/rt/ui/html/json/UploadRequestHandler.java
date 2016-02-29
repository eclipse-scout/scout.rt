/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.HttpSessionHelper;
import org.eclipse.scout.rt.ui.html.ISessionStore;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiRunContexts;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler contributes to the {@link UiServlet} as the POST handler for /upload
 */
@Order(5030)
public class UploadRequestHandler extends AbstractUiServletRequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(UploadRequestHandler.class);

  private static final Pattern PATTERN_UPLOAD_ADAPTER_RESOURCE_PATH = Pattern.compile("^/upload/([^/]*)/([^/]*)$");

  private final HttpSessionHelper m_httpSessionHelper = BEANS.get(HttpSessionHelper.class);
  private final JsonRequestHelper m_jsonRequestHelper = BEANS.get(JsonRequestHelper.class);

  @Override
  public boolean handlePost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    //serve only /upload
    String pathInfo = req.getPathInfo();
    Matcher matcher = PATTERN_UPLOAD_ADAPTER_RESOURCE_PATH.matcher(pathInfo);
    if (!matcher.matches()) {
      return false;
    }

    final String uiSessionId = matcher.group(1);
    final String targetAdapterId = matcher.group(2);

    // Check if is really a file upload
    if (!ServletFileUpload.isMultipartContent(req)) {
      return false;
    }

    final long startNanos = System.nanoTime();
    if (LOG.isDebugEnabled()) {
      LOG.debug("File upload started");
    }

    try {
      // Get and validate existing UI session
      IUiSession uiSession = getUiSession(req, uiSessionId);
      if (uiSession == null) {
        throw new IllegalStateException("Could not resolve UI session with ID " + uiSessionId);
      }

      // Touch the session
      uiSession.touch();
      final IUiSession uiSession1 = uiSession;

      // Associate subsequent processing with the uiSession.
      UiRunContexts.copyCurrent()
          .withSession(uiSession1)
          .run(new IRunnable() {
            @Override
            public void run() throws Exception {
              handleUploadFileRequest(IUiSession.CURRENT.get(), req, resp, targetAdapterId);
            }
          }, DefaultExceptionTranslator.class);
    }
    catch (Exception e) {
      LOG.error("Unexpected error while handling multipart upload request", e);
      writeJsonResponse(resp, m_jsonRequestHelper.createUnrecoverableFailureResponse());
    }
    finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File upload completed in {} ms", StringUtility.formatNanos(System.nanoTime() - startNanos));
      }
    }
    return true;
  }

  protected void handleUploadFileRequest(IUiSession uiSession, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String targetAdapterId) throws IOException, FileUploadException {
    IBinaryResourceConsumer binaryResourceConsumer = resolveJsonAdapter(uiSession, targetAdapterId);
    if (binaryResourceConsumer == null) {
      //Request was already processed and adapter does not exist anymore;
      return;
    }

    // Read uploaded data
    Map<String, String> uploadProperties = new HashMap<String, String>();
    List<BinaryResource> uploadResources = new ArrayList<>();
    readUploadData(httpServletRequest, binaryResourceConsumer.getMaximumBinaryResourceUploadSize(), uploadProperties, uploadResources);

    if (uploadProperties.containsKey("legacyFormTextPlainAnswer")) {
      httpServletResponse.setContentType("text/plain");
    }

    // GUI requests for the same session must be processed consecutively
    uiSession.uiSessionLock().lock();
    try {
      if (uiSession.isDisposed()) {
        writeJsonResponse(httpServletResponse, m_jsonRequestHelper.createSessionTimeoutResponse());
        return;
      }
      JSONObject jsonResp = uiSession.processFileUpload(httpServletRequest, httpServletResponse, binaryResourceConsumer, uploadResources, uploadProperties);
      if (jsonResp == null) {
        jsonResp = m_jsonRequestHelper.createEmptyResponse();
      }
      writeJsonResponse(httpServletResponse, jsonResp);
    }
    finally {
      uiSession.uiSessionLock().unlock();
    }
  }

  protected void readUploadData(HttpServletRequest httpReq, long maxSize, Map<String, String> uploadProperties, List<BinaryResource> uploadResources) throws FileUploadException, IOException {
    ServletFileUpload upload = new ServletFileUpload();
    upload.setHeaderEncoding(StandardCharsets.UTF_8.name());
    upload.setSizeMax(maxSize);
    for (FileItemIterator it = upload.getItemIterator(httpReq); it.hasNext();) {
      FileItemStream item = it.next();
      String name = item.getFieldName();
      InputStream stream = item.openStream();

      if (item.isFormField()) {
        // Handle non-file fields (interpreted as properties)
        uploadProperties.put(name, Streams.asString(stream, StandardCharsets.UTF_8.name()));
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
        // Info: we cannot set the charset property for uploaded files here, because we simply don't know it.
        // the only thing we could do is to guess the charset (encoding) by reading the byte contents of
        // uploaded text files (for binary file types the encoding is not relevant). However: currently we
        // do not set the charset at all.
        uploadResources.add(new BinaryResource(filename, contentType, content));
      }
    }
  }

  protected IUiSession getUiSession(HttpServletRequest req, String uiSessionId) throws ServletException, IOException {
    HttpSession httpSession = req.getSession();
    ISessionStore sessionStore = m_httpSessionHelper.getSessionStore(httpSession);
    return sessionStore.getUiSession(uiSessionId);
  }

  /**
   * Returns the {@link IBinaryResourceConsumer} that is registered to the specified session under the given adapter ID.
   * If the adapter could not be found, or the adapter is not a {@link IBinaryResourceConsumer}, a runtime exception is
   * thrown.
   */
  protected IBinaryResourceConsumer resolveJsonAdapter(IUiSession uiSession, String targetAdapterId) {
    // Resolve adapter
    if (!StringUtility.hasText(targetAdapterId)) {
      throw new IllegalArgumentException("Missing target adapter ID");
    }
    IJsonAdapter<?> jsonAdapter = uiSession.getJsonAdapter(targetAdapterId);
    if (jsonAdapter == null) {
      return null;
    }
    if (jsonAdapter instanceof IBinaryResourceConsumer) {
      return (IBinaryResourceConsumer) jsonAdapter;
    }
    throw new IllegalStateException("Invalid adapter for ID " + targetAdapterId + " (unexpected type: " + jsonAdapter.getClass().getName() + ")");
  }

  /**
   * Writes the given {@link JSONObject} into the given {@link ServletResponse}.
   */
  protected void writeJsonResponse(ServletResponse servletResponse, JSONObject jsonObject) throws IOException {
    m_jsonRequestHelper.writeResponse(servletResponse, jsonObject);
  }
}
