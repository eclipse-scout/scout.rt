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
