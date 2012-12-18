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
package org.eclipse.scout.rt.ui.rap.window.desktop.toolbar;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class RwtScoutMainMenuButton extends RwtScoutComposite<IDesktop> implements IRwtScoutMainMenuButton {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutMainMenuButton.class);

  private Button m_menuButton;

  private static final String VARIANT_TOOLBAR_MENU_BUTTON = "menuButton";

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);

    createMenu(container);

    //layout
    FillLayout containerLayout = new FillLayout();
    container.setLayout(containerLayout);

    setUiContainer(container);
  }

  protected Control createMenu(Composite parent) {
    m_menuButton = getUiEnvironment().getFormToolkit().createButton(parent, "", SWT.PUSH);
    m_menuButton.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        Rectangle buttonBounds = m_menuButton.getBounds();
        Point buttonLocation = m_menuButton.toDisplay(buttonBounds.x, buttonBounds.y);
        if (getScoutObject() != null) {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutObject().prepareAllMenus();
            }
          };

          JobEx job = getUiEnvironment().invokeScoutLater(t, 5000);
          try {
            job.join(5000);
          }
          catch (InterruptedException ex) {
            LOG.warn("Exception occured while preparing all menus.", ex);
          }
        }
        Menu menu = m_menuButton.getMenu();
        menu.setLocation(new Point(buttonLocation.x, buttonLocation.y + buttonBounds.height));
        menu.setVisible(true);
      }
    });
    m_menuButton.setData(RWT.CUSTOM_VARIANT, VARIANT_TOOLBAR_MENU_BUTTON);
    Menu contextMenu = new Menu(m_menuButton.getShell(), SWT.POP_UP);
    RwtMenuUtility.fillContextMenu(getScoutObject().getMenus(), getUiEnvironment(), contextMenu);
    m_menuButton.setMenu(contextMenu);

    return m_menuButton;
  }
}
