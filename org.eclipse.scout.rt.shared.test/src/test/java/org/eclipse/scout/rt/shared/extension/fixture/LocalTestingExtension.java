/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
