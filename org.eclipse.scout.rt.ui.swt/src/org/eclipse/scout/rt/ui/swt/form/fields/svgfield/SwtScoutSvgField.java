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
package org.eclipse.scout.rt.ui.swt.form.fields.svgfield;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.svgfield.ISvgField;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ScoutSVGModel;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.form.fields.svgfield.internal.SvgViewer;
import org.eclipse.scout.rt.ui.swt.form.fields.svgfield.internal.SwtScoutSvgRenderer;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class SwtScoutSvgField extends SwtScoutFieldComposite<ISvgField> implements ISwtScoutSvgField {

  private Point m_contextMenuMouseLocation;
  private Menu m_contextMenu;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    if (getScoutObject().isLabelVisible()) {
      int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
      StatusLabelEx label = new StatusLabelEx(container, labelStyle, getEnvironment());
      getEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);
      setSwtLabel(label);
    }
    //
    SvgViewer viewer = new SvgViewer(container);
    getEnvironment().getFormToolkit().adapt(viewer, false, false);
    setSwtField(viewer);
    //cleanup
    viewer.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });
    //mouse listener
    viewer.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        m_contextMenuMouseLocation = new Point(e.x, e.y);
      }

      @Override
      public void mouseUp(MouseEvent e) {
        m_contextMenuMouseLocation = new Point(e.x, e.y);
        if (e.button == 1) {
          handleSwtClick(e);
        }
      }
    });
    //context menu
    m_contextMenu = new Menu(viewer.getShell(), SWT.POP_UP);
    m_contextMenu.addMenuListener(new MenuAdapter() {
      @Override
      public void menuShown(MenuEvent e) {
        if (m_contextMenuMouseLocation != null) {
          handleSwtPopup(e, m_contextMenuMouseLocation);
        }
      }
    });
    viewer.setMenu(m_contextMenu);
    //
    setSwtContainer(container);
    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));

  }

  private void freeResources() {
    if (m_contextMenu != null && !m_contextMenu.isDisposed()) {
      m_contextMenu.dispose();
    }
  }

  @Override
  public SvgViewer getSwtField() {
    return (SvgViewer) super.getSwtField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    getSwtField().setAlignmentX(SwtUtility.getHorizontalAlignment(getScoutObject().getGridData().horizontalAlignment));
    getSwtField().setAlignmentY(SwtUtility.getHorizontalAlignment(getScoutObject().getGridData().verticalAlignment));
    updateValueFromScout();
  }

  protected void updateValueFromScout() {
    ScoutSVGModel svg = getScoutObject().getValue();
    if (svg != null) {
      getSwtField().setSvgRenderer(new SwtScoutSvgRenderer(svg, getSwtField(), getEnvironment()));
    }
    else {
      getSwtField().setSvgRenderer(null);
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ISvgField.PROP_VALUE)) {
      updateValueFromScout();
    }
  }

  protected void handleSwtClick(MouseEvent e) {
    final IScoutSVGElement svgElement = getSwtField().elementAtViewLocation(e.x, e.y, null);
    if (svgElement == null) {
      return;
    }
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireElementClickFromUI(svgElement);
      }
    };
    getEnvironment().invokeScoutLater(t, 5678);
    // end notify
  }

  protected void handleSwtPopup(final MenuEvent e, Point mouseLocation) {
    final IScoutSVGElement svgElement = getSwtField().elementAtViewLocation(mouseLocation.x, mouseLocation.y, null);
    if (svgElement == null) {
      return;
    }
    // clear all previous
    // Windows BUG: fires menu hide before the selection on the menu item is
    // propagated.
    if (m_contextMenu != null) {
      for (MenuItem item : m_contextMenu.getItems()) {
        disposeMenuItem(item);
      }
    }
    final AtomicReference<IMenu[]> scoutMenusRef = new AtomicReference<IMenu[]>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        scoutMenusRef.set(getScoutObject().getUIFacade().fireElementPopupFromUI(svgElement));
      }
    };
    JobEx job = getEnvironment().invokeScoutLater(t, 1200);
    try {
      job.join(1200);
    }
    catch (InterruptedException ex) {
      //nop
    }
    // grab the actions out of the job, when the actions are providden
    // within the scheduled time the popup will be handled.
    if (scoutMenusRef.get() != null) {
      SwtMenuUtility.fillContextMenu(scoutMenusRef.get(), m_contextMenu, getEnvironment());
    }
  }

  private void disposeMenuItem(MenuItem item) {
    Menu menu = item.getMenu();
    if (menu != null) {
      for (MenuItem childItem : menu.getItems()) {
        disposeMenuItem(childItem);
      }
      menu.dispose();
    }
    item.dispose();
  }
}
