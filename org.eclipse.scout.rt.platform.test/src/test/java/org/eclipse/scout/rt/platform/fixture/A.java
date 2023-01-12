/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.fixture;

public class A {

  public B b = new B();

  public class B {

    public C1 c1 = new C1();
    public C2 c2 = new C2();

    public class C1 extends AbstractC {

    }

    public class C2 extends AbstractC {

    }
  }
}
