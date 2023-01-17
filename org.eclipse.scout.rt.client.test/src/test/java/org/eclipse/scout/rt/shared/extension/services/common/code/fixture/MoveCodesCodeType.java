/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.services.common.code.fixture;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeTypeWithGeneric;

@ClassId("a3246869-3de5-49e6-8251-08269e7df594")
public class MoveCodesCodeType extends AbstractCodeTypeWithGeneric<Long, String, AbstractCode<String>> {

  private static final long serialVersionUID = 1L;
  public static final long ID = 1234;

  @Override
  public Long getId() {
    return ID;
  }

  @Order(10)
  public static class Top1Code extends AbstractCode<String> {
    private static final long serialVersionUID = 1L;
    public static final String ID = "Top1";

    @Override
    public String getId() {
      return ID;
    }

    @Order(5)
    public static class Sub1Top1Code extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "Sub1Top1";

      @Override
      public String getId() {
        return ID;
      }

      @Order(10)
      public static class Sub1Sub1Top1Code extends AbstractCode<String> {
        private static final long serialVersionUID = 1L;
        public static final String ID = "Sub1Sub1Top1";

        @Override
        public String getId() {
          return ID;
        }
      }
    }

    @Order(20)
    public static class Sub2Top1Code extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "Sub2Top1";

      @Override
      public String getId() {
        return ID;
      }
    }
  }

  @Order(20)
  public static class Top2Code extends AbstractCode<String> {
    private static final long serialVersionUID = 1L;
    public static final String ID = "Top2";

    @Override
    public String getId() {
      return ID;
    }

    @Order(10)
    public static class Sub1Top2Code extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "Sub1Top2";

      @Override
      public String getId() {
        return ID;
      }
    }

    @Order(20)
    public static class Sub2Top2Code extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "Sub2Top2";

      @Override
      public String getId() {
        return ID;
      }
    }
  }
}
