/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.fixture;

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
