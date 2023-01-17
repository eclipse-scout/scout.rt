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

import java.util.List;

import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.extension.IExtension;

/**
 * @since 4.2
 */
public class TestingExtensionChain<EXTENSION extends ITestingExtension> extends AbstractExtensionChain<EXTENSION> {

  public TestingExtensionChain(List<? extends IExtension<?>> extensions, Class<? extends IExtension> filterClass) {
    super(extensions, filterClass);
  }

  public void execOperation() {
    MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
      @Override
      protected void callMethod(EXTENSION next) {
        next.execOperation(TestingExtensionChain.this);
      }
    };
    callChain(methodInvocation);
  }
}
