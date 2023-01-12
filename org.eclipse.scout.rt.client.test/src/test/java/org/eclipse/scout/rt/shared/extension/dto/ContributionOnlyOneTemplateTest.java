/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.dto;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.IllegalExtensionException;
import org.eclipse.scout.rt.shared.extension.dto.fixture.AbstractTemplateBoxData.SecondStringInTemplate;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormData.SecondUseOfTemplateBox;
import org.eclipse.scout.rt.shared.extension.dto.fixture.PropertyExtensionData;
import org.junit.Assert;
import org.junit.Test;

public class ContributionOnlyOneTemplateTest extends AbstractLocalExtensionTestCase {
  @Test
  public void testContributeToDtoInOnlyOneTemplate() {
    IExtensionRegistry extensionRegistry = BEANS.get(IExtensionRegistry.class);
    extensionRegistry.register(PropertyExtensionData.class, new ClassIdentifier(SecondUseOfTemplateBox.class, SecondStringInTemplate.class));
    doTest();
  }

  private void doTest() {
    OrigFormData dto = new OrigFormData();
    Assert.assertNotNull(dto.getSecondUseOfTemplateBox().getSecondStringInTemplate().getContribution(PropertyExtensionData.class));

    try {
      dto.getFirstUseOfTemplateBox().getSecondStringInTemplate().getContribution(PropertyExtensionData.class);
      Assert.fail();
    }
    catch (IllegalExtensionException e) {
      Assert.assertNotNull(e); // ok on exception
    }
  }
}
