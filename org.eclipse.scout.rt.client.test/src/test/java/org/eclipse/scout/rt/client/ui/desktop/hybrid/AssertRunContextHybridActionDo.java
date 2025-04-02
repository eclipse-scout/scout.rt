/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.AssertRunContextHybridAction")
public class AssertRunContextHybridActionDo extends DoEntity {

  public DoValue<IDesktop> desktop() {
    return doValue("desktop");
  }

  public DoValue<IOutline> outline() {
    return doValue("outline");
  }

  public DoValue<IForm> form() {
    return doValue("form");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public AssertRunContextHybridActionDo withDesktop(IDesktop desktop) {
    desktop().set(desktop);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDesktop getDesktop() {
    return desktop().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AssertRunContextHybridActionDo withOutline(IOutline outline) {
    outline().set(outline);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IOutline getOutline() {
    return outline().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AssertRunContextHybridActionDo withForm(IForm form) {
    form().set(form);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IForm getForm() {
    return form().get();
  }
}
