/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrationContextDataTarget;
import org.eclipse.scout.rt.dataobject.migration.IDoStructureMigrationTargetContextData;

@DoStructureMigrationContextDataTarget(doEntityClasses = {CustomerFixtureDo.class})
public class CustomerFixtureTargetContextData implements IDoStructureMigrationTargetContextData {

  private String m_firstName;

  public String getFirstName() {
    return m_firstName;
  }

  @Override
  public boolean initialize(DoStructureMigrationContext ctx, IDoEntity doEntity) {
    if (doEntity instanceof CustomerFixtureDo) {
      CustomerFixtureDo house = (CustomerFixtureDo) doEntity;
      m_firstName = house.getFirstName();
      return true;
    }

    return false;
  }
}
