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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.ext.ImageViewer;
import org.eclipse.scout.rt.ui.rap.ext.MenuAdapterEx;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.fields.AbstractRwtScoutDndSupport;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * <h3>SwtScoutImageBox</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutImageField extends RwtScoutFieldComposite<IImageField> implements IRwtScoutImageBox {

  private Image m_image;
  private Menu m_contextMenu;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    ImageViewer imgViewer = getUiEnvironment().getFormToolkit().createImageViewer(container);
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

    m_contextMenu = new Menu(imgViewer.getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new P_ContextMenuListener(imgViewer, getUiField()));
    imgViewer.setMenu(m_contextMenu);

    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
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
    getUiField().setAlignmentY(RwtUtility.getHorizontalAlignment(getScoutObject().getGridData().verticalAlignment));
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
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (IImageField.PROP_IMAGE.equals(name)) {
      updateImageFromScout();
    }
    else if (IImageField.PROP_AUTO_FIT.equals(name)) {
      updateAutoFitFromScout();

    }
    super.handleScoutPropertyChange(name, newValue);

  }

  private class P_ContextMenuListener extends MenuAdapterEx {
    private static final long serialVersionUID = 1L;

    public P_ContextMenuListener(Control menuControl, Control keyStrokeWidget) {
      super(menuControl, keyStrokeWidget);
    }

    @Override
    protected Menu getContextMenu() {
      return m_contextMenu;
    }

    @Override
    protected void setContextMenu(Menu contextMenu) {
      m_contextMenu = contextMenu;
    }

    @Override
    public void menuShown(MenuEvent e) {
      super.menuShown(e);

      final AtomicReference<IMenu[]> scoutMenusRef = new AtomicReference<IMenu[]>();
      Runnable t = new Runnable() {
        @Override
        public void run() {
          IMenu[] scoutMenus = getScoutObject().getUIFacade().firePopupFromUI();
          scoutMenusRef.set(scoutMenus);
        }
      };
      JobEx job = RwtScoutImageField.this.getUiEnvironment().invokeScoutLater(t, 1200);
      try {
        job.join(1200);
      }
      catch (InterruptedException ex) {
        //nop
      }
      // grab the actions out of the job, when the actions are provided within
      // the scheduled time the popup will be handled.
      if (scoutMenusRef.get() != null) {
        RwtMenuUtility.fillContextMenu(scoutMenusRef.get(), RwtScoutImageField.this.getUiEnvironment(), m_contextMenu);
      }
    }
  } // end class P_ContextMenuListener

  private class P_DndSupport extends AbstractRwtScoutDndSupport {
    public P_DndSupport(IPropertyObserver scoutObject, IDNDSupport scoutDndSupportable, Control control) {
      super(scoutObject, scoutDndSupportable, control, RwtScoutImageField.this.getUiEnvironment());
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
