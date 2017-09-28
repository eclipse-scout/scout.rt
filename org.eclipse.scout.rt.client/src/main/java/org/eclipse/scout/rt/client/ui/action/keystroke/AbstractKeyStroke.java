/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.keystroke;

import org.eclipse.scout.rt.client.extension.ui.action.keystroke.IKeyStrokeExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("a65c2866-e55c-4f31-a5fa-cd3f0ecfea05")
public abstract class AbstractKeyStroke extends AbstractAction implements IKeyStroke {

  /**
   * Constructor for configured entities
   */
  public AbstractKeyStroke() {
    this(true);
  }

  public AbstractKeyStroke(boolean callInitializer) {
    super(false);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[key=" + getKeyStroke() + "]";
  }

  protected static class LocalKeyStrokeExtension<OWNER extends AbstractKeyStroke> extends LocalActionExtension<OWNER> implements IKeyStrokeExtension<OWNER> {

    public LocalKeyStrokeExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IKeyStrokeExtension<? extends AbstractKeyStroke> createLocalExtension() {
    return new LocalKeyStrokeExtension<>(this);
  }

}
