/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.mobile.action;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.swt.SWT;

/**
 * @since 3.9.0
 */
public interface IRwtScoutActionButton extends IRwtScoutComposite<IAction> {

  /**
   * Returns either{@link SWT#DOWN} or {@link SWT#UP}.
   */
  public int getMenuOpeningDirection();

  /**
   * Accepts {@link SWT#DOWN} or {@link SWT#UP}.<br/>
   * Default is {@link SWT#DOWN}
   */
  public void setMenuOpeningDirection(int menuOpeningDirection);

}
