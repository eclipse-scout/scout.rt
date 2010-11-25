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
package org.eclipse.scout.rt.ui.swing.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.Serializable;
import java.util.TooManyListenersException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.UIResource;

public class DefaultDropTarget extends DropTarget implements UIResource {
  private static final long serialVersionUID = 1L;
  private EventListenerList m_eventListenerList;

  public DefaultDropTarget(JComponent c) {
    super();
    setComponent(c);
    try {
      super.addDropTargetListener(new P_DefaultDropHandler());
    }
    catch (TooManyListenersException tmle) {
    }
  }

  @Override
  public void addDropTargetListener(DropTargetListener listener) throws TooManyListenersException {
    // The super class only supports 1 DropTargetListener, the 1 from the constructor is already added, so all others are added to the event list
    if (m_eventListenerList == null) {
      m_eventListenerList = new EventListenerList();
    }
    m_eventListenerList.add(DropTargetListener.class, listener);
  }

  @Override
  public void removeDropTargetListener(DropTargetListener listener) {
    if (m_eventListenerList != null) {
      m_eventListenerList.remove(DropTargetListener.class, listener);
    }
  }

  @Override
  public void dragEnter(DropTargetDragEvent evt) {
    super.dragEnter(evt);
    if (m_eventListenerList != null) {
      Object[] dropTargetListeners = m_eventListenerList.getListenerList();
      for (int k = dropTargetListeners.length - 2; k >= 0; k -= 2) {
        if (dropTargetListeners[k] == DropTargetListener.class) {
          ((DropTargetListener) dropTargetListeners[k + 1]).dragEnter(evt);
        }
      }
    }
  }

  @Override
  public void dragOver(DropTargetDragEvent evt) {
    super.dragOver(evt);
    if (m_eventListenerList != null) {
      Object[] dropTargetListeners = m_eventListenerList.getListenerList();
      for (int k = dropTargetListeners.length - 2; k >= 0; k -= 2) {
        if (dropTargetListeners[k] == DropTargetListener.class) {
          ((DropTargetListener) dropTargetListeners[k + 1]).dragOver(evt);
        }
      }
    }
  }

  @Override
  public void dragExit(DropTargetEvent evt) {
    super.dragExit(evt);
    if (m_eventListenerList != null) {
      Object[] dropTargetListeners = m_eventListenerList.getListenerList();
      for (int k = dropTargetListeners.length - 2; k >= 0; k -= 2) {
        if (dropTargetListeners[k] == DropTargetListener.class) {
          ((DropTargetListener) dropTargetListeners[k + 1]).dragExit(evt);
        }
      }
    }
  }

  @Override
  public void drop(DropTargetDropEvent evt) {
    super.drop(evt);
    if (m_eventListenerList != null) {
      Object[] dropTargetListeners = m_eventListenerList.getListenerList();
      for (int k = dropTargetListeners.length - 2; k >= 0; k -= 2) {
        if (dropTargetListeners[k] == DropTargetListener.class) {
          ((DropTargetListener) dropTargetListeners[k + 1]).drop(evt);
        }
      }
    }
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent evt) {
    super.dropActionChanged(evt);
    if (m_eventListenerList != null) {
      Object[] dropTargetListeners = m_eventListenerList.getListenerList();
      for (int k = dropTargetListeners.length - 2; k >= 0; k -= 2) {
        if (dropTargetListeners[k] == DropTargetListener.class) {
          ((DropTargetListener) dropTargetListeners[k + 1]).dropActionChanged(evt);
        }
      }
    }
  }

  private static class P_DefaultDropHandler implements DropTargetListener, Serializable {
    private static final long serialVersionUID = 1L;

    private boolean m_canImport;

    private boolean actionSupported(int a) {
      int mask = a & (DnDConstants.ACTION_LINK | DnDConstants.ACTION_COPY_OR_MOVE);
      return mask != DnDConstants.ACTION_NONE;
    }

    public void dragEnter(DropTargetDragEvent evt) {
      DataFlavor[] currentFlavors = evt.getCurrentDataFlavors();
      JComponent jcomponent = (JComponent) evt.getDropTargetContext().getComponent();
      TransferHandler transferHandler = jcomponent.getTransferHandler();
      if (transferHandler != null && transferHandler.canImport(jcomponent, currentFlavors)) {
        m_canImport = true;
      }
      else {
        m_canImport = false;
      }
      int eventDropAction = evt.getDropAction();
      if (m_canImport && actionSupported(eventDropAction)) {
        evt.acceptDrag(eventDropAction);
      }
      else {
        evt.rejectDrag();
      }
    }

    public void dragOver(DropTargetDragEvent evt) {
      int eventDropAction = evt.getDropAction();
      if (m_canImport && actionSupported(eventDropAction)) {
        evt.acceptDrag(eventDropAction);
      }
      else {
        evt.rejectDrag();
      }
    }

    public void dragExit(DropTargetEvent evt) {
      //nop
    }

    public void drop(DropTargetDropEvent evt) {
      int eventDropAction = evt.getDropAction();
      JComponent jcomp = (JComponent) evt.getDropTargetContext().getComponent();
      TransferHandler transferHandler = jcomp.getTransferHandler();
      if (m_canImport && transferHandler != null && actionSupported(eventDropAction)) {
        evt.acceptDrop(DnDConstants.ACTION_COPY);
        try {
          Transferable t = evt.getTransferable();
          if (transferHandler instanceof TransferHandlerEx) {
            evt.dropComplete(((TransferHandlerEx) transferHandler).importDataEx(jcomp, t, evt.getLocation()));
          }
          else {
            evt.dropComplete(transferHandler.importData(jcomp, t));
          }
        }
        catch (RuntimeException re) {
          evt.dropComplete(false);
        }
      }
      else {
        evt.rejectDrop();
      }
    }

    public void dropActionChanged(DropTargetDragEvent evt) {
      int eventDropAction = evt.getDropAction();
      if (m_canImport && actionSupported(eventDropAction)) {
        evt.acceptDrag(eventDropAction);
      }
      else {
        evt.rejectDrag();
      }
    }
  }
}
