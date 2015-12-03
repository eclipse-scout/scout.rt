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
package org.eclipse.scout.rt.shared.extension.services.common.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.IMoveModelObjectToRootMarker;
import org.eclipse.scout.rt.shared.extension.IllegalExtensionException;
import org.eclipse.scout.rt.shared.extension.services.common.code.fixture.MoveCodesCodeType;
import org.eclipse.scout.rt.shared.extension.services.common.code.fixture.MoveCodesCodeType.Top1Code;
import org.eclipse.scout.rt.shared.extension.services.common.code.fixture.MoveCodesCodeType.Top1Code.Sub1Top1Code;
import org.eclipse.scout.rt.shared.extension.services.common.code.fixture.MoveCodesCodeType.Top1Code.Sub1Top1Code.Sub1Sub1Top1Code;
import org.eclipse.scout.rt.shared.extension.services.common.code.fixture.MoveCodesCodeType.Top1Code.Sub2Top1Code;
import org.eclipse.scout.rt.shared.extension.services.common.code.fixture.MoveCodesCodeType.Top2Code;
import org.eclipse.scout.rt.shared.extension.services.common.code.fixture.MoveCodesCodeType.Top2Code.Sub1Top2Code;
import org.eclipse.scout.rt.shared.extension.services.common.code.fixture.MoveCodesCodeType.Top2Code.Sub2Top2Code;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.junit.Assert;
import org.junit.Test;

public class MoveCodesTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testSetup() throws Exception {
    MoveCodesCodeType codeType = new MoveCodesCodeType();
    List<? extends AbstractCode<String>> rootCodes = codeType.getCodes();
    assertCodes(rootCodes, Top1Code.class, Top2Code.class);
    assertCodes(rootCodes.get(0).getChildCodes(), Sub1Top1Code.class, Sub2Top1Code.class);
    assertCodes(rootCodes.get(0).getChildCodes().get(0).getChildCodes(), Sub1Sub1Top1Code.class);
    assertCodes(rootCodes.get(1).getChildCodes(), Sub1Top2Code.class, Sub2Top2Code.class);
  }

  @Test
  public void testMoveTopLevelCode() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top2Code.class, 5);
    doTestMoveTopLevelCode();
  }

  @Test
  public void testMoveTopLevelCodeMoveToRoot() {
    BEANS.get(IExtensionRegistry.class).registerMoveToRoot(Top2Code.class, 5d);
    doTestMoveTopLevelCode();
  }

  @Test
  public void testMoveTopLevelCodeMoveWithIMoveModelObjectToRootMarker() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top2Code.class, 5d, IMoveModelObjectToRootMarker.class);
    doTestMoveTopLevelCode();
  }

  @Test
  public void testMoveTopLevelCodeMoveExplicitNullParent() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top2Code.class, 5d, null);
    doTestMoveTopLevelCode();
  }

  protected void doTestMoveTopLevelCode() {
    MoveCodesCodeType codeType = new MoveCodesCodeType();
    List<? extends AbstractCode<String>> rootCodes = codeType.getCodes();
    assertCodes(rootCodes, Top2Code.class, Top1Code.class);
    assertCodes(rootCodes.get(0).getChildCodes(), Sub1Top2Code.class, Sub2Top2Code.class);
    assertCodes(rootCodes.get(1).getChildCodes(), Sub1Top1Code.class, Sub2Top1Code.class);
    assertCodes(rootCodes.get(1).getChildCodes().get(0).getChildCodes(), Sub1Sub1Top1Code.class);
  }

  @Test
  public void testMoveSubCodeWithinSameParent() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Code.class, 30);
    doTestMoveSubCodeWithinSameParent();
  }

  @Test
  public void testMoveSubCodeWithinSameParentExplicitNullParent() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Code.class, 30d, null);
    doTestMoveSubCodeWithinSameParent();
  }

  @Test
  public void testMoveSubCodeWithinSameParentExplicitParentClass() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Code.class, 30d, Top1Code.class);
    doTestMoveSubCodeWithinSameParent();
  }

  protected void doTestMoveSubCodeWithinSameParent() {
    MoveCodesCodeType codeType = new MoveCodesCodeType();
    List<? extends AbstractCode<String>> rootCodes = codeType.getCodes();
    assertCodes(rootCodes, Top1Code.class, Top2Code.class);
    assertCodes(rootCodes.get(0).getChildCodes(), Sub2Top1Code.class, Sub1Top1Code.class);
    assertCodes(rootCodes.get(0).getChildCodes().get(1).getChildCodes(), Sub1Sub1Top1Code.class);
    assertCodes(rootCodes.get(1).getChildCodes(), Sub1Top2Code.class, Sub2Top2Code.class);
  }

  @Test
  public void testMoveSubCodeToRoot() {
    BEANS.get(IExtensionRegistry.class).registerMoveToRoot(Sub1Top1Code.class, 15d);
    doTestMoveSubCodeToRoot();
  }

  @Test
  public void testMoveSubCodeToRootWithIMoveModelObjectToRootMarker() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Code.class, 15d, IMoveModelObjectToRootMarker.class);
    doTestMoveSubCodeToRoot();
  }

  protected void doTestMoveSubCodeToRoot() {
    MoveCodesCodeType codeType = new MoveCodesCodeType();
    List<? extends AbstractCode<String>> rootCodes = codeType.getCodes();
    assertCodes(rootCodes, Top1Code.class, Sub1Top1Code.class, Top2Code.class);
    assertCodes(rootCodes.get(0).getChildCodes(), Sub2Top1Code.class);
    assertCodes(rootCodes.get(1).getChildCodes(), Sub1Sub1Top1Code.class);
    assertCodes(rootCodes.get(2).getChildCodes(), Sub1Top2Code.class, Sub2Top2Code.class);
  }

  @Test
  public void testMoveRootCodeToSubCode() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top1Code.class, 15d, Top2Code.class);

    MoveCodesCodeType codeType = new MoveCodesCodeType();
    List<? extends AbstractCode<String>> rootCodes = codeType.getCodes();
    assertCodes(rootCodes, Top2Code.class);
    assertCodes(rootCodes.get(0).getChildCodes(), Sub1Top2Code.class, Top1Code.class, Sub2Top2Code.class);
    assertCodes(rootCodes.get(0).getChildCodes().get(1).getChildCodes(), Sub1Top1Code.class, Sub2Top1Code.class);
    assertCodes(rootCodes.get(0).getChildCodes().get(1).getChildCodes().get(0).getChildCodes(), Sub1Sub1Top1Code.class);
  }

  @Test
  public void testMoveCodeToAnotherCodeWithouChangingOrder() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Code.class, null, Top2Code.class);

    MoveCodesCodeType codeType = new MoveCodesCodeType();
    List<? extends AbstractCode<String>> rootCodes = codeType.getCodes();
    assertCodes(rootCodes, Top1Code.class, Top2Code.class);
    assertCodes(rootCodes.get(0).getChildCodes(), Sub2Top1Code.class);
    assertCodes(rootCodes.get(1).getChildCodes(), Sub1Top1Code.class, Sub1Top2Code.class, Sub2Top2Code.class);
    assertCodes(rootCodes.get(1).getChildCodes().get(0).getChildCodes(), Sub1Sub1Top1Code.class);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testMoveCodeToItselfExceptionOnRegisterMove() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top1Code.class, null, Top1Code.class);
    new MoveCodesCodeType();
  }

  @Test(expected = IllegalExtensionException.class)
  public void testMoveCodeToOtherCodeClass() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top1Code.class, null, Top1Code.class);
    new MoveCodesCodeType();
    Assert.fail();
  }

  protected static void assertCodes(List<? extends ICode<?>> codes, Class<?>... expectedCodeClasses) {
    assertEquals(expectedCodeClasses.length, CollectionUtility.size(codes));

    for (int i = 0; i < expectedCodeClasses.length; i++) {
      assertSame(expectedCodeClasses[i], codes.get(i).getClass());
    }
  }
}
