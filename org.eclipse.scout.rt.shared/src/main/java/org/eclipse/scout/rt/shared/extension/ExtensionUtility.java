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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;

public final class ExtensionUtility {

  private ExtensionUtility() {
  }

  /**
   * Applies move descriptors registered on {@link IExtensionRegistry#registerMove(Class, double)} to all objects of the
   * given list.
   * <p/>
   * <b>Important</b>: The given {@link Iterable} is not sorted by this method.
   */
  public static void moveModelObjects(Iterable<? extends IOrdered> modelObjects) {
    IInternalExtensionRegistry extensionRegistry = BEANS.get(IInternalExtensionRegistry.class);
    for (IOrdered m : modelObjects) {
      MoveDescriptor<IOrdered> moveDesc = extensionRegistry.createModelMoveDescriptorFor(m, null);
      if (moveDesc != null && moveDesc.getNewOrder() != null) {
        m.setOrder(moveDesc.getNewOrder());
      }
    }
  }
}
