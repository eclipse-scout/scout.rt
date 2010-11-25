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
package org.eclipse.scout.rt.ui.swt.form.fields.imagebox;

import java.io.ByteArrayInputStream;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.ImageViewer;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.AbstractSwtScoutDndSupport;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>SwtScoutImageBox</h3> ...
 * 
 * @since 1.0.9 16.07.2008
 */
public class SwtScoutImageField extends SwtScoutFieldComposite<IImageField> implements ISwtScoutImageBox {

  private Image m_image;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle, getEnvironment());
    getEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);
    ImageViewer imgViewer = getEnvironment().getFormToolkit().createImageViewer(container);
    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(imgViewer);

    imgViewer.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });

    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));
  }

  private void freeResources() {
    if (m_image != null && !m_image.isDisposed()) {
      if (getSwtField() != null && !getSwtField().isDisposed()) {
        getSwtField().setImage(null);
      }
      m_image.dispose();
      m_image = null;
    }
  }

  @Override
  public ImageViewer getSwtField() {
    return (ImageViewer) super.getSwtField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    getSwtField().setAlignmentX(SwtUtility.getHorizontalAlignment(getScoutObject().getGridData().horizontalAlignment));
    getSwtField().setAlignmentY(SwtUtility.getHorizontalAlignment(getScoutObject().getGridData().verticalAlignment));
    updateAutoFitFromScout();
    updateImageFromScout();
    new P_DndSupport(getScoutObject(), getScoutObject(), getSwtField(), getEnvironment());
  }

  protected void updateImageFromScout() {
    freeResources();
    if (getScoutObject().getImage() instanceof byte[]) {
      m_image = new Image(getSwtField().getDisplay(), new ByteArrayInputStream((byte[]) getScoutObject().getImage()));
      getSwtField().setImage(m_image);
    }
    else if (getScoutObject().getImage() instanceof ImageData) {
      m_image = new Image(getSwtField().getDisplay(), (ImageData) getScoutObject().getImage());
      getSwtField().setImage(m_image);
    }
    else if (!StringUtility.isNullOrEmpty(getScoutObject().getImageId())) {
      getSwtField().setImage(getEnvironment().getIcon(getScoutObject().getImageId()));
    }
    getSwtField().redraw();
  }

  protected void updateAutoFitFromScout() {
    getSwtField().setAutoFit(getScoutObject().isAutoFit());
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

  private class P_DndSupport extends AbstractSwtScoutDndSupport {
    public P_DndSupport(IPropertyObserver scoutObject, IDNDSupport scoutDndSupportable, Control control, ISwtEnvironment environment) {
      super(scoutObject, scoutDndSupportable, control, environment);
    }

    @Override
    protected TransferObject handleSwtDragRequest() {
      // will never be called here, since handleDragSetData never calls super.
      final Holder<TransferObject> result = new Holder<TransferObject>(TransferObject.class, null);
      Runnable t = new Runnable() {
        @Override
        public void run() {
          TransferObject scoutTransferable = getScoutObject().getUIFacade().fireDragRequestFromUI();
          result.setValue(scoutTransferable);
        }
      };
      JobEx job = getEnvironment().invokeScoutLater(t, 2345);
      try {
        job.join(2345);
      }
      catch (InterruptedException e) {
        //nop
      }
      return result.getValue();
    }

    @Override
    protected void handleSwtDropAction(DropTargetEvent event, final TransferObject scoutTransferObject) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireDropActionFromUi(scoutTransferObject);
        }
      };
      getEnvironment().invokeScoutLater(job, 200);
    }
  } // end class P_DndSupport
}
