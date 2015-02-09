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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField;
import org.eclipse.scout.rt.ui.swt.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.IValidateRoot;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.action.menu.MenuPositionCorrectionListener;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtScoutContextMenu;
import org.eclipse.scout.rt.ui.swt.ext.ImageViewer;
import org.eclipse.scout.rt.ui.swt.ext.ScrolledFormEx;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.AbstractSwtScoutDndSupport;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>SwtScoutImageBox</h3> ...
 *
 * @since 1.0.9 16.07.2008
 */
public class SwtScoutImageField extends SwtScoutFieldComposite<IImageField> implements ISwtScoutImageBox {

  private Image m_image;
  private ImageViewer m_imageViewer;
  private ScrolledFormEx m_scrolledForm;
  private SwtContextMenuMarkerComposite m_menuMarkerComposite;
  private SwtScoutContextMenu m_contextMenu;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());
    m_menuMarkerComposite = new SwtContextMenuMarkerComposite(container, getEnvironment(), SWT.NONE);
    getEnvironment().getFormToolkit().adapt(m_menuMarkerComposite);
    m_menuMarkerComposite.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        getSwtField().setFocus();
        m_contextMenu.getSwtMenu().setVisible(true);
      }
    });

    Composite body = null;
    // scrollable handling
    if (getScoutObject().isScrollBarEnabled()) {
      m_scrolledForm = getEnvironment().getFormToolkit().createScrolledFormEx(m_menuMarkerComposite, SWT.H_SCROLL | SWT.V_SCROLL);
      setSwtField(m_scrolledForm);
      m_scrolledForm.setBackground(m_scrolledForm.getDisplay().getSystemColor(SWT.COLOR_BLUE));
      body = m_scrolledForm.getBody();
      m_scrolledForm.setData(ISwtScoutPart.MARKER_SCOLLED_FORM, new Object());
    }
    else {
      body = getEnvironment().getFormToolkit().createComposite(m_menuMarkerComposite);
      setSwtField(body);
    }

    body.setData(IValidateRoot.VALIDATE_ROOT_DATA, new DefaultValidateRoot(body) {
      @Override
      public void validate() {
        if (m_scrolledForm != null) {
          if (!m_scrolledForm.isDisposed()) {
            m_scrolledForm.reflow(true);
          }
        }
      }
    });

    m_imageViewer = getEnvironment().getFormToolkit().createImageViewer(body);
    setSwtContainer(container);
    setSwtLabel(label);

    m_imageViewer.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });

    // layout
    LogicalGridLayout layout = new LogicalGridLayout(1, 0);
    getSwtContainer().setLayout(layout);
    m_menuMarkerComposite.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    body.setLayout(new FillLayout());
  }

  protected void installContextMenu() {
    m_menuMarkerComposite.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    m_contextMenuVisibilityListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
          final boolean markerVisible = getScoutObject().getContextMenu().isVisible();
          getEnvironment().invokeSwtLater(new Runnable() {
            @Override
            public void run() {
              m_menuMarkerComposite.setMarkerVisible(markerVisible);
            }
          });
        }
      }
    };
    getScoutObject().getContextMenu().addPropertyChangeListener(m_contextMenuVisibilityListener);

    m_contextMenu = new SwtScoutContextMenu(getImageViewer().getShell(), getScoutObject().getContextMenu(), getEnvironment());
    getImageViewer().setMenu(m_contextMenu.getSwtMenu());

    // correction of menu position
    int hAlign;
    switch (getImageViewer().getAlignmentX()) {
      case SWT.LEFT:
        hAlign = MenuPositionCorrectionListener.HORIZONTAL_LEFT;
        break;
      case SWT.RIGHT:
        hAlign = MenuPositionCorrectionListener.HORIZONTAL_RIGHT;
        break;
      default:
        hAlign = MenuPositionCorrectionListener.HORIZONTAL_CENTER;
        break;
    }

    int vAlign;
    switch (getImageViewer().getAlignmentY()) {
      case SWT.TOP:
        vAlign = MenuPositionCorrectionListener.VERTICAL_TOP;
        break;
      case SWT.BOTTOM:
        vAlign = MenuPositionCorrectionListener.VERTICAL_BOTTOM;
        break;
      default:
        vAlign = MenuPositionCorrectionListener.VERTICAL_CENTER;
        break;
    }

    getImageViewer().addListener(SWT.MenuDetect, new MenuPositionCorrectionListener(getImageViewer(), hAlign | vAlign));
  }

  protected void uninstallContextMenu() {
    if (m_contextMenuVisibilityListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(m_contextMenuVisibilityListener);
      m_contextMenuVisibilityListener = null;
    }
  }

  public ImageViewer getImageViewer() {
    return m_imageViewer;
  }

  private void freeResources() {
    if (m_image != null && !m_image.isDisposed()) {
      if (getSwtField() != null && !getSwtField().isDisposed()) {
        getImageViewer().setImage(null);
      }
      m_image.dispose();
      m_image = null;
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    getImageViewer().setAlignmentX(SwtUtility.getHorizontalAlignment(getScoutObject().getGridData().horizontalAlignment));
    getImageViewer().setAlignmentY(SwtUtility.getVerticalAlignment(getScoutObject().getGridData().verticalAlignment));
    updateAutoFitFromScout();
    updateImageFromScout();
    new P_DndSupport(getScoutObject(), getScoutObject(), getSwtField(), getEnvironment());
    installContextMenu();
  }

  @Override
  protected void detachScout() {
    uninstallContextMenu();
    super.detachScout();
  }

  protected void updateImageFromScout() {
    freeResources();
    if (getScoutObject().getImage() instanceof byte[]) {
      m_image = new Image(getSwtField().getDisplay(), new ByteArrayInputStream((byte[]) getScoutObject().getImage()));
      getImageViewer().setImage(m_image);
    }
    else if (getScoutObject().getImage() instanceof ImageData) {
      m_image = new Image(getSwtField().getDisplay(), (ImageData) getScoutObject().getImage());
      getImageViewer().setImage(m_image);
    }
    else if (!StringUtility.isNullOrEmpty(getScoutObject().getImageId())) {
      getImageViewer().setImage(getEnvironment().getIcon(getScoutObject().getImageId()));
    }
    getImageViewer().redraw();
  }

  protected void updateAutoFitFromScout() {
    getImageViewer().setAutoFit(getScoutObject().isAutoFit());
  }

  @Override
  protected void setFocusableFromScout(boolean b) {
    ImageViewer imageViewer = getImageViewer();
    if (imageViewer != null) {
      imageViewer.setFocusable(b);
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(IImageField.PROP_IMAGE_ID) || IImageField.PROP_IMAGE.equals(name)) {
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
