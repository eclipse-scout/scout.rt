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
package org.eclipse.scout.rt.ui.html.officeonline.wopi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.TTLCache;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class WopiRoundtrip implements WopiConstants {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(WopiRoundtrip.class);

  private final TTLCache<String/*fileId*/, String/*lockId*/> m_fileLocks;
  private final HttpServletRequest m_req;
  private final HttpServletResponse m_res;
  private final String[] m_tokens;
  private final IWopiContentProvider m_contentProvider;
  private final FileInfo m_fileInfo;

  /**
   * <code>/wopi/files/a.docx/contents</code>
   *
   * @param tokens
   *          0="", 1="wopi", 2="files", 3="a.docx", 4="contents"
   */
  public WopiRoundtrip(
      TTLCache<String/*fileId*/, String/*lockId*/> fileLocks,
      HttpServletRequest req,
      HttpServletResponse res,
      String[] tokens,
      IWopiContentProvider contentProvider,
      FileInfo fileInfo) {
    m_fileLocks = fileLocks;
    m_req = req;
    m_res = res;
    m_tokens = tokens;
    m_contentProvider = contentProvider;
    m_fileInfo = fileInfo;
  }

  public void handle() throws IOException {
    boolean httpGet = WopiConstants.GET.equals(m_req.getMethod());
    boolean httpPost = !httpGet;
    if (m_tokens.length < 4 || !FILES.equals(m_tokens[2])) {
      handleError(501);
      return;
    }

    if (httpGet && m_tokens.length == 4)
    {
      handleCheckFileInfo();
      return;
    }
    if (httpGet && m_tokens.length == 5 && CONTENTS.equals(m_tokens[4]))
    {
      handleGetFileContent();
      return;
    }
    if (httpPost && m_tokens.length == 5 && CONTENTS.equals(m_tokens[4]))
    {
      handleSetFileContent(m_req.getHeader(X_WOPI_LOCK));
      return;
    }
    if (httpPost && m_tokens.length == 4 && LOCK.equals(m_req.getHeader(X_WOPI_OVERRIDE)))
    {
      handleLock(m_req.getHeader(X_WOPI_OLDLOCK), m_req.getHeader(X_WOPI_LOCK));
      return;
    }
    if (httpPost && m_tokens.length == 4 && UNLOCK.equals(m_req.getHeader(X_WOPI_OVERRIDE)))
    {
      handleUnlock(m_req.getHeader(X_WOPI_LOCK));
      return;
    }
    if (httpPost && m_tokens.length == 4 && REFRESH_LOCK.equals(m_req.getHeader(X_WOPI_OVERRIDE)))
    {
      handleRefreshLock(m_req.getHeader(X_WOPI_LOCK));
      return;
    }
    if (httpPost && m_tokens.length == 4 && COBALT.equals(m_req.getHeader(X_WOPI_OVERRIDE)))
    {
      handleExecuteCellStorageRequest();
      return;
    }
    handleError(501);
  }

  protected void handleCheckFileInfo() throws IOException
  {
    if (LOG.isInfoEnabled()) {
      LOG.info("Getting metdata for the file: " + m_fileInfo.getFileId());
    }

    WopiCheckFileInfo cfi = new WopiCheckFileInfo();
    cfi.AllowExternalMarketplace = false;
    cfi.BaseFileName = m_fileInfo.getFileId();
    cfi.CloseButtonClosesWindow = false;
    cfi.DisableBrowserCachingOfUserContent = true;
    cfi.DisablePrint = false;
    cfi.DisableTranslation = true;
    //cfi.HostAuthenticationId = "s-1-5-21-3430578067-4192788304-1690859819-21774";
    cfi.HostName = "SharePoint";
    cfi.HostNotes = "HostBIEnabled";
    cfi.OwnerId = "Internal";
    cfi.ReadOnly = false;
    cfi.RestrictedWebViewOnly = false;
    cfi.Size = m_fileInfo.getLength();

    String extension = m_fileInfo.getFileId().substring(m_fileInfo.getFileId().lastIndexOf('.'));
    if (extension.equals(".docx")) {
      cfi.SupportsCoauth = true;
      cfi.SupportsCobalt = true;
      cfi.SupportsFolders = true;
      cfi.SupportsLocks = true;
      cfi.SupportsScenarioLinks = false;
      cfi.SupportsSecureStore = false;
      cfi.SupportsUpdate = true;
    }
    if (extension.equals(".xlsx")) {
      cfi.SupportsCoauth = false;
      cfi.SupportsCobalt = false;
      cfi.SupportsFolders = false;
      cfi.SupportsLocks = true;
      cfi.SupportsScenarioLinks = false;
      cfi.SupportsSecureStore = false;
      cfi.SupportsUpdate = true;
    }
    if (extension.equals(".pptx")) {
      cfi.SupportsCoauth = false;
      cfi.SupportsCobalt = false;
      cfi.SupportsFolders = false;
      cfi.SupportsLocks = true;
      cfi.SupportsScenarioLinks = false;
      cfi.SupportsSecureStore = false;
      cfi.SupportsUpdate = true;
    }

    cfi.TenantId = "33b62539-8c5e-423c-aa3e-cc2a9fd796f2";
    //cfi.TimeZone = "+0300#0000-11-00-01T02:00:00:0000#+0000#0000-03-00-02T02:00:00:0000#-0060";
    cfi.UserCanAttend = false;
    cfi.UserCanNotWriteRelative = true;
    cfi.UserCanPresent = true;
    cfi.UserCanWrite = true;
    cfi.Version = WopiUtility.createWopiFileVersion(m_fileInfo);
    cfi.WebEditingDisabled = false;

    // create json
    byte[] jsonResponse = cfi.toJson().getBytes("UTF-8");
    writeWopiResponseHeaders();
    m_res.setContentType("application/json");
    m_res.setContentLength(jsonResponse.length);
    m_res.getOutputStream().write(jsonResponse);
  }

  protected void handleGetFileContent() throws IOException {
    if (LOG.isInfoEnabled()) {
      LOG.info("Getting content for the file: " + m_fileInfo.getFileId());
    }
    byte[] stream = m_contentProvider.getFileContent(m_fileInfo.getFileId());
    writeWopiResponseHeaders();
    m_res.setContentType("application/octet-stream");
    m_res.setContentLength(stream.length);
    m_res.getOutputStream().write(stream);
    if (LOG.isInfoEnabled()) {
      LOG.info("loaded " + stream.length + " bytes");
    }
    writeWopiResponseHeaders();
  }

  protected void handleSetFileContent(String lockId) throws IOException {
    String currentLockId = m_fileLocks.get(m_fileInfo.getFileId());
    if (LOG.isInfoEnabled()) {
      LOG.info("Setting content for the file: " + m_fileInfo.getFileId());
    }
    if (!currentLockId.equals(lockId)) {
      handleError(404);
      return;
    }
    byte[] stream = new byte[m_req.getContentLength()];
    InputStream in = m_req.getInputStream();
    int pos = 0;
    while (pos < stream.length) {
      int n = in.read(stream, pos, stream.length - pos);
      pos += n;
    }
    m_contentProvider.setFileContent(m_fileInfo.getFileId(), stream);
    if (LOG.isInfoEnabled()) {
      LOG.info("saved " + stream.length + " bytes");
    }
    writeWopiResponseHeaders();
  }

  protected void handleLock(String oldLockId, String lockId) throws IOException {
    String currentLockId = m_fileLocks.get(m_fileInfo.getFileId());
    if (LOG.isInfoEnabled()) {
      LOG.info("Lock file : " + m_fileInfo.getFileId() + " as " + lockId + "; currentLockId=" + currentLockId);
    }
    if (notEqual(currentLockId, oldLockId) && notEqual(currentLockId, lockId)) {
      handleError(409);
      return;
    }
    m_fileLocks.put(m_fileInfo.getFileId(), lockId);
    writeWopiResponseHeaders();
    m_res.setHeader(X_WOPI_OLDLOCK, oldLockId);
  }

  protected void handleUnlock(String lockId) throws IOException {
    String currentLockId = m_fileLocks.get(m_fileInfo.getFileId());
    if (LOG.isInfoEnabled()) {
      LOG.info("Unlock file : " + m_fileInfo.getFileId() + " as " + lockId + "; currentLockId=" + currentLockId);
    }
    if (notEqual(currentLockId, lockId)) {
      handleError(409);
      return;
    }
    m_fileLocks.remove(m_fileInfo.getFileId());
    writeWopiResponseHeaders();
  }

  protected void handleRefreshLock(String lockId) throws IOException {
    String currentLockId = m_fileLocks.get(m_fileInfo.getFileId());
    if (LOG.isInfoEnabled()) {
      LOG.info("Refresh lock on file : " + m_fileInfo.getFileId() + " as " + lockId + "; currentLockId=" + currentLockId);
    }
    if (notEqual(currentLockId, null) && notEqual(currentLockId, lockId)) {
      handleError(409);
      return;
    }
    m_fileLocks.put(m_fileInfo.getFileId(), lockId);
    writeWopiResponseHeaders();
  }

  protected void handleExecuteCellStorageRequest() throws IOException {
    /*
    if(LOG.isInfoEnabled()) LOG.info("Cobalt file: " + fi.getFileId());
    Atom atom = m_cobaltProcessor.ExecuteCobalt(context.Request.InputStream);
    WriteWopiResponseHeaders(req, res);
    res.setContentLength(atom.length);
    atom.CopyTo(context.Response.OutputStream);
     */
    handleError(501);
  }

  protected void handleError(int httpCode) throws IOException {
    if (LOG.isInfoEnabled()) {
      LOG.info("send error " + httpCode);
    }
    m_res.sendError(httpCode);
  }

  protected void writeWopiResponseHeaders() throws IOException {
    m_res.setHeader(X_WOPI_SERVERVERSION, "2");
    m_res.setHeader(X_WOPI_MACHINENAME, m_req.getLocalName());
    //res.setHeader(X_WOPI_PERFTRACE, "true");
    //res.setHeader(X_WOPI_SERVERERROR, "");
  }

  protected void dumpHttpRequest() throws IOException {
    StringBuilder buf = new StringBuilder();
    buf.append("############## HTTP ###############\n");
    buf.append("HTTP " + m_req.getMethod() + " " + m_req.getPathInfo() + "\n");
    for (Enumeration<String> en = m_req.getHeaderNames(); en.hasMoreElements();) {
      String key = en.nextElement();
      String value = m_req.getHeader(key);
      buf.append("" + key + ": " + value + "\n");
    }
    LOG.info(buf.toString());
  }

  protected static boolean notEqual(Object a, Object b) {
    return !(a == b || (a != null && a.equals(b)));
  }

}
