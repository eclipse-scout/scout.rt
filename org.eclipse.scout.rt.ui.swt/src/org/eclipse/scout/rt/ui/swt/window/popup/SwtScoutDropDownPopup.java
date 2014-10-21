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
package org.eclipse.scout.rt.ui.swt.window.popup;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swt.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.IValidateRoot;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

/**
 * <p>
 * Popup window bound to a {@link Control}.
 * </p>
 * Additionaly, a {@link SWT#Dispose}-event is fired when the {@link Control ownerComponent} or one of its ancestors is
 * being scrolled, resized or moved.
 */
public class SwtScoutDropDownPopup extends SwtScoutPopup {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutDropDownPopup.class);

  private final Listener m_viewPortChangedListener = new P_ViewPortChangedListener();
  private Set<ScrollBar> scrollBars;

  /**
   * @param env
   *          {@link ISwtEnvironment}.
   * @param ownerComponent
   *          the {@link Control} the popup is bound to.
   * @param takeFocusOnOpen
   *          A boolean indicating whether focus should be taken by this popup when it opens.
   * @param style
   *          the style of control to construct
   */
  public SwtScoutDropDownPopup(ISwtEnvironment env, Control ownerComponent, boolean takeFocusOnOpen, int style) {
    super(env, ownerComponent, takeFocusOnOpen, style);

    getShell().setData(IValidateRoot.VALIDATE_ROOT_DATA, new DefaultValidateRoot(getShell()) {
      @Override
      public void validate() {
        autoAdjustBounds();
      }
    });
  }

  @Override
  protected void onPopupOpened() {
    // Install listener to close the popup if being scrolled.
    scrollBars = findScrollbarsInHierarchy(getOwnerComponent());
    installScrollListeners(scrollBars, m_viewPortChangedListener);

    // Install listener to close the popup if being moved or resized.
    getOwnerComponent().getShell().addListener(SWT.Move, m_viewPortChangedListener);
    getOwnerComponent().getShell().addListener(SWT.Resize, m_viewPortChangedListener);

    super.onPopupOpened();
  }

  @Override
  protected void onPopupClosed() {
    // Uninstall listener to close the popup if being scrolled.
    uninstallScrollListeners(scrollBars, m_viewPortChangedListener);
    scrollBars = null;

    // Uninstall listener to close the popup if being moved or resized.
    Shell shell = getOwnerShell();
    if (shell != null) {
      getOwnerComponent().getShell().removeListener(SWT.Move, m_viewPortChangedListener);
      getOwnerComponent().getShell().removeListener(SWT.Resize, m_viewPortChangedListener);
    }

    super.onPopupClosed();
  }

  /**
   * Adds the given listener to the given scrollbars.
   */
  private static void installScrollListeners(Set<ScrollBar> scrollBars, Listener listener) {
    for (ScrollBar scrollBar : scrollBars) {
      scrollBar.addListener(SWT.Selection, listener);
    }
  }

  /**
   * Removes the given listener from them given scrollbars.
   */
  private static void uninstallScrollListeners(Set<ScrollBar> scrollBars, Listener listener) {
    if (scrollBars == null) {
      return;
    }
    for (ScrollBar scrollBar : scrollBars) {
      if (!scrollBar.isDisposed()) {
        scrollBar.removeListener(SWT.Selection, listener);
      }
    }
  }

  /**
   * Finds the scrollbars in the ancestor hierarchy of the given control.
   */
  private static Set<ScrollBar> findScrollbarsInHierarchy(Control control) {
    final Set<ScrollBar> scrollBars = new HashSet<ScrollBar>();
    if (!(control instanceof Composite)) {
      return Collections.emptySet();
    }

    final Composite composite = (Composite) control;

    final ScrollBar hBar = composite.getHorizontalBar();
    if (hBar != null) {
      scrollBars.add(hBar);
    }
    final ScrollBar vBar = composite.getVerticalBar();
    if (vBar != null) {
      scrollBars.add(vBar);
    }

    scrollBars.addAll(findScrollbarsInHierarchy(control.getParent()));

    return scrollBars;
  }

  /**
   * @return Shell of the owner component or <code>null</code> if already disposed.
   */
  private Shell getOwnerShell() {
    final Control ownerComponent = getOwnerComponent();
    if (ownerComponent.isDisposed()) {
      return null;
    }
    final Shell shell = ownerComponent.getShell();
    if (shell.isDisposed()) {
      return null;
    }
    return shell;
  }

  private class P_ViewPortChangedListener implements Listener {

    @Override
    public void handleEvent(Event event) {
      if (getShell().isDisposed()) {
        return;
      }
      // viewport changes are propagated as Shell deactivation events.
      event.type = SWT.Deactivate;
      getShell().notifyListeners(SWT.Deactivate, event);
    }
  }
}
