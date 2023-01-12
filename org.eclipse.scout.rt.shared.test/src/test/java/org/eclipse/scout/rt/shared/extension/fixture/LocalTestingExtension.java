/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.fixture;

import org.eclipse.scout.rt.shared.extension.AbstractExtension;

/**
 * @since 5.2
 */
public class LocalTestingExtension extends AbstractExtension<TestingExtensibleObject> implements ITestingExtension {

  public LocalTestingExtension(TestingExtensibleObject owner) {
    super(owner);
  }

  @Override
  public void execOperation(TestingExtensionChain<? extends ITestingExtension> chain) {
  }
}
