/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.breadcrumbbar;

public final class BreadcrumbItems {

  private BreadcrumbItems() {
    // private
  }

  public static IBreadcrumbItem create(String text, String ref) {
    return new AbstractBreadcrumbItem() {
      @Override
      protected String getConfiguredText() {
        return text;
      }

      @Override
      protected String getConfiguredRef() {
        return ref;
      }
    };
  }
}
