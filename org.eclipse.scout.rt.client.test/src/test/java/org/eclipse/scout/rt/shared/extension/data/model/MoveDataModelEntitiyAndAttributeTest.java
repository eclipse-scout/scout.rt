/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.IllegalExtensionException;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top1Attribute;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top1Entity;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top1Entity.Sub1Top1Attribute;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top1Entity.Sub1Top1Entity;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top1Entity.Sub2Top1Attribute;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top1Entity.Sub2Top1Entity;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top1Entity.Sub3Top1Attribute;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top1Entity.Sub3Top1Entity;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top2Attribute;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top2Entity;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top3Attribute;
import org.eclipse.scout.rt.shared.extension.data.model.fixture.TestDataModel.Top3Entity;
import org.junit.Test;

public class MoveDataModelEntitiyAndAttributeTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testSetup() {
    TestDataModel dataModel = new TestDataModel();
    assertDataModelElements(dataModel.getEntities(), Top1Entity.class, Top2Entity.class, Top3Entity.class);
    assertDataModelElements(dataModel.getAttributes(), Top1Attribute.class, Top2Attribute.class, Top3Attribute.class);

    IDataModelEntity entity = dataModel.getEntities().get(0);
    assertDataModelElements(entity.getEntities(), Sub1Top1Entity.class, Sub2Top1Entity.class, Sub3Top1Entity.class);
    assertDataModelElements(entity.getAttributes(), Sub1Top1Attribute.class, Sub2Top1Attribute.class, Sub3Top1Attribute.class);
  }

  @Test
  public void testMoveTopLevelEntity() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top1Entity.class, 40);

    TestDataModel dataModel = new TestDataModel();
    assertDataModelElements(dataModel.getEntities(), Top2Entity.class, Top3Entity.class, Top1Entity.class);
    assertDataModelElements(dataModel.getAttributes(), Top1Attribute.class, Top2Attribute.class, Top3Attribute.class);
    IDataModelEntity entity = dataModel.getEntities().get(2);
    assertDataModelElements(entity.getEntities(), Sub1Top1Entity.class, Sub2Top1Entity.class, Sub3Top1Entity.class);
    assertDataModelElements(entity.getAttributes(), Sub1Top1Attribute.class, Sub2Top1Attribute.class, Sub3Top1Attribute.class);
  }

  @Test
  public void testMoveTopLevelAttribute() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top1Attribute.class, 40);

    TestDataModel dataModel = new TestDataModel();
    assertDataModelElements(dataModel.getEntities(), Top1Entity.class, Top2Entity.class, Top3Entity.class);
    assertDataModelElements(dataModel.getAttributes(), Top2Attribute.class, Top3Attribute.class, Top1Attribute.class);

    IDataModelEntity entity = dataModel.getEntities().get(0);
    assertDataModelElements(entity.getEntities(), Sub1Top1Entity.class, Sub2Top1Entity.class, Sub3Top1Entity.class);
    assertDataModelElements(entity.getAttributes(), Sub1Top1Attribute.class, Sub2Top1Attribute.class, Sub3Top1Attribute.class);
  }

  @Test
  public void testMoveSubLevelEntity() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Entity.class, 40);

    TestDataModel dataModel = new TestDataModel();
    assertDataModelElements(dataModel.getEntities(), Top1Entity.class, Top2Entity.class, Top3Entity.class);
    assertDataModelElements(dataModel.getAttributes(), Top1Attribute.class, Top2Attribute.class, Top3Attribute.class);
    IDataModelEntity entity = dataModel.getEntities().get(0);
    assertDataModelElements(entity.getEntities(), Sub2Top1Entity.class, Sub3Top1Entity.class, Sub1Top1Entity.class);
    assertDataModelElements(entity.getAttributes(), Sub1Top1Attribute.class, Sub2Top1Attribute.class, Sub3Top1Attribute.class);
  }

  @Test
  public void testMoveSubLevelAttribute() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Attribute.class, 40);

    TestDataModel dataModel = new TestDataModel();
    assertDataModelElements(dataModel.getEntities(), Top1Entity.class, Top2Entity.class, Top3Entity.class);
    assertDataModelElements(dataModel.getAttributes(), Top1Attribute.class, Top2Attribute.class, Top3Attribute.class);

    IDataModelEntity entity = dataModel.getEntities().get(0);
    assertDataModelElements(entity.getEntities(), Sub1Top1Entity.class, Sub2Top1Entity.class, Sub3Top1Entity.class);
    assertDataModelElements(entity.getAttributes(), Sub2Top1Attribute.class, Sub3Top1Attribute.class, Sub1Top1Attribute.class);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testMoveSubLevelEntityToRoot() {
    BEANS.get(IExtensionRegistry.class).registerMoveToRoot(Sub1Top1Entity.class, 40d);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testMoveSubLevelAttributeToRoot() {
    BEANS.get(IExtensionRegistry.class).registerMoveToRoot(Sub1Top1Attribute.class, 40d);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testMoveEntityToAnotherEntity() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Entity.class, 40d, Top2Entity.class);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testMoveAttributeToAnotherEntity() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Attribute.class, 40d, Top2Entity.class);
  }

  protected static void assertDataModelElements(List<?> list, Class<?>... expectedWizardStepClasses) {
    assertEquals(expectedWizardStepClasses.length, list.size());
    for (int i = 0; i < expectedWizardStepClasses.length; i++) {
      assertSame(expectedWizardStepClasses[i], list.get(i).getClass());
    }
  }
}
