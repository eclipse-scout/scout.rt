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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Component;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JRootPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.window.internalframe.SwingScoutInternalFrame;

/**
 * Custom popup factory which should currently be used when mixing lightweight (swing) and heavyweight (awt/swt)
 * components.<br>
 * According to <a href="http://www.oracle.com/technetwork/articles/java/mixing-components-433992.html">this article</a>
 * the whole component hierarchy must be valid that lightweight components (popups and tooltips) which overlap
 * heavyweight components will be correctly painted.<br>
 * Unfortunately there are some components (namely {@link JScrollPaneEx} and the {@link JRootPane} in
 * {@link SwingScoutInternalFrame}) which do not forward their validate events as expected and some components stay
 * invalid. Therefore this factory calls {@link Component#validate()} on the root component before opening
 * (lightweight-) popups.
 */
public final class PopupFactoryEx extends PopupFactory {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PopupFactoryEx.class);

  /**
   * Instance of the custom popup factory which will be used when awt/swt components are displayed.
   */
  private static final PopupFactoryEx CUSTOM_POPUP_FACTORY = new PopupFactoryEx();

  /**
   * Instance of the original popup factory which will be used when all awt/swt components are closed.
   */
  private static final PopupFactory ORIGINAL_POPUP_FACTORY = getSharedInstance();

  /**
   * Counter which counts the number of activations and deactivations.
   */
  private static final AtomicInteger COUNTER = new AtomicInteger();

  private PopupFactoryEx() {
  }

  /**
   * <p>
   * Call this method to activate the custom popup factory when awt/swt components are desplayed. We don't need to
   * synchronzie here because these methods will be called from the same thread.
   * </p>
   * <p>
   * Please note that this request is put onto a stack meaning that you have to call
   * {@link P_FocusLostListener#deactivate()} as many times as you called {@link PopupFactoryEx#activate()} to use the
   * regular popup factory again.
   * </p>
   * <p>
   * <small>Counterpart of {@link PopupFactoryEx#deactivate()}.</small>
   * </p>
   */
  public static final void activate() {
    if (COUNTER.incrementAndGet() == 1) {
      LOG.debug("Activate custom popup factory to ensure, all swing components are validated before displaying a popup.");
      setSharedInstance(CUSTOM_POPUP_FACTORY);
    }
    else {
      LOG.debug("Custom popup factory already active, counter was incremented.");
    }
  }

  /**
   * <p>
   * Call this method to deactivate the custom popup factory when awt/swt components are disposed. We don't need to
   * synchronzie here because these methods will be called from the same thread.
   * </p>
   * <p>
   * Please note that this request is put onto a stack meaning that you have to call
   * {@link P_FocusLostListener#deactivate()} as many times as you called {@link PopupFactoryEx#activate()} to use the
   * regular popup factory again.
   * </p>
   * <p>
   * <small>Counterpart of {@link PopupFactoryEx#activate()}.</small>
   * </p>
   */
  public static final void deactivate() {
    if (COUNTER.decrementAndGet() <= 0) {
      LOG.debug("Deactivate custom popup factory.");
      setSharedInstance(ORIGINAL_POPUP_FACTORY);
    }
    else {
      LOG.debug("There are still awt/swt components displayed, counter was decremented.");
    }
  }

  @Override
  public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
    Component root = SwingUtilities.getRoot(owner);
    if (root != null) {
      root.validate();
    }
    return super.getPopup(owner, contents, x, y);
  }
}
