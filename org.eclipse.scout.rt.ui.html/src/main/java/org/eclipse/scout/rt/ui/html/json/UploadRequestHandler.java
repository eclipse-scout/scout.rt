/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.resource.MimeTypes;
import org.eclipse.scout.rt.platform.security.MalwareScanner;
import org.eclipse.scout.rt.platform.security.RejectedResourceException;
import org.eclipse.scout.rt.platform.security.UnsafeResourceException;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.eclipse.scout.rt.ui.html.UiSession;
import org.eclipse.scout.rt.ui.html.logging.IUiRunContextDiagnostics;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.eclipse.scout.rt.ui.html.res.IUploadable;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * This handler contributes to the {@link UiServlet} as the POST handler for /upload.<br>
 * See Session.uploadFiles() method in {@code Session.ts}.
 */
@Order(4520)
public class UploadRequestHandler extends AbstractUiServletRequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(UploadRequestHandler.class);

  /**
   * See Sessions.js. If no filename is set, this filename will be set instead, to ensure no browser sets a default
   * filename like 'blob'.
   */
  private static final String EMPTY_UPLOAD_FILENAME = "*empty*";

  private static final Pattern PATTERN_UPLOAD_ADAPTER_RESOURCE_PATH = Pattern.compile("^/upload/([^/]*)/([^/]*)$");

  public static final Set<String> DEFAULT_VALID_FILE_EXTENSIONS = Stream.of("avi", "bmp", "docx", "dotx", "gif", "html", "jpg", "jpeg", "log", "m2v", "mkv", "mov", "mp3", "mp4", "mpg", "m4p", "oga", "ogv", "pdf", "png", "potx", "ppsx",
      "pptx", "sldx", "svg", "thmx", "tif", "tiff", "txt", "vcard", "vcf", "vcs", "xlsx", "xltx").collect(Collectors.toSet());

  private final HttpCacheControl m_httpCacheControl = BEANS.get(HttpCacheControl.class);
  private final JsonRequestHelper m_jsonRequestHelper = BEANS.get(JsonRequestHelper.class);

  @Override
  public boolean handlePost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    //serve only /upload
    String pathInfo = req.getPathInfo();
    Matcher matcher = PATTERN_UPLOAD_ADAPTER_RESOURCE_PATH.matcher(pathInfo);
    if (!matcher.matches()) {
      return false;
    }

    final String uiSessionId = matcher.group(1);
    final String targetAdapterId = matcher.group(2);

    // Check if is really a file upload
    if (!isMultipartContent(req)) {
      return false;
    }

    final long startNanos = System.nanoTime();
    if (LOG.isDebugEnabled()) {
      LOG.debug("File upload started");
    }

    // disable caching
    m_httpCacheControl.checkAndSetCacheHeaders(req, resp, null);

    try {
      // Get and validate existing UI session
      final IUiSession uiSession = UiSession.get(req, uiSessionId);
      if (uiSession == null) {
        throw new IllegalStateException("Could not resolve UI session with ID " + uiSessionId);
      }

      // Touch the session
      uiSession.touch();

      // Associate subsequent processing with the uiSession.
      RunContexts.copyCurrent()
          .withThreadLocal(IUiSession.CURRENT, uiSession)
          .withDiagnostics(BEANS.all(IUiRunContextDiagnostics.class))
          .run(() -> handleUploadFileRequest(IUiSession.CURRENT.get(), req, resp, targetAdapterId), DefaultExceptionTranslator.class);
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

  protected boolean isMultipartContent(HttpServletRequest request) {
    String contentType = request.getContentType();
    if (contentType == null) {
      return false;
    }
    return contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/");
  }

  protected void handleUploadFileRequest(IUiSession uiSession, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String targetAdapterId) throws IOException, ServletException {
    // If client sent ACK#, cleanup response history accordingly
    uiSession.confirmResponseProcessed(getAckSequenceNo(httpServletRequest));

    IUploadable uploadable = resolveJsonAdapter(uiSession, targetAdapterId);
    if (uploadable == null) {
      // Request was already processed and adapter does not exist anymore
      return;
    }
    if (httpServletRequest.getParameter("legacy") != null) {
      httpServletResponse.setContentType("text/plain");
    }

    // Read uploaded data
    // GUI requests for the same session must be processed consecutively
    final ReentrantLock uiSessionLock = uiSession.uiSessionLock();
    uiSessionLock.lock();
    try {
      if (uiSession.isDisposed()) {
        writeJsonResponse(httpServletResponse, m_jsonRequestHelper.createSessionTimeoutResponse());
        return;
      }
      Map<String, String> uploadProperties = new HashMap<>();
      List<BinaryResource> uploadResources = new ArrayList<>();
      try {
        readUploadData(httpServletRequest, uploadable, uploadProperties, uploadResources);
      }
      catch (UnsafeResourceException e) { // NOSONAR
        // LOG is done in MalwareScanner, verifyFileSafety is the only method throwing this exception
        writeJsonResponse(httpServletResponse, m_jsonRequestHelper.createUnsafeUploadResponse());
        return;
      }
      catch (RejectedResourceException e) { // NOSONAR
        // verifyFileName and verifyFileIntegrity and maxFileCount are the only methods throwing this exception
        //mark resources as FAILED
        uploadResources = null;
        uploadProperties = null;
        //continue
      }
      if (uiSession.isDisposed()) {
        writeJsonResponse(httpServletResponse, m_jsonRequestHelper.createSessionTimeoutResponse());
        return;
      }
      JSONObject jsonResp = uiSession.processFileUpload(httpServletRequest, httpServletResponse, uploadable, uploadResources, uploadProperties);
      if (jsonResp == null) {
        jsonResp = m_jsonRequestHelper.createEmptyResponse();
      }
      writeJsonResponse(httpServletResponse, jsonResp);
    }
    finally {
      uiSessionLock.unlock();
    }
  }

  /**
   * @return the value of the HTTP header <code>X-Scout-#ACK</code> as {@link Long}, or <code>null</code> if value is
   *         not set or not a number.
   */
  protected Long getAckSequenceNo(HttpServletRequest req) {
    String ackSeqNoStr = req.getHeader("X-Scout-#ACK");
    if (ackSeqNoStr != null) {
      try {
        return Long.valueOf(ackSeqNoStr);
      }
      catch (NumberFormatException e) {
        // nop
      }
    }
    return null;
  }

  /**
   * Since 5.2 this performs a {@link MalwareScanner#scan(BinaryResource)} on the resources and throws a
   * {@link PlatformException} if some resources are unsafe
   */
  protected void readUploadData(HttpServletRequest httpReq, IUploadable uploadable, Map<String, String> uploadProperties, List<BinaryResource> uploadResources) throws IOException, ServletException {
    Set<String> validFileExtensions = getValidFileExtensionsFor(uploadable, uploadProperties);
    long maxFileCount = CONFIG.getPropertyValue(UiHtmlConfigProperties.MaxUploadFileCountProperty.class);
    int fileCount = 0;
    for (Part part : httpReq.getParts()) {
      fileCount++;
      //the first entry in an upload multipart is typically a "rowId" entry. be tolerant with one more file.
      if (maxFileCount > 0 && (fileCount - 1) > maxFileCount) {
        throw new RejectedResourceException("Too many files ({}).", fileCount);
      }
      String filename = part.getSubmittedFileName();
      if (StringUtility.hasText(filename)) {
        String[] parts = StringUtility.split(filename, "[/\\\\]");
        filename = parts[parts.length - 1];
      }
      if (EMPTY_UPLOAD_FILENAME.equals(filename)) {
        filename = null;
      }
      if (StringUtility.hasText(filename)) {
        String ext = FileUtility.getFileExtension(filename);
        if (ext != null) {
          ext = ext.toLowerCase(Locale.ROOT);
        }
        verifyFileName(validFileExtensions, filename, ext);
      }
      verifyMaximumUploadSize(uploadable, part);

      byte[] content;
      try (InputStream in = part.getInputStream()) {
        content = IOUtility.readBytes(in);
      }
      BinaryResource res = BinaryResources.create()
          .withFilename(filename)
          .withContentType(detectContentType(filename, part, content))
          .withContent(content)
          .build();
      verifyFileSafety(res);
      verifyFileIntegrity(res);

      // properties are sent as form fields without file name by UI (see Session.ts)
      if (StringUtility.isNullOrEmpty(part.getSubmittedFileName())) {
        // Handle non-file fields (interpreted as properties)
        String name = part.getName();
        uploadProperties.put(name, new String(content, StandardCharsets.UTF_8));
      }
      else {
        // Handle files
        // Info: we cannot set the charset property for uploaded files here, because we simply don't know it.
        // the only thing we could do is to guess the charset (encoding) by reading the byte contents of
        // uploaded text files (for binary file types the encoding is not relevant). However: currently we
        // do not set the charset at all.
        uploadResources.add(res);
      }
    }
  }

  /**
   * Detects the content type for an uploaded file.
   * <p>
   * The default implementation returns <code>null</code> if a filename is provided. In that case the content-type will
   * be derived from the file-extension in the constructor of {@link BinaryResource}. Otherwise, the content type sent
   * with the uploaded file is used.
   * <p>
   * The content is passed as well to allow for a custom content type detection logic.
   */
  protected String detectContentType(String filename, Part part, byte[] content) {
    if (filename != null) {
      return null;
    }
    return part.getContentType();
  }

  /**
   * @param uploadable
   *          is the JsonAdapter that triggers the upload
   * @return the set of accepted lowercase file extensions or media types for that uploadable. If the set contains '*'
   *         then all files are accepted.
   * @since 10.x
   */
  protected Set<String> getValidFileExtensionsFor(IUploadable uploadable, Map<String, String> uploadProperties) {
    Set<String> extSet = toExtensionsLowercase(uploadable.getAcceptedUploadFileExtensions());
    if (extSet.isEmpty()) {
      return getValidFileExtensionsDefault();
    }
    return extSet;
  }

  protected Set<String> toExtensionsLowercase(Collection<String> extOrMediaList) {
    if (extOrMediaList == null) {
      return Collections.emptySet();
    }
    Set<String> extSet = new HashSet<>();
    for (String extOrMedia : extOrMediaList) {
      if (extOrMedia != null && extOrMedia.indexOf('/') < 0) {
        extSet.add(extOrMedia);
      }
      else {
        MimeTypes.findByMimeTypeName(extOrMedia).forEach(t -> extSet.add(t.getFileExtension()));
      }
    }
    return extSet;
  }

  protected Set<String> getValidFileExtensionsDefault() {
    return DEFAULT_VALID_FILE_EXTENSIONS;
  }

  /**
   * Returns the {@link IBinaryResourceConsumer} that is registered to the specified session under the given adapter ID.
   * If the adapter could not be found, or the adapter is not a {@link IBinaryResourceConsumer}, a runtime exception is
   * thrown.
   */
  protected IUploadable resolveJsonAdapter(IUiSession uiSession, String targetAdapterId) {
    // Resolve adapter
    if (!StringUtility.hasText(targetAdapterId)) {
      throw new IllegalArgumentException("Missing target adapter ID");
    }
    IJsonAdapter<?> jsonAdapter = uiSession.getJsonAdapter(targetAdapterId);
    if (jsonAdapter == null) {
      return null;
    }
    if (jsonAdapter instanceof IUploadable) {
      return (IUploadable) jsonAdapter;
    }
    throw new IllegalStateException("Invalid adapter for ID " + targetAdapterId + " (unexpected type: " + jsonAdapter.getClass().getName() + ")");
  }

  /**
   * Writes the given {@link JSONObject} into the given {@link ServletResponse}.
   */
  protected void writeJsonResponse(ServletResponse servletResponse, JSONObject jsonObject) throws IOException {
    m_jsonRequestHelper.writeResponse(servletResponse, jsonObject);
  }

  /**
   * @throws RejectedResourceException
   *           when filename extension is not accepted
   */
  protected void verifyFileName(Set<String> validFileExtensions, String filename, String ext) {
    if (!validFileExtensions.isEmpty() && !validFileExtensions.contains("*") && !validFileExtensions.contains(ext)) {
      throw new RejectedResourceException("Filename '{}' has no accepted extension.", filename);
    }
  }

  /**
   * Checks if the uploaded file exceeds the maximum allowed upload size for given {@code uploadable}.
   *
   * @throws RejectedResourceException
   *           when size of part exceeds the allowed upload size
   */
  protected void verifyMaximumUploadSize(IUploadable uploadable, Part part) {
    if (part.getSize() > uploadable.getMaximumUploadSize()) {
      throw new RejectedResourceException("The field {} exceeds its maximum permitted size of {} bytes.", part.getName(), uploadable.getMaximumUploadSize());
    }
  }

  /**
   * Checks the resource to be upload for malware
   *
   * @throws UnsafeResourceException
   *           when unsafe
   */
  protected void verifyFileSafety(BinaryResource res) {
    //do malware scan and log issues
    BEANS.get(MalwareScanner.class).scan(res);
  }

  /**
   * @throws RejectedResourceException
   *           when not compliant
   */
  protected void verifyFileIntegrity(BinaryResource res) {
    if (!MimeTypes.verifyMagic(res)) {
      byte[] content = res.getContent();
      String header = (content == null || content.length == 0) ? "" : HexUtility.encode(Arrays.copyOfRange(content, 0, Math.min(8, content.length)));
      String message = "File '{}' has content header '{}' which does not match its extension.";
      LOG.info(message, res.getFilename(), header);
      throw new RejectedResourceException(message, res.getFilename(), header);
    }
  }
}
