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
package org.eclipse.scout.rt.ui.swing;

import java.awt.Color;
import java.awt.Frame;

import javax.swing.JComponent;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;
import org.eclipse.scout.rt.ui.swing.action.LegacySwingScoutActionButton;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.tablefield.ISwingScoutTableField;
import org.eclipse.scout.rt.ui.swing.form.fields.tablefield.LegacySwingScoutTableField;
import org.eclipse.scout.rt.ui.swing.window.desktop.ISwingScoutRootFrame;
import org.eclipse.scout.rt.ui.swing.window.desktop.LegacySwingScoutRootFrame;

/**
 *
 */
public class LegacySwingEnvironment extends AbstractSwingEnvironment {
  /*
   * (non-Javadoc)
   * @see org.eclipse.scout.rt.ui.swing.DefaultSwingEnvironment#createRootComposite(java.awt.Frame, org.eclipse.scout.rt.client.ui.desktop.IDesktop)
   */
  @Override
  public ISwingScoutRootFrame createRootComposite(Frame rootFrame, IDesktop desktop) {
    ISwingScoutRootFrame ui = new LegacySwingScoutRootFrame(this, rootFrame, desktop);
    decorate(desktop, ui);
    return ui;
  }

  @Override
  public ISwingScoutFormField createFormField(JComponent parent, IFormField field) {
    if (field instanceof ITableField) {
      ISwingScoutTableField ui = new LegacySwingScoutTableField();
      ui.createField((ITableField<?>) field, this);
      decorate(field, ui);
      return ui;
    }
    else {
      return super.createFormField(parent, field);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.scout.rt.ui.swing.DefaultSwingEnvironment#createAction(javax.swing.JComponent, org.eclipse.scout.rt.client.ui.action.IAction)
   */
  @Override
  public ISwingScoutAction createAction(JComponent parent, IAction action) {
    if (action instanceof IToolButton) {
      ISwingScoutAction<IToolButton> ui = new LegacySwingScoutActionButton<IToolButton>();
      ui.createField((IToolButton) action, this);
      decorate(action, ui);
      return ui;
    }
    else if (action instanceof IViewButton) {
      ISwingScoutAction<IViewButton> ui = new LegacySwingScoutActionButton<IViewButton>();
      ui.createField((IViewButton) action, this);
      decorate(action, ui);
      return ui;
    }
    else {
      return super.createAction(parent, action);
    }
  }

  @Override
  public int getProcessButtonHeight() {
    return 23;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.scout.rt.ui.swing.DefaultSwingEnvironment#decorate(java.lang.Object, java.lang.Object)
   */
  @Override
  protected void decorate(Object scoutObject, Object swingScoutComposite) {
    super.decorate(scoutObject, swingScoutComposite);
    if (swingScoutComposite instanceof LegacySwingScoutActionButton) {
      ((LegacySwingScoutActionButton) swingScoutComposite).getSwingField().setForeground(Color.white);
      ((LegacySwingScoutActionButton) swingScoutComposite).getSwingField().setContentAreaFilled(false);
    }
  }

}
