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

/**
 * Super interface of all Scout model extensions.<br>
 * Extensions can be applied to all {@link IExtensibleObject}s.<br>
 * Use the {@link IExtensionRegistry} service to register your extensions.
 *
 * @since 4.2
 */
@FunctionalInterface
public interface IExtension<OWNER extends IExtensibleObject> {

  /**
   * @return the owner of the extension (the object that is extended).
   */
  OWNER getOwner();
}
