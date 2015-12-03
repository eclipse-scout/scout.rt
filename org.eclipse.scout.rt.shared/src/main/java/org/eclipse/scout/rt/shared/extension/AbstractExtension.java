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
package org.eclipse.scout.rt.shared.extension;

public abstract class AbstractExtension<OWNER extends IExtensibleObject> implements IExtension<OWNER> {
  private final OWNER m_owner;

  public AbstractExtension(OWNER owner) {
    m_owner = owner;
  }

  @Override
  public OWNER getOwner() {
    return m_owner;
  }

}
