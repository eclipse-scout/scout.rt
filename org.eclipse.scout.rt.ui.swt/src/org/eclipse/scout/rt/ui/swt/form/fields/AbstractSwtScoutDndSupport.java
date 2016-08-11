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
package org.eclipse.scout.rt.ui.swt.form.fields;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.util.SwtTransferObject;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>AbstractSwtScoutDndSupport</h3> ...
 * 
 * @since 1.0.9 24.07.2008
 */
public abstract class AbstractSwtScoutDndSupport implements ISwtScoutDndSupport {

  private final Control m_control;
  private final IPropertyObserver m_scoutObject;
  private final IDNDSupport m_scoutDndSupportable;
  private final ISwtEnvironment m_environment;
  private DropTargetListener m_dropTargetListener;
  private DragSourceListener m_dragSourceListener;
  private PropertyChangeListener m_scoutPropertyListener;
  private Transfer[] m_dragTransferTypes;
  private Transfer[] m_dropTransferTypes;

  public AbstractSwtScoutDndSupport(IPropertyObserver scoutObject, IDNDSupport scoutDndSupportable, Control control, ISwtEnvironment environment) {
    m_scoutObject = scoutObject;
    m_scoutDndSupportable = scoutDndSupportable;
    m_control = control;
    m_environment = environment;
    attachScout();
    m_control.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        detachScout();
      }
    });
  }

  protected void attachScout() {
    m_scoutPropertyListener = new P_ScoutObjectPropertyListener();
    m_scoutObject.addPropertyChangeListener(m_scoutPropertyListener);
    updateDragSupportFromScout();
    updateDropSupportFromScout();
  }

  protected void detachScout() {
    m_scoutObject.removePropertyChangeListener(m_scoutPropertyListener);

    if (m_dragTransferTypes != null) {
      DragSource dragSource = (DragSource) m_control.getData(DND_DRAG_SOURCE);
      if (dragSource != null && !dragSource.isDisposed()) {
        dragSource.removeDragListener(m_dragSourceListener);
        ArrayList<Transfer> types = new ArrayList<Transfer>(Arrays.asList(dragSource.getTransfer()));
        for (Transfer t : m_dragTransferTypes) {
          types.remove(t);
        }
        if (types.size() > 0) {
          dragSource.setTransfer(types.toArray(new Transfer[types.size()]));
        }
        else {
          dragSource.dispose();
          m_control.setData(DND_DRAG_SOURCE, null);
        }
      }
    }
    if (m_dropTransferTypes != null) {
      DropTarget dropTarget = (DropTarget) m_control.getData(DND_DROP_TARGET);
      if (dropTarget != null && !dropTarget.isDisposed()) {
        dropTarget.removeDropListener(m_dropTargetListener);
        ArrayList<Transfer> types = new ArrayList<Transfer>(Arrays.asList(dropTarget.getTransfer()));
        for (Transfer t : m_dropTransferTypes) {
          types.remove(t);
        }
        if (types.size() > 0) {
          dropTarget.setTransfer(types.toArray(new Transfer[types.size()]));
        }
        else {
          dropTarget.dispose();
          m_control.setData(DND_DROP_TARGET, null);
        }
      }
    }

  }

  protected abstract void handleSwtDropAction(DropTargetEvent event, TransferObject scoutTransferObject);

  protected abstract TransferObject handleSwtDragRequest();

  protected boolean acceptDrag() {
    return true;
  }

  @Override
  public boolean isDraggingEnabled() {
    return m_control.getData(DND_DRAG_SOURCE) != null;
  }

  protected void updateDragSupportFromScout() {
    if (m_scoutObject == null || m_control == null || m_control.isDisposed()) {
      return;
    }
    int scoutType = m_scoutDndSupportable.getDragType();
    Transfer[] swtTransferTypes = SwtUtility.convertScoutTransferTypes(scoutType);
    DragSource dragSource = (DragSource) m_control.getData(DND_DRAG_SOURCE);
    if (dragSource == null) {
      if (swtTransferTypes.length > 0) {
        // create new
        dragSource = new DragSource(m_control, DND.DROP_COPY);
      }
    }
    if (dragSource != null) {
      // remove old
      ArrayList<Transfer> types = new ArrayList<Transfer>(Arrays.asList(dragSource.getTransfer()));
      if (m_dragTransferTypes != null) {
        for (Transfer t : m_dragTransferTypes) {
          types.remove(t);
        }
        m_dragTransferTypes = null;
      }
      // add new transfer types
      m_dragTransferTypes = swtTransferTypes;
      for (Transfer t : m_dragTransferTypes) {
        types.add(t);
      }
      if (types.size() > 0) {
        dragSource.setTransfer(types.toArray(new Transfer[types.size()]));
        if (m_dragSourceListener == null) {
          m_dragSourceListener = new P_SwtDragSourceListener();
          dragSource.addDragListener(m_dragSourceListener);
        }
      }
      else {
        if (m_dragSourceListener != null) {
          dragSource.removeDragListener(m_dragSourceListener);
          m_dragSourceListener = null;
        }
        dragSource.dispose();
      }
    }

  }

  protected void updateDropSupportFromScout() {
    if (m_scoutObject == null || m_control == null || m_control.isDisposed()) {
      return;
    }
    int scoutType = m_scoutDndSupportable.getDropType();
    Transfer[] swtTransferTypes = SwtUtility.convertScoutTransferTypes(scoutType);
    DropTarget dropTarget = (DropTarget) m_control.getData(DND_DROP_TARGET);
    if (dropTarget == null) {
      if (swtTransferTypes.length > 0) {
        // create new
        dropTarget = new DropTarget(m_control, DND.DROP_MOVE | DND.DROP_COPY);
      }
    }
    if (dropTarget != null) {
      // remove old
      ArrayList<Transfer> types = new ArrayList<Transfer>(Arrays.asList(dropTarget.getTransfer()));
      if (m_dropTransferTypes != null) {
        for (Transfer t : m_dropTransferTypes) {
          types.remove(t);
        }
        m_dropTransferTypes = null;
      }
      // add new transfer types
      m_dropTransferTypes = swtTransferTypes;
      for (Transfer t : m_dropTransferTypes) {
        types.add(t);
      }
      if (types.size() > 0) {
        dropTarget.setTransfer(types.toArray(new Transfer[types.size()]));
        if (m_dropTargetListener == null) {
          m_dropTargetListener = new P_SwtDropTargetListener();
          dropTarget.addDropListener(m_dropTargetListener);
        }
      }
      else {
        if (m_dropTargetListener != null) {
          dropTarget.removeDropListener(m_dropTargetListener);
          m_dropTargetListener = null;
        }
        dropTarget.dispose();
      }
    }

  }

  protected void handleScoutProperty(String name, Object newValue) {
    if (IDNDSupport.PROP_DRAG_TYPE.equals(name)) {
      updateDragSupportFromScout();
    }
    else if (IDNDSupport.PROP_DROP_TYPE.equals(name)) {
      updateDropSupportFromScout();
    }
  }

  private class P_SwtDropTargetListener extends DropTargetAdapter {
    @Override
    public void dropAccept(DropTargetEvent event) {

    }

    @Override
    public void drop(DropTargetEvent event) {
      TransferObject scoutTransferable = SwtUtility.createScoutTransferable(event);
      if (scoutTransferable != null) {
        handleSwtDropAction(event, scoutTransferable);
      }
    }

  } // end class P_SwtDropTargetListener

  private class P_SwtDragSourceListener extends DragSourceAdapter {
    @Override
    public void dragStart(DragSourceEvent event) {
      if (!acceptDrag()) {
        event.doit = false;
      }
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
      if (event.data instanceof String && event.data == "") {
        event.doit = false;
        return;
      }
      TransferObject scoutTransferObject = handleSwtDragRequest();
      if (scoutTransferObject == null) {
        return;
      }
      SwtTransferObject[] swtTransferables = SwtUtility.createSwtTransferables(scoutTransferObject);
      if (swtTransferables.length == 0) {
        return;
      }
      Object data = swtTransferables[0].getData();
      if (data == null) {
        return;
      }
      event.data = data;
    }

  } // end class P_SwtDragSourceListener

  private class P_ScoutObjectPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          handleScoutProperty(evt.getPropertyName(), evt.getNewValue());
        }
      };
      m_environment.invokeSwtLater(job);
    }
  }
}