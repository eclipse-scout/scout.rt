/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import org.eclipse.scout.rt.platform.namespace.INamespace;

public final class DataObjectProjectFixtureNamespace implements INamespace {

  public static final String ID = "dataObjectProjectFixture";
  public static final double ORDER = 9000;

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public double getOrder() {
    return ORDER;
  }
}
