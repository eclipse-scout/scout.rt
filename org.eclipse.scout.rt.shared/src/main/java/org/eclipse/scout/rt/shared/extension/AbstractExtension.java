/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
