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
package org.eclipse.scout.rt.ui.swing.form.fields.imagebox;

/**
 * , Samuel Moser
 */
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.ImageFieldEvent;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.ImageFieldListener;
import org.eclipse.scout.rt.shared.data.basic.AffineTransformSpec;
import org.eclipse.scout.rt.shared.data.basic.BoundsSpec;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;
import org.eclipse.scout.rt.ui.swing.form.fields.imagebox.imageviewer.ImageTransformEvent;
import org.eclipse.scout.rt.ui.swing.form.fields.imagebox.imageviewer.ImageTransformListener;
import org.eclipse.scout.rt.ui.swing.form.fields.imagebox.imageviewer.SwingImageViewer;

public class SwingScoutImageField extends SwingScoutFieldComposite<IImageField> implements ISwingScoutImageField {
  private P_ScoutImageFieldListener m_scoutImageFieldListener;

  // DND
  private DragGestureRecognizer m_dragSource;
  private DropTarget m_dropTarget;

  public SwingScoutImageField() {
  }

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    SwingImageViewer imageViewer = new SwingImageViewer();
    SwingUtility.installDefaultFocusHandling(imageViewer);
    imageViewer.addMouseListener(new P_SwingPopupListener());
    imageViewer.addKeyListener(new P_SwingKeyPopupListener());
    imageViewer.addImageTransformListener(new P_SwingTransformListener());
    if (getScoutObject().isScrollBarEnabled() && !getScoutObject().isAutoFit()) {
      JScrollPaneEx scrollPane = new JScrollPaneEx(imageViewer);
      scrollPane.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData()));
      scrollPane.getVerticalScrollBar().setUnitIncrement(16);
      scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
      scrollPane.setBorder(null);
      container.add(scrollPane);
    }
    else {
      container.add(imageViewer);
    }
    //
    setSwingLabel(label);
    setSwingField(imageViewer);
    setSwingContainer(container);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  @Override
  public IImageField getScoutImageField() {
    return getScoutObject();
  }

  @Override
  public SwingImageViewer getSwingImageViewer() {
    return (SwingImageViewer) getSwingField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_scoutImageFieldListener == null) {
      m_scoutImageFieldListener = new P_ScoutImageFieldListener();
      getScoutImageField().addImageFieldListener(m_scoutImageFieldListener);
    }
    IImageField imageField = getScoutImageField();
    setImageFromScout(imageField.getImageId(), imageField.getImage());
    setAnalysisRectangleFromScout(imageField.getAnalysisRectangle());
    setImageTransformFromScout(imageField.getImageTransform());
    setAutoFitFromScout(imageField.isAutoFit());
    updateDropTransferTypesFromScout();
    updateDragTransferTypesFromScout();
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (m_scoutImageFieldListener != null) {
      getScoutImageField().removeImageFieldListener(m_scoutImageFieldListener);
      m_scoutImageFieldListener = null;
    }
  }

  protected void setImageFromScout(String imageId, Object image) {
    if (image == null) {
      if (imageId != null) {
        image = getSwingEnvironment().getImage(imageId);
      }
    }
    getSwingImageViewer().setImage(image);
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    float swingAlignX = SwingUtility.createAlignmentX(scoutAlign);
    getSwingImageViewer().setAlignmentX(swingAlignX);
  }

  @Override
  protected void setVerticalAlignmentFromScout(int scoutAlign) {
    float swingAlignY = SwingUtility.createAlignmentY(scoutAlign);
    getSwingImageViewer().setAlignmentY(swingAlignY);
  }

  @Override
  protected void setFocusableFromScout(boolean b) {
    SwingImageViewer swingImageViewer = getSwingImageViewer();
    if (swingImageViewer != null) {
      swingImageViewer.setFocusable(b);
    }
  }

  protected void setAnalysisRectangleFromScout(BoundsSpec r) {
    Rectangle swingRect = SwingUtility.createRectangle(r);
    getSwingImageViewer().setAnalysisRect(swingRect);
  }

  protected void setAutoFitFromScout(boolean b) {
    getSwingImageViewer().setAutoFit(b);
  }

  protected void setImageTransformFromScout(AffineTransformSpec at) {
    getSwingImageViewer().setImageTransform(at.dx, at.dy, at.sx, at.sy, at.angle);
  }

  protected void updateDragTransferTypesFromScout() {
    int scoutDragTransfer = getScoutImageField().getDragType();
    if (scoutDragTransfer != 0) {
      // install
      if (m_dragSource == null) {
        // create new
        m_dragSource = DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(getSwingImageViewer(),
            DnDConstants.ACTION_COPY, new P_DragGestureListener());
      }
      m_dragSource.setComponent(getSwingImageViewer());
    }
    else {
      if (m_dragSource != null) {
        m_dragSource.setComponent(null);
      }
    }
  }

  protected void updateDropTransferTypesFromScout() {
    if (getScoutImageField().getDropType() != 0) {
      // install drop support
      if (m_dropTarget == null) {
        // create new
        m_dropTarget = new DropTarget(getSwingImageViewer(), new P_DropTargetListener());
      }
      m_dropTarget.setComponent(getSwingImageViewer());
    }
    else {
      if (m_dropTarget != null) {
        m_dropTarget.setComponent(null);
      }
    }
  }

  /**
   * Scout property handler in gui thread
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IImageField.PROP_IMAGE_ID) || name.equals(IImageField.PROP_IMAGE)) {
      setImageFromScout(getScoutImageField().getImageId(), getScoutImageField().getImage());
    }
    else if (name.equals(IImageField.PROP_ANALYSIS_RECTANGLE)) {
      setAnalysisRectangleFromScout((BoundsSpec) newValue);
    }
    else if (name.equals(IImageField.PROP_IMAGE_TRANSFORM)) {
      setImageTransformFromScout((AffineTransformSpec) newValue);
    }
    else if (name.equals(IImageField.PROP_AUTO_FIT)) {
      setAutoFitFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(ITable.PROP_DRAG_TYPE)) {
      updateDragTransferTypesFromScout();
    }
    else if (name.equals(ITable.PROP_DROP_TYPE)) {
      updateDropTransferTypesFromScout();
    }
  }

  protected boolean isHandleScoutImageFieldEvent(ImageFieldEvent e) {
    switch (e.getType()) {
      case ImageFieldEvent.TYPE_AUTO_FIT:
      case ImageFieldEvent.TYPE_ZOOM_RECTANGLE: {
        return true;
      }
    }
    return false;
  }

  protected void handleScoutImageFieldEventInSwing(ImageFieldEvent e) {
    switch (e.getType()) {
      case ImageFieldEvent.TYPE_AUTO_FIT: {
        getSwingImageViewer().doFitImage();
        break;
      }
      case ImageFieldEvent.TYPE_ZOOM_RECTANGLE: {
        getSwingImageViewer().doZoomRectangle(SwingUtility.createRectangle(e.getZoomRectangle()));
        break;
      }
    }
  }

  protected void handleSwingPopup(final Component target, final Point point) {
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        List<IMenu> a = getScoutImageField().getUIFacade().firePopupFromUI();
        // call swing menu
        new SwingPopupWorker(getSwingEnvironment(), target, point, a).enqueue();
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 5678);
    // end notify
  }

  protected void handleSwingImageTransform(ImageTransformEvent e) {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    // notify Scout
    final AffineTransformSpec at = new AffineTransformSpec(e.getDx(), e.getDy(), e.getSx(), e.getSy(), e.getAngle());
    Runnable t = new Runnable() {
      @Override
      public void run() {
        //avoid circular events
        try {
          addIgnoredScoutEvent(PropertyChangeEvent.class, IImageField.PROP_IMAGE_TRANSFORM);
          //
          getScoutImageField().getUIFacade().setImageTransformFromUI(at);
        }
        finally {
          removeIgnoredScoutEvent(PropertyChangeEvent.class, IImageField.PROP_IMAGE_TRANSFORM);
        }
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 0);
  }

  protected Transferable handleSwingDragRequest() {
    final Holder<TransferObject> result = new Holder<TransferObject>(TransferObject.class, null);
    if (getScoutImageField() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          TransferObject scoutTransferable = getScoutImageField().getUIFacade().fireDragRequestFromUI();
          result.setValue(scoutTransferable);
        }
      };
      try {
        getSwingEnvironment().invokeScoutLater(t, 5678).join(5678);
      }
      catch (InterruptedException e) {
        //nop
      }
      // end notify
    }
    TransferObject scoutTransferable = result.getValue();
    Transferable swingTransferable = null;
    swingTransferable = SwingUtility.createSwingTransferable(scoutTransferable);
    return swingTransferable;
  }

  protected void handleSwingDropAction(Transferable swingTransferable) {
    if (getScoutImageField() != null) {
      if (swingTransferable != null) {
        final TransferObject scoutTransferable = SwingUtility.createScoutTransferable(swingTransferable);
        if (scoutTransferable != null) {
          // notify Scout (asynchronous !)
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutImageField().getUIFacade().fireDropActionFromUi(scoutTransferable);
            }
          };
          getSwingEnvironment().invokeScoutLater(t, 0);
          // end notify
        }
      }
    }
  }

  private class P_SwingPopupListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      // Mac popup
      if (e.isPopupTrigger() && e.getComponent().isEnabled()) {
        handleSwingPopup(e.getComponent(), e.getPoint());
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger() && e.getComponent().isEnabled()) {
        handleSwingPopup(e.getComponent(), e.getPoint());
      }
    }
  }// end private class

  private class P_SwingKeyPopupListener extends KeyAdapter {
    @Override
    public void keyReleased(KeyEvent e) {
      if (KeyEvent.VK_CONTEXT_MENU == e.getKeyCode()) {
        handleSwingPopup(e.getComponent(), getSwingImageViewer().getImageLocation());
      }
    }
  }

  private class P_SwingTransformListener implements ImageTransformListener {
    @Override
    public void transformChanged(ImageTransformEvent e) {
      handleSwingImageTransform(e);
    }
  }// end private class

  private class P_ScoutImageFieldListener implements ImageFieldListener {
    @Override
    public void imageFieldChanged(final ImageFieldEvent e) {
      if (isIgnoredScoutEvent(ImageFieldEvent.class, "" + e.getType())) {
        return;
      }
      //
      if (isHandleScoutImageFieldEvent(e)) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            handleScoutImageFieldEventInSwing(e);
          }
        };
        getSwingEnvironment().invokeSwingLater(t);
      }
    }
  }// end private class

  private class P_DropTargetListener implements DropTargetListener {
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
      // void
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
      // void
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
      // void
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
      dtde.acceptDrop(DnDConstants.ACTION_COPY);
      handleSwingDropAction(dtde.getTransferable());
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
      // void
    }
  } // end class P_DropTargetListener

  private class P_DragGestureListener implements DragGestureListener {
    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
      Transferable to = handleSwingDragRequest();
      if (to != null) {
        dge.startDrag(null, to);
      }
    }
  } // end class P_DragGestureListener

}
