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
package org.eclipse.scout.rt.ui.html.services.common.clipboard;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.scout.commons.dnd.TextTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.commons.resource.MimeTypes;
import org.eclipse.scout.rt.client.Client;
import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardService;
import org.eclipse.scout.rt.client.ui.form.clipboard.ClipboardForm;
import org.eclipse.scout.rt.platform.service.AbstractService;

@Client
public class HtmlScoutClipboardService extends AbstractService implements IClipboardService {
  //private static final IScoutLogger LOG = ScoutLogManager.getLogger(HtmlScoutClipboardService.class);

  @Override
  public Collection<BinaryResource> getClipboardContents(MimeTypes... mimeTypes) throws ProcessingException {
    ClipboardForm form = new ClipboardForm();
    form.setMimeTypes(mimeTypes);
    form.setHandler(form.new ClipboardHandler());
    form.start();
    form.waitFor();
    if (form.isFormStored()) {
      return form.getClipboardField().getValue();
    }
    return Collections.emptyList();
  }

  @Override
  public void setContents(final TransferObject transferObject) throws ProcessingException {
    // FIXME mot Implementieren.
    throw new ProcessingException("Not yet supported.");
  }

  @Override
  public void setTextContents(String textContents) throws ProcessingException {
    // FIXME mot Implementieren.
    setContents(new TextTransferObject(textContents));
  }

//  private TransferObject[] createScoutTransferables(Transferable contents, TransferObjectRequest... requests) {
////    ArrayList<TransferObject> result = new ArrayList<TransferObject>();
////    if (requests != null) {
////      for (TransferObjectRequest request : requests) {
////        try {
////          TransferObject scoutTransferObject = createScoutTransferable(contents, request);
////          if (scoutTransferObject != null) {
////            result.add(scoutTransferObject);
////          }
////        }
////        catch (Exception e) {
////          LOG.debug("Cannot create scout transform object", e);
////        }
////      }
////    }
////    if (result.isEmpty()) {
////      result.add(HtmlUtility.createScoutTransferable(contents));
////    }
////    return result.toArray(new TransferObject[result.size()]);
//  }
//
//  @SuppressWarnings("unchecked")
//  private TransferObject createScoutTransferable(Transferable contents, TransferObjectRequest request) throws UnsupportedFlavorException, IOException, ProcessingException {
//    if (request == null) {
//      return null;
//    }
//    if (TextTransferObject.class.equals(request.getTransferObjectType())) {
//      DataFlavor requestedDataFlavor = null;
//      if (StringUtility.hasText(request.getMimeType())) {
//        try {
//          for (DataFlavor flavor : contents.getTransferDataFlavors()) {
//            if (flavor.isMimeTypeEqual(request.getMimeType())) {
//              requestedDataFlavor = flavor;
//              break;
//            }
//          }
//        }
//        catch (Throwable t) {
//          LOG.debug(null, t);
//        }
//      }
//      if (requestedDataFlavor == null) {
//        requestedDataFlavor = DataFlavor.stringFlavor;
//      }
//      Reader reader = requestedDataFlavor.getReaderForText(contents);
//      String content = IOUtility.getContent(reader);
//      TextTransferObject result = new TextTransferObject(content);
//      result.setMimeType(requestedDataFlavor.getMimeType());
//      return result;
//    }
//    else if (ImageTransferObject.class.equals(request.getTransferObjectType())) {
//      ImageTransferObject result = new ImageTransferObject(contents.getTransferData(DataFlavor.imageFlavor));
//      result.setMimeType(DataFlavor.imageFlavor.getMimeType());
//      return result;
//    }
//    else if (ResourceListTransferObject.class.equals(request.getTransferObjectType())) {
//      ArrayList<File> fileList = new ArrayList<File>();
//      CollectionUtility.appendAllList(fileList, (List) contents.getTransferData(DataFlavor.javaFileListFlavor));
//
//      // create binary resource list from file list
//      List<BinaryResource> resources = new ArrayList<BinaryResource>();
//      for (File file : fileList) {
//        resources.add(new BinaryResource(file.getName(), IOUtility.getContent(file)));
//      }
//
//      ResourceListTransferObject result = new ResourceListTransferObject(resources);
//      result.setMimeType(DataFlavor.javaFileListFlavor.getMimeType());
//      return result;
//    }
//    else {
//      return null;
//    }
//  }
}
