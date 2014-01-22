/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.action;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.basic.AbstractSwtScoutPropertyObserver;

/**
 * Common code for SWT elements rendering the Scout model {@link IAction}.
 * 
 * @since 3.10.0-M5
 */
public abstract class AbstractSwtScoutAction extends AbstractSwtScoutPropertyObserver<IAction> {

  /**
   * @param scoutAction
   * @param environment
   */
  public AbstractSwtScoutAction(IAction scoutAction, ISwtEnvironment environment) {
    setScoutObjectAndSwtEnvironment(scoutAction, environment);
  }

  @Override
  protected void applyScoutProperties() {
    IAction scoutAction = getScoutAction();
    setEnabledFromScout(scoutAction.isEnabled());
    setTextWithMnemonicFromScout(scoutAction.getTextWithMnemonic());
    setTooltipTextFromScout(scoutAction.getTooltipText());
    setIconFromScout(scoutAction.getIconId());
    setKeyStrokeFromScout(scoutAction.getKeyStroke());
  }

  /**
   * @param enabled
   *          value corresponding to {@link IAction#isEnabled()}
   */
  protected abstract void setEnabledFromScout(boolean enabled);

  /**
   * @param textWithMnemonic
   *          value corresponding to {@link IAction#getTextWithMnemonic()}
   */
  protected abstract void setTextWithMnemonicFromScout(String textWithMnemonic);

  /**
   * @param tooltipText
   *          value corresponding to {@link IAction#getTooltipText()}
   */
  protected abstract void setTooltipTextFromScout(String tooltipText);

  /**
   * @param iconId
   *          value corresponding to {@link IAction#getIconId()}
   */
  protected abstract void setIconFromScout(String iconId);

  /**
   * @param keyStroke
   *          value corresponding to {@link IAction#getKeyStroke()}
   */
  protected abstract void setKeyStrokeFromScout(String keyStroke);

  /**
   * Convenience for {@link #getScoutObject()}.
   */
  public IAction getScoutAction() {
    return getScoutObject();
  }

  /**
   * in swt thread
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(IAction.PROP_ENABLED)) {
      setEnabledFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IAction.PROP_TEXT_WITH_MNEMONIC)) {
      setTextWithMnemonicFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_TOOLTIP_TEXT)) {
      setTooltipTextFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_ICON_ID)) {
      setIconFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_KEYSTROKE)) {
      setKeyStrokeFromScout((String) newValue);
    }
  }
}
