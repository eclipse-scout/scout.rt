/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.data.model.fixture;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModel;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;

public class TestDataModel extends AbstractDataModel {

  private static final long serialVersionUID = 1L;

  @Order(10)
  public class Top1Entity extends AbstractDataModelEntity {
    private static final long serialVersionUID = 1L;

    @Order(10)
    public class Sub1Top1Entity extends AbstractDataModelEntity {
      private static final long serialVersionUID = 1L;
    }

    @Order(20)
    public class Sub2Top1Entity extends AbstractDataModelEntity {
      private static final long serialVersionUID = 1L;
    }

    @Order(30)
    public class Sub3Top1Entity extends AbstractDataModelEntity {
      private static final long serialVersionUID = 1L;
    }

    @Order(10)
    public class Sub1Top1Attribute extends AbstractDataModelAttribute {
      private static final long serialVersionUID = 1L;
    }

    @Order(20)
    public class Sub2Top1Attribute extends AbstractDataModelAttribute {
      private static final long serialVersionUID = 1L;
    }

    @Order(30)
    public class Sub3Top1Attribute extends AbstractDataModelAttribute {
      private static final long serialVersionUID = 1L;
    }
  }

  @Order(20)
  public class Top2Entity extends AbstractDataModelEntity {
    private static final long serialVersionUID = 1L;
  }

  @Order(30)
  public class Top3Entity extends AbstractDataModelEntity {
    private static final long serialVersionUID = 1L;
  }

  @Order(10)
  public class Top1Attribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;
  }

  @Order(20)
  public class Top2Attribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;
  }

  @Order(30)
  public class Top3Attribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;
  }
}
