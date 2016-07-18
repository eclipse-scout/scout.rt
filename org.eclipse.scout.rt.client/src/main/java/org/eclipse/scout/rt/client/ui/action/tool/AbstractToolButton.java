/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.tool;

import org.eclipse.scout.rt.client.extension.ui.action.tool.IToolButtonExtension;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

/**
 * @deprecated use {@link AbstractMenu} instead, will be removed in Scout 6.1
 */
@Deprecated
@SuppressWarnings("deprecation")
public abstract class AbstractToolButton extends AbstractMenu implements IMenu {

  public AbstractToolButton() {
    super();
  }

  public AbstractToolButton(boolean callInitializer) {
    super(callInitializer);
  }

  protected static class LocalToolButtonExtension<OWNER extends AbstractToolButton> extends LocalMenuExtension<OWNER> implements IToolButtonExtension<OWNER> {

    public LocalToolButtonExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IToolButtonExtension<? extends AbstractToolButton> createLocalExtension() {
    return new LocalToolButtonExtension<AbstractToolButton>(this);
  }
}
