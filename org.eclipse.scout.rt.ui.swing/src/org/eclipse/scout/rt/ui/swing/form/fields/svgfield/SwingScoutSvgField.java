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
package org.eclipse.scout.rt.ui.swing.form.fields.svgfield;

/**
 * , Samuel Moser
 */
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.svgfield.ISvgField;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ScoutSVGModel;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;
import org.eclipse.scout.rt.ui.swing.form.fields.svgfield.internal.SvgViewer;
import org.eclipse.scout.rt.ui.swing.form.fields.svgfield.internal.SwingScoutSvgRenderer;

public class SwingScoutSvgField extends SwingScoutFieldComposite<ISvgField> implements ISwingScoutSvgField {

  public SwingScoutSvgField() {
  }

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    //
    if (getScoutObject().isLabelVisible()) {
      JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
      container.add(label);
      setSwingLabel(label);
    }
    //
    SvgViewer svgViewer = new SvgViewer();
    SwingUtility.installDefaultFocusHandling(svgViewer);
    svgViewer.addMouseListener(new P_SwingMouseListener());
    container.add(svgViewer);
    setSwingField(svgViewer);
    //
    setSwingContainer(container);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  @Override
  public SvgViewer getSwingField() {
    return (SvgViewer) super.getSwingField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    updateValueFromScout();
  }

  @Override
  protected void detachScout() {
    super.detachScout();
  }

  protected void updateValueFromScout() {
    ScoutSVGModel svg = getScoutObject().getValue();
    if (svg != null) {
      getSwingField().setSvgRenderer(new SwingScoutSvgRenderer(svg, getSwingField(), getSwingEnvironment()));
    }
    else {
      getSwingField().setSvgRenderer(null);
    }
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    float swingAlignX = SwingUtility.createAlignmentX(scoutAlign);
    getSwingField().setAlignmentX(swingAlignX);
  }

  @Override
  protected void setVerticalAlignmentFromScout(int scoutAlign) {
    float swingAlignY = SwingUtility.createAlignmentY(scoutAlign);
    getSwingField().setAlignmentY(swingAlignY);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ISvgField.PROP_VALUE)) {
      updateValueFromScout();
    }
  }

  protected void handleSwingClick(MouseEvent e) {
    final IScoutSVGElement svgElement = getSwingField().elementAtViewLocation(e.getX(), e.getY(), null);
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
    getSwingEnvironment().invokeScoutLater(t, 5678);
    // end notify
  }

  protected void handleSwingPopup(final MouseEvent e) {
    final IScoutSVGElement svgElement = getSwingField().elementAtViewLocation(e.getX(), e.getY(), null);
    if (svgElement == null) {
      return;
    }
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        IMenu[] a = getScoutObject().getUIFacade().fireElementPopupFromUI(svgElement);
        // call swing menu
        new SwingPopupWorker(getSwingEnvironment(), e.getComponent(), e.getPoint(), a).enqueue();
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 5678);
    // end notify
  }

  private class P_SwingMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      // Mac popup
      if (e.isPopupTrigger()) {
        handleSwingPopup(e);
        return;
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        handleSwingPopup(e);
        return;
      }
      if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
        handleSwingClick(e);
      }
    }
  }// end private class
}
