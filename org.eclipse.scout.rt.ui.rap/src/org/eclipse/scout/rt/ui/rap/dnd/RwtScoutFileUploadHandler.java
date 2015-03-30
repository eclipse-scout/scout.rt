/*******************************************************************************
 * Copyright (c) 2010, 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.dnd;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.rap.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.fileupload.FileDetails;
import org.eclipse.rap.fileupload.FileUploadEvent;
import org.eclipse.rap.fileupload.FileUploadHandler;
import org.eclipse.rap.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutDndUploadCallback;
import org.eclipse.swt.dnd.DropTargetEvent;

/**
 * File upload handler for the Drag & Drop functionality used when dragging files from the
 * client desktop to the application.
 *
 * @since 4.0.0-M7
 */
public class RwtScoutFileUploadHandler implements IRwtScoutFileUploadHandler {
  private final IRwtScoutDndUploadCallback m_uploadCallback;
  private FileUploadHandler m_fileUploadHandler;

  public RwtScoutFileUploadHandler(IRwtScoutDndUploadCallback uploadCallback) {
    m_uploadCallback = uploadCallback;
  }

  @Override
  public boolean startFileUpload(DropTargetEvent event) {
    m_fileUploadHandler = new FileUploadHandler(new DiskFileUploadReceiver());
    m_fileUploadHandler.addUploadListener(new P_UploadListener(event));
    ClientFile[] clientFiles = (ClientFile[]) event.data;
    ClientFileUploader uploader = RWT.getClient().getService(ClientFileUploader.class);
    uploader.submit(m_fileUploadHandler.getUploadUrl(), clientFiles);
    return true;
  }

  private static RwtScoutFileUploadEvent createRwtScoutFileUploadEvent(FileUploadEvent event, IRwtScoutFileUploadHandler source) {
    List<RwtScoutFileUploadFileDetail> uploadDetails = new ArrayList<RwtScoutFileUploadFileDetail>();
    for (FileDetails fd : event.getFileDetails()) {
      uploadDetails.add(new RwtScoutFileUploadFileDetail(fd.getFileName(), fd.getContentType()));
    }
    return new RwtScoutFileUploadEvent(source, event.getContentLength(), event.getBytesRead(), event.getException(), uploadDetails);
  }

  private class P_UploadListener implements FileUploadListener {
    private final ServerPushSession m_pushSession;
    private final DropTargetEvent m_dropTargetEvent;

    public P_UploadListener(DropTargetEvent dropTargetEvent) {
      m_dropTargetEvent = dropTargetEvent;
      m_pushSession = new ServerPushSession();
      m_pushSession.start();
    }

    @Override
    public void uploadProgress(FileUploadEvent event) {
      RwtScoutFileUploadEvent fileUploadEvent = createRwtScoutFileUploadEvent(event, RwtScoutFileUploadHandler.this);
      m_uploadCallback.uploadProgress(m_dropTargetEvent, fileUploadEvent);
    }

    @Override
    public void uploadFinished(final FileUploadEvent event) {
      DiskFileUploadReceiver receiver = (DiskFileUploadReceiver) m_fileUploadHandler.getReceiver();
      final List<File> uploadedFiles = new ArrayList<File>();
      uploadedFiles.addAll(Arrays.asList(receiver.getTargetFiles()));
      RwtScoutFileUploadEvent fileUploadEvent = createRwtScoutFileUploadEvent(event, RwtScoutFileUploadHandler.this);
      m_uploadCallback.uploadFinished(m_dropTargetEvent, fileUploadEvent, uploadedFiles);
      m_pushSession.stop();
      m_fileUploadHandler.removeUploadListener(this);
    }

    @Override
    public void uploadFailed(FileUploadEvent event) {
      RwtScoutFileUploadEvent fileUploadEvent = createRwtScoutFileUploadEvent(event, RwtScoutFileUploadHandler.this);
      m_uploadCallback.uploadFailed(m_dropTargetEvent, fileUploadEvent);
      m_pushSession.stop();
      m_fileUploadHandler.removeUploadListener(this);
    }
  }
}
