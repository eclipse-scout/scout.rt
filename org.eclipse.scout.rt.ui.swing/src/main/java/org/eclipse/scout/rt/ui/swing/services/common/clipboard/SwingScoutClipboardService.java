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
package org.eclipse.scout.rt.ui.swing.services.common.clipboard;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.dnd.FileListTransferObject;
import org.eclipse.scout.commons.dnd.ImageTransferObject;
import org.eclipse.scout.commons.dnd.TextTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.dnd.TransferObjectRequest;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardConsumer;
import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardService;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.service.AbstractService;

@Priority(-2)
public class SwingScoutClipboardService extends AbstractService implements IClipboardService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutClipboardService.class);

  @Override
  public void consumeContents(final IClipboardConsumer clipboardConsumer, final TransferObjectRequest... requests) throws ProcessingException {
    final IClientSession clientSession = ClientSyncJob.getCurrentSession();
    try {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          try {
            Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            final TransferObject[] transferObjects = createScoutTransferables(contents, requests);
            ClientSyncJob clipboardConsumerJob = new ClientSyncJob(SwingScoutClipboardService.class.getSimpleName() + " consume", clientSession) {
              @Override
              protected void runVoid(IProgressMonitor monitor) throws Throwable {
                clipboardConsumer.consume(transferObjects);
              }
            };
            clipboardConsumerJob.schedule();
          }
          catch (Throwable t) {
            LOG.debug("Cannot get system clipboard's contents", t);
          }
        }
      });
    }
    catch (Throwable t) {
      throw new ProcessingException("Cannot get system clipboard's contents", t);
    }
  }

  @Override
  public void setContents(final TransferObject transferObject) throws ProcessingException {
    try {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          try {
            Transferable contents = SwingUtility.createSwingTransferable(transferObject);
            if (contents != null) {
              Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, null);
            }
          }
          catch (Throwable t) {
            LOG.warn("Cannot set system clipboard's contents", t);
          }
        }
      });
    }
    catch (Throwable t) {
      throw new ProcessingException("Cannot set system clipboard's contents", t);
    }
  }

  @Override
  public void setTextContents(String textContents) throws ProcessingException {
    setContents(new TextTransferObject(textContents));
  }

  private TransferObject[] createScoutTransferables(Transferable contents, TransferObjectRequest... requests) {
    ArrayList<TransferObject> result = new ArrayList<TransferObject>();
    if (requests != null) {
      for (TransferObjectRequest request : requests) {
        try {
          TransferObject scoutTransferObject = createScoutTransferable(contents, request);
          if (scoutTransferObject != null) {
            result.add(scoutTransferObject);
          }
        }
        catch (Exception e) {
          LOG.debug("Cannot create scout transform object", e);
        }
      }
    }
    if (result.isEmpty()) {
      result.add(SwingUtility.createScoutTransferable(contents));
    }
    return result.toArray(new TransferObject[result.size()]);
  }

  @SuppressWarnings("unchecked")
  private TransferObject createScoutTransferable(Transferable contents, TransferObjectRequest request) throws UnsupportedFlavorException, IOException, ProcessingException {
    if (request == null) {
      return null;
    }
    if (TextTransferObject.class.equals(request.getTransferObjectType())) {
      DataFlavor requestedDataFlavor = null;
      if (StringUtility.hasText(request.getMimeType())) {
        try {
          for (DataFlavor flavor : contents.getTransferDataFlavors()) {
            if (flavor.isMimeTypeEqual(request.getMimeType())) {
              requestedDataFlavor = flavor;
              break;
            }
          }
        }
        catch (Throwable t) {
          LOG.debug(null, t);
        }
      }
      if (requestedDataFlavor == null) {
        requestedDataFlavor = DataFlavor.stringFlavor;
      }
      Reader reader = requestedDataFlavor.getReaderForText(contents);
      String content = IOUtility.getContent(reader);
      TextTransferObject result = new TextTransferObject(content);
      result.setMimeType(requestedDataFlavor.getMimeType());
      return result;
    }
    else if (ImageTransferObject.class.equals(request.getTransferObjectType())) {
      ImageTransferObject result = new ImageTransferObject(contents.getTransferData(DataFlavor.imageFlavor));
      result.setMimeType(DataFlavor.imageFlavor.getMimeType());
      return result;
    }
    else if (FileListTransferObject.class.equals(request.getTransferObjectType())) {
      ArrayList<File> fileList = new ArrayList<File>();
      CollectionUtility.appendAllList(fileList, (List) contents.getTransferData(DataFlavor.javaFileListFlavor));
      FileListTransferObject result = new FileListTransferObject(fileList);
      result.setMimeType(DataFlavor.javaFileListFlavor.getMimeType());
      return result;
    }
    else {
      return null;
    }
  }
}
