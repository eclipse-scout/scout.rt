/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;

public abstract class AbstractTestAddressDo extends DoEntity {

  public DoValue<String> id() {
    return doValue("id");
  }

  public AbstractTestAddressDo withId(String id) {
    id().set(id);
    return this;
  }

  public String getId() {
    return id().get();
  }
}
