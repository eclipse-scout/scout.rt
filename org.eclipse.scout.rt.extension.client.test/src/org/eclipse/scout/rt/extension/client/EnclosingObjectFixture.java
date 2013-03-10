/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client;

/**
 * Class providing some test scenarios for {@link ExtensionUtility#getEnclosingObject(Object)}.
 * 
 * @since 3.9.0
 */
public class EnclosingObjectFixture {

  public static class StaticInnerClass {

    public InnerStaticInnerClass createInner() {
      return new InnerStaticInnerClass();
    }

    public class InnerStaticInnerClass {

      public InnerInnerStaticInnerClass createInner() {
        return new InnerInnerStaticInnerClass();
      }

      public class InnerInnerStaticInnerClass {

        public InnerInnerInnerStaticInnerClass createInner() {
          return new InnerInnerInnerStaticInnerClass();
        }

        public class InnerInnerInnerStaticInnerClass {
        }
      }
    }
  }

  public InnerClass createInner() {
    return new InnerClass();
  }

  public class InnerClass {
    public InnerInnerClass createInner() {
      return new InnerInnerClass();
    }

    public class InnerInnerClass {

      public InnerInnerInnerClass createInner() {
        return new InnerInnerInnerClass();
      }

      public class InnerInnerInnerClass {

        public InnerInnerInnerInnerClass createInner() {
          return new InnerInnerInnerInnerClass();
        }

        public class InnerInnerInnerInnerClass {
        }
      }
    }
  }

  public static class StaticPathInnerClass {

    public static class InnerStaticPathInnerClass {

      public static class InnerInnerStaticPathInnerClass {

        public InnerInnerInnerStaticPathInnerClass createInner() {
          return new InnerInnerInnerStaticPathInnerClass();
        }

        public class InnerInnerInnerStaticPathInnerClass {
        }
      }
    }
  }
}
