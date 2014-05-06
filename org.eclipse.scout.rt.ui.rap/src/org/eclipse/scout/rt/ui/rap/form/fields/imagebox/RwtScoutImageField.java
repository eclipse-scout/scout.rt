/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.imagebox;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.ImageTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtScoutContextMenu;
import org.eclipse.scout.rt.ui.rap.ext.ImageViewer;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.fields.AbstractRwtScoutDndSupport;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.8.0
 */
public class RwtScoutImageField extends RwtScoutFieldComposite<IImageField> implements IRwtScoutImageBox {

  private static final String CLIENT_FILE_TYPE_IMAGE = "image";

  private Image m_image;
  private RwtContextMenuMarkerComposite m_contextMenuMarker;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    m_contextMenuMarker = new RwtContextMenuMarkerComposite(container, getUiEnvironment(), SWT.NONE);
    getUiEnvironment().getFormToolkit().adapt(m_contextMenuMarker);
    ImageViewer imgViewer = getUiEnvironment().getFormToolkit().createImageViewer(m_contextMenuMarker);
    setUiContainer(container);
    setUiLabel(label);
    setUiField(imgViewer);

    imgViewer.addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });

    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
    m_contextMenuMarker.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
  }

  @Override
  protected void installContextMenu() {
    RwtScoutContextMenu contextMenu = new RwtScoutContextMenu(getUiContainer().getShell(), getScoutObject().getContextMenu(), m_contextMenuMarker, getUiEnvironment());
    getUiField().setMenu(contextMenu.getUiMenu());
  }

  private void freeResources() {
    if (m_image != null && !m_image.isDisposed() && m_image.getDevice() != null) {
      if (getUiField() != null && !getUiField().isDisposed()) {
        getUiField().setImage(null);
      }
      m_image.dispose();
      m_image = null;
    }
  }

  @Override
  public ImageViewer getUiField() {
    return (ImageViewer) super.getUiField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    getUiField().setAlignmentX(RwtUtility.getHorizontalAlignment(getScoutObject().getGridData().horizontalAlignment));
    getUiField().setAlignmentY(RwtUtility.getVerticalAlignment(getScoutObject().getGridData().verticalAlignment));
    updateAutoFitFromScout();
    updateImageFromScout();
    attachDndSupport();
  }

  protected void attachDndSupport() {
    if (UiDecorationExtensionPoint.getLookAndFeel().isDndSupportEnabled()) {
      new P_DndSupport(getScoutObject(), getScoutObject(), getUiField());
    }
  }

  protected void updateImageFromScout() {
    freeResources();
    if (getScoutObject().getImage() instanceof byte[]) {
      m_image = new Image(getUiField().getDisplay(), new ByteArrayInputStream((byte[]) getScoutObject().getImage()));
      getUiField().setImage(m_image);
    }
    else if (getScoutObject().getImage() instanceof ImageData) {
      m_image = new Image(getUiField().getDisplay(), (ImageData) getScoutObject().getImage());
      getUiField().setImage(m_image);
    }
    else if (!StringUtility.isNullOrEmpty(getScoutObject().getImageId())) {
      getUiField().setImage(getUiEnvironment().getIcon(getScoutObject().getImageId()));
    }
    getUiField().redraw();
  }

  protected void updateAutoFitFromScout() {
    getUiField().setAutoFit(getScoutObject().isAutoFit());
  }

  @Override
  protected void setFocusableFromScout(boolean b) {
    ImageViewer imageViewer = getUiField();
    if (imageViewer != null) {
      imageViewer.setFocusable(b);
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (IImageField.PROP_IMAGE.equals(name)) {
      updateImageFromScout();
    }
    else if (IImageField.PROP_AUTO_FIT.equals(name)) {
      updateAutoFitFromScout();

    }
    super.handleScoutPropertyChange(name, newValue);

  }

  private class P_DndSupport extends AbstractRwtScoutDndSupport {
    public P_DndSupport(IPropertyObserver scoutObject, IDNDSupport scoutDndSupportable, Control control) {
      super(scoutObject, scoutDndSupportable, control, RwtScoutImageField.this.getUiEnvironment());
    }

    @Override
    protected TransferObject createScoutTransferableObjectFromFileUpload(DropTargetEvent event, List<File> uploadedFiles) {
      if (ClientFileTransfer.getInstance().isSupportedType(event.currentDataType) && (getScoutObject().getDropType() & IDNDSupport.TYPE_IMAGE_TRANSFER) != 0) {
        ClientFile[] clientFiles = (ClientFile[]) event.data;
        int index = 0;
        for (ClientFile clientFile : clientFiles) {
          String clientFileType = clientFile.getType();
          if (clientFileType != null && StringUtility.lowercase(clientFileType).startsWith(CLIENT_FILE_TYPE_IMAGE)) {
            ImageData imageData = new ImageData(uploadedFiles.get(index).getAbsolutePath());
            return new ImageTransferObject(imageData);
          }
          index++;
        }
      }
      return super.createScoutTransferableObjectFromFileUpload(event, uploadedFiles);
    }

    @Override
    protected TransferObject handleUiDragRequest() {
      // will never be called here, since handleDragSetData never calls super.
      final Holder<TransferObject> result = new Holder<TransferObject>(TransferObject.class, null);
      Runnable t = new Runnable() {
        @Override
        public void run() {
          TransferObject scoutTransferable = getScoutObject().getUIFacade().fireDragRequestFromUI();
          result.setValue(scoutTransferable);
        }
      };
      JobEx job = getUiEnvironment().invokeScoutLater(t, 2345);
      try {
        job.join(2345);
      }
      catch (InterruptedException e) {
        //nop
      }
      return result.getValue();
    }

    @Override
    protected void handleUiDropAction(DropTargetEvent event, final TransferObject scoutTransferObject) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireDropActionFromUi(scoutTransferObject);
        }
      };
      getUiEnvironment().invokeScoutLater(job, 200);
    }
  } // end class P_DndSupport
}
