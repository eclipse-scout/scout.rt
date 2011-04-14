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
package org.eclipse.scout.rt.ui.swt.services.common.clipboard;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.annotations.Priority;
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
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

@Priority(-2)
public class SwtScoutClipboardService extends AbstractService implements IClipboardService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutClipboardService.class);

  private Clipboard m_clipboard;

  @Override
  public void consumeContents(final IClipboardConsumer clipboardConsumer, final TransferObjectRequest... requests) throws ProcessingException {
    final IClientSession clientSession = ClientSyncJob.getCurrentSession();
    try {
      getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          try {
            final TransferObject[] transferObjects = createScoutTransferables(requests);
            ClientSyncJob clipboardConsumerJob = new ClientSyncJob(SwtScoutClipboardService.class.getSimpleName() + " consume", clientSession) {
              @Override
              protected void runVoid(IProgressMonitor monitor) throws Throwable {
                clipboardConsumer.consume(transferObjects);
              }
            };
            clipboardConsumerJob.schedule();
          }
          catch (Throwable t) {
            LOG.warn("Cannot get system clipboard's contents", t);
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
      if (transferObject == null) {
        return;
      }
      final Transfer transfer = createSwtTransfer(transferObject.getClass());
      if (transfer == null) {
        throw new IllegalArgumentException("unsupported transfer object type: " + transferObject);
      }
      getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          try {
            Object contents = SwtUtility.createSwtTransferable(transferObject);
            if (contents != null) {
              getSwtClipboard().setContents(new Object[]{contents}, new Transfer[]{transfer});
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

  private TransferObject[] createScoutTransferables(TransferObjectRequest... requests) {
    ArrayList<TransferObject> result = new ArrayList<TransferObject>();
    if (requests != null) {
      for (TransferObjectRequest request : requests) {
        try {
          Transfer swtTransfer = createSwtTransfer(request.getTransferObjectType());
          Object contents = getSwtClipboard().getContents(swtTransfer);
          TransferObject scoutTransferObject = createScoutTransferObject(swtTransfer, contents);
          if (scoutTransferObject != null) {
            result.add(scoutTransferObject);
          }
        }
        catch (Exception e) {
          LOG.warn("Cannot create scout transform object", e);
        }
      }
    }
    return result.toArray(new TransferObject[result.size()]);
  }

  private Transfer createSwtTransfer(Class<? extends TransferObject> transferObjectType) {
    if (TextTransferObject.class.equals(transferObjectType)) {
      return TextTransfer.getInstance();
    }
    else if (ImageTransferObject.class.equals(transferObjectType)) {
      return ImageTransfer.getInstance();
    }
    else {
      return null;
    }
  }

  private TransferObject createScoutTransferObject(Transfer transfer, Object contents) {
    if (transfer instanceof ImageTransfer) {
      return new ImageTransferObject(contents);
    }
    else if (transfer instanceof TextTransfer) {
      return new TextTransferObject((String) contents);
    }
    else {
      return null;
    }
  }

  private Clipboard getSwtClipboard() {
    if (m_clipboard == null || m_clipboard.isDisposed()) {
      m_clipboard = new Clipboard(getDisplay());
    }
    return m_clipboard;
  }

  private Display getDisplay() {
    if (PlatformUI.isWorkbenchRunning()) {
      return PlatformUI.getWorkbench().getDisplay();
    }
    else {
      Display display = Display.getCurrent();
      if (display == null) {
        display = Display.getDefault();
      }
      return display;
    }
  }
}
