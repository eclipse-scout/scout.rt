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
package org.eclipse.scout.rt.ui.rap.mobile.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.9.0
 */
public class ActionButtonBar extends Composite {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ActionButtonBar.class);

  public static final int ORIENTATION_LEFT_TO_RIGHT = SWT.LEFT_TO_RIGHT;
  public static final int ORIENTATION_RIGHT_TO_LEFT = 1 << 26;

  private static final long serialVersionUID = 1L;
  private static final String VARIANT_ACTION_BAR = "actionBar";
  private static final int BUTTON_SPACING = 6;

  private IMenu[] m_menus;
  private List<IMenu> m_displayedMenus;
  private IRwtEnvironment m_uiEnvironment;
  private Job m_handleButtonPilingJob;

  private Composite m_buttonBar;
  private int m_style;
  private boolean m_pilingEnabled;
  private int m_minNumberOfAlwaysVisibleButtons;
  private int m_maxNumberOfAlwaysVisibleButtons;
  private P_ScoutPropertyChangeListener m_scoutPropertyChangeListener;

  public ActionButtonBar(Composite parent, IRwtEnvironment uiEnvironment, IMenu[] menus) {
    this(parent, uiEnvironment, menus, SWT.DEFAULT);
  }

  /**
   * @param style
   *          Style flag to set the horizontalAlignment of the action bar. <br/>
   *          Accepted values: {@link SWT#LEFT}, {@link SWT#CENTER}, {@link SWT#RIGHT}
   */
  public ActionButtonBar(Composite parent, IRwtEnvironment uiEnvironment, IMenu[] menus, int style) {
    super(parent, SWT.NONE);

    m_menus = menus;
    m_uiEnvironment = uiEnvironment;
    if (style == SWT.DEFAULT) {
      style = SWT.LEFT;
    }
    m_style = style;

    m_pilingEnabled = true;
    m_minNumberOfAlwaysVisibleButtons = 0;
    m_maxNumberOfAlwaysVisibleButtons = Integer.MAX_VALUE;

    m_displayedMenus = new LinkedList<IMenu>();
    m_displayedMenus.addAll(Arrays.asList(m_menus));

    if (menus != null && menus.length > 0) {
      // attaching the listeners must happen before creating the buttons to avoid events getting lost
      attachScoutPropertyChangeListener();
    }

    createButtonBar();

    if (menus != null && menus.length > 0) {
      addListener(SWT.Resize, new P_ResizeListener());
      scheduleHandleButtonPilingInUiThread();
    }

    initLayout(this);
  }

  private void attachScoutPropertyChangeListener() {
    if (m_menus == null || m_menus.length == 0) {
      return;
    }

    if (m_scoutPropertyChangeListener != null) {
      return;
    }

    m_scoutPropertyChangeListener = new P_ScoutPropertyChangeListener();

    for (IMenu menu : m_menus) {
      menu.addPropertyChangeListener(m_scoutPropertyChangeListener);
    }

    addDisposeListener(new DisposeListener() {

      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent event) {
        detachScoutPropertyChangeListener();
      }

    });
  }

  private void detachScoutPropertyChangeListener() {
    if (m_menus == null || m_menus.length == 0) {
      return;
    }

    if (m_scoutPropertyChangeListener == null) {
      return;
    }

    for (IMenu menu : m_menus) {
      menu.removePropertyChangeListener(m_scoutPropertyChangeListener);
    }

    m_scoutPropertyChangeListener = null;
  }

  private void initLayout(Composite composite) {
    GridLayout gridLayout = RwtLayoutUtility.createGridLayoutNoSpacing(1, true);
    composite.setLayout(gridLayout);
  }

  private void rebuildButtonBar() {
    if (m_buttonBar != null) {
      m_buttonBar.dispose();
    }

    createButtonBar();

    getParent().layout(true, true);
  }

  private void createButtonBar() {
    m_buttonBar = createButtonBar(this, m_displayedMenus);
  }

  private Composite createButtonBar(Composite parent, List<IMenu> actions) {
    Composite buttonBar = getUiEnvironment().getFormToolkit().createComposite(parent);
    buttonBar.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_ACTION_BAR);
    initButtonBarLayout(buttonBar);

    createButtons(buttonBar, actions);

    return buttonBar;
  }

  public int getActionsOrientation() {
    if ((m_style & ORIENTATION_RIGHT_TO_LEFT) != 0) {
      return ORIENTATION_RIGHT_TO_LEFT;
    }

    return ORIENTATION_LEFT_TO_RIGHT;
  }

  private void initButtonBarLayout(Composite buttonBar) {
    RowLayout layout = new RowLayout(SWT.HORIZONTAL);
    layout.marginBottom = 5;
    layout.marginTop = 5;
    layout.marginLeft = 5;
    layout.marginRight = 5;
    layout.spacing = BUTTON_SPACING;
    layout.wrap = false;
    buttonBar.setLayout(layout);

    int horizontalAlignment = getHorizontalAlignment();
    buttonBar.setLayoutData(new GridData(horizontalAlignment, SWT.CENTER, true, true));
  }

  private int getHorizontalAlignment() {
    if ((m_style & SWT.LEFT) != 0) {
      return SWT.LEFT;
    }
    else if ((m_style & SWT.CENTER) != 0) {
      return SWT.CENTER;
    }
    else if ((m_style & SWT.RIGHT) != 0) {
      return SWT.RIGHT;
    }

    return SWT.DEFAULT;
  }

  private void createButtons(Composite buttonBar, List<IMenu> actions) {
    if (actions.size() == 0 || !isAnyActionVisible(actions)) {
      return;
    }

    if (getActionsOrientation() == ORIENTATION_RIGHT_TO_LEFT) {
      actions = reverseActions(actions);
    }

    for (IMenu menu : actions) {
      createButton(buttonBar, menu);
    }
  }

  private List<IMenu> reverseActions(List<IMenu> actions) {
    List<IMenu> reversedMenuList = new LinkedList<IMenu>(actions);
    Collections.reverse(reversedMenuList);

    return reversedMenuList;
  }

  protected void createButton(Composite parent, IAction action) {
    if (action == null) {
      return;
    }
    if (action.isSeparator()) {
      return;
    }
    if (!action.isVisible()) {
      return;
    }
    if (RwtMenuUtility.hasChildActions(action) && !RwtMenuUtility.hasVisibleChildActions(action)) {
      return;
    }

    IRwtScoutActionButton button = new RwtScoutActionButton();
    button.setMenuOpeningDirection(getMenuOpeningDirection());
    button.createUiField(parent, action, getUiEnvironment());
  }

  protected int getMenuOpeningDirection() {
    if ((m_style & SWT.UP) != 0) {
      return SWT.UP;
    }

    return SWT.DOWN;
  }

  public void handleButtonPiling() {
    if (isButtonBarTooSmall(m_buttonBar) || getNumberOfDisplayedButtons() > m_maxNumberOfAlwaysVisibleButtons) {
      pileButtons();
    }
    else {
      breakPileButtonsApart();
    }
  }

  private void scheduleHandleButtonPilingInUiThread() {
    if (m_buttonBar == null || m_buttonBar.isDisposed()) {
      return;
    }

    getUiEnvironment().getDisplay().asyncExec(new Runnable() {

      @Override
      public void run() {
        if (m_buttonBar == null || m_buttonBar.isDisposed()) {
          return;
        }

        handleButtonPiling();
      }

    });
  }

  private void breakPileButtonsApart() {
    boolean brokeApart = false;

    do {
      brokeApart = breakPileButtonApart();
    }
    while (brokeApart);
  }

  private void pileButtons() {
    boolean stackingSuccessful = false;

    do {
      stackingSuccessful = createPileButton();
    }
    while (stackingSuccessful && (isButtonBarTooSmall(m_buttonBar) || getNumberOfDisplayedButtons() > m_maxNumberOfAlwaysVisibleButtons));
  }

  private boolean isButtonBarTooSmall(Composite buttonBar) {
    Rectangle actualSize = buttonBar.getBounds();
    Point preferredSize = buttonBar.computeSize(SWT.DEFAULT, actualSize.height, false);

    if (actualSize.width < preferredSize.x) {
      return true;
    }

    return false;
  }

  protected boolean breakPileButtonApart() {
    if (m_displayedMenus == null || m_displayedMenus.size() == 0) {
      return false;
    }

    if (getNumberOfDisplayedButtons() >= m_maxNumberOfAlwaysVisibleButtons) {
      return false;
    }

    List<IMenu> displayedMenusCopy = new LinkedList<IMenu>();
    displayedMenusCopy.addAll(m_displayedMenus);

    IMenu lastDisplayedMenu = displayedMenusCopy.remove(displayedMenusCopy.size() - 1);
    if (!(lastDisplayedMenu instanceof PileMenu)) {
      return false;
    }

    PileMenu pileMenu = (PileMenu) lastDisplayedMenu;

    List<IMenu> currentPileMenus = pileMenu.getChildActions();
    IMenu firstMenu = currentPileMenus.remove(0);
    displayedMenusCopy.add(firstMenu);

    if (currentPileMenus.size() > 0) {
      PileMenu newPileMenu = createPileMenu(currentPileMenus);
      displayedMenusCopy.add(newPileMenu);
    }

    if (isEnoughSpaceToBreakApart(displayedMenusCopy)) {
      m_displayedMenus = displayedMenusCopy;
      rebuildButtonBar();
      return true;
    }

    return false;
  }

  /**
   * Creates and layouts an invisible composite to compute if there is enough space to fully display the buttons for the
   * given actions.
   */
  private boolean isEnoughSpaceToBreakApart(List<IMenu> menusToDisplay) {
    Shell shell = new Shell();
    try {
      shell.setSize(getBounds().width, getBounds().height);
      initLayout(shell);

      Composite buttonBar = createButtonBar(shell, menusToDisplay);

      shell.layout();

      return (!isButtonBarTooSmall(buttonBar));
    }
    finally {
      shell.dispose();
    }
  }

  private PileMenu createPileMenu(final List<IMenu> childActions) {
    final Holder<PileMenu> result = new Holder<PileMenu>(PileMenu.class);

    // Synchronize with model thread to create model element.
    JobEx job = new ClientSyncJob("PileMenu", getUiEnvironment().getClientSession()) {

      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        PileMenu pileMenu = new PileMenu();
        pileMenu.setChildActions(childActions);
        if (getMenuOpeningDirection() == SWT.UP) {
          pileMenu.setIconId(Icons.MoreActionsUp);
        }
        else {
          pileMenu.setIconId(Icons.MoreActionsDown);
        }

        result.setValue(pileMenu);
      }
    };
    job.schedule();
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for the PileMenu model to be created.", e);
    }

    return result.getValue();
  }

  protected boolean createPileButton() {
    if (!isPilingAllowed()) {
      return false;
    }

    List<IMenu> newPiledActions = new LinkedList<IMenu>();
    IMenu lastMenu = m_displayedMenus.remove(m_displayedMenus.size() - 1);
    if (lastMenu instanceof PileMenu) {
      if (m_displayedMenus.size() > getMinNumberOfAlwaysVisibleButtons()) {
        IMenu secondLastMenu = m_displayedMenus.remove(m_displayedMenus.size() - 1);
        newPiledActions.add(secondLastMenu);
      }
      //add every existing piled actions to the new list
      newPiledActions.addAll(lastMenu.getChildActions());
    }
    else {
      newPiledActions.add(lastMenu);
    }

    IMenu pileMenu = createPileMenu(newPiledActions);
    m_displayedMenus.add(pileMenu);

    rebuildButtonBar();

    return true;
  }

  private boolean isPilingAllowed() {
    if (!isPilingEnabled()) {
      return false;
    }

    if (m_displayedMenus.size() == 0) {
      return false;
    }

    if (getNumberOfDisplayedButtons() <= getMinNumberOfAlwaysVisibleButtons()) {
      return false;
    }

    return true;
  }

  /**
   * @return The number of currently displayed buttons. Pile buttons and separators are not counted.
   */
  private int getNumberOfDisplayedButtons() {
    int size = 0;
    for (IAction action : m_displayedMenus) {
      if (!action.isSeparator() && !(action instanceof PileMenu)) {
        size++;
      }
    }

    return size;
  }

  private class PileMenu extends AbstractMenu {

  }

  public int getMinNumberOfAlwaysVisibleButtons() {
    return m_minNumberOfAlwaysVisibleButtons;
  }

  public void setMinNumberOfAlwaysVisibleButtons(int minNumberOfAlwaysVisibleButtons) {
    m_minNumberOfAlwaysVisibleButtons = minNumberOfAlwaysVisibleButtons;
  }

  public void setMaxNumberOfAlwaysVisibleButtons(int maxNumberOfAlwaysVisibleButtons) {
    m_maxNumberOfAlwaysVisibleButtons = maxNumberOfAlwaysVisibleButtons;
  }

  public int getMaxNumberOfAlwaysVisibleButtons() {
    return m_maxNumberOfAlwaysVisibleButtons;
  }

  public void setPilingEnabled(boolean pilingEnabled) {
    m_pilingEnabled = pilingEnabled;
  }

  public boolean isPilingEnabled() {
    return m_pilingEnabled;
  }

  public IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  public IMenu[] getMenus() {
    return m_menus;
  }

  public boolean hasButtons() {
    return getMenus() != null && getMenus().length > 0;
  }

  public boolean isEqualMenuList(List<IMenu> menuList) {
    if (getMenus() == null || menuList == null) {
      return false;
    }

    return menuList.equals(Arrays.asList(getMenus()));
  }

  public boolean isAnyActionVisible(List<IMenu> menuList) {
    if (menuList == null) {
      return false;
    }

    for (IMenu menu : menuList) {
      if (menu.isVisible()) {
        return true;
      }
    }

    return false;
  }

  private void setActionVisibleFromScout(IAction action, boolean visible) {
    rebuildButtonBar();
  }

  private void handleScoutPropertyChange(final PropertyChangeEvent evt) {
    if (IAction.PROP_VISIBLE.equals(evt.getPropertyName())) {
      IAction action = (IAction) evt.getSource();
      setActionVisibleFromScout(action, (Boolean) evt.getNewValue());
    }
  }

  private class P_ScoutPropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          if (isDisposed()) {
            return;
          }

          handleScoutPropertyChange(evt);
        }
      };
      getUiEnvironment().invokeUiLater(t);
    }

  }

  private class P_ResizeListener implements Listener {

    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      if (event.type == SWT.Resize) {
        buttonBarResized();
      }
    }

    private void buttonBarResized() {
      if (m_buttonBar == null || m_buttonBar.isDisposed()) {
        return;
      }

      if (m_handleButtonPilingJob != null) {
        m_handleButtonPilingJob.cancel();
      }

      m_handleButtonPilingJob = new Job("Handling button piling") {

        @Override
        protected IStatus run(IProgressMonitor monitor) {
          scheduleHandleButtonPilingInUiThread();

          return Status.OK_STATUS;
        }

      };

      // Pile handling is executed delayed to avoid flickering during resize and to avoid too many attempts to compute the necessity of the piling.
      m_handleButtonPilingJob.setSystem(true);
      m_handleButtonPilingJob.schedule(200);
    }

  }

}
