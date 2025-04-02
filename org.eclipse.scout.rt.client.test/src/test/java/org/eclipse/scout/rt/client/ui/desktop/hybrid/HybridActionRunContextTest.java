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

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class HybridActionRunContextTest {

  @Test
  public void testEmpty() {
    ClientRunContexts.empty()
        .run(() -> BEANS.get(AssertRunContextHybridAction.class)
            .execute(
                "42",
                BEANS.get(AssertRunContextHybridActionDo.class),
                BEANS.get(HybridActionContextElements.class)));
  }

  @Test
  public void testFromDesktop() {
    var desktop = new MyDesktop();
    var outline = new MyOutline();
    var form = new MyForm();

    ClientRunContexts.empty()
        .withDesktop(desktop)
        .run(() -> BEANS.get(AssertRunContextHybridAction.class)
            .execute(
                "42",
                BEANS.get(AssertRunContextHybridActionDo.class)
                    .withDesktop(desktop),
                BEANS.get(HybridActionContextElements.class)));

    desktop.setOutline(outline);

    ClientRunContexts.empty()
        .withDesktop(desktop)
        .run(() -> BEANS.get(AssertRunContextHybridAction.class)
            .execute(
                "42",
                BEANS.get(AssertRunContextHybridActionDo.class)
                    .withDesktop(desktop)
                    .withOutline(outline),
                BEANS.get(HybridActionContextElements.class)));

    desktop.setOutline((IOutline) null);
    desktop.setActiveForm(form);

    ClientRunContexts.empty()
        .withDesktop(desktop)
        .run(() -> BEANS.get(AssertRunContextHybridAction.class)
            .execute(
                "42",
                BEANS.get(AssertRunContextHybridActionDo.class)
                    .withDesktop(desktop)
                    .withForm(form),
                BEANS.get(HybridActionContextElements.class)));

    desktop.setOutline(outline);

    ClientRunContexts.empty()
        .withDesktop(desktop)
        .run(() -> BEANS.get(AssertRunContextHybridAction.class)
            .execute(
                "42",
                BEANS.get(AssertRunContextHybridActionDo.class)
                    .withDesktop(desktop)
                    .withOutline(outline)
                    .withForm(form),
                BEANS.get(HybridActionContextElements.class)));
  }

  @Test
  public void testFromContextElements() {
    var desktop = new MyDesktop();
    var desktopOutline = new MyOutline();
    var desktopForm = new MyForm();

    desktop.setOutline(desktopOutline);
    desktop.setActiveForm(desktopForm);

    var outline = new MyOutline();
    var form = new MyForm();

    ClientRunContexts.empty()
        .withDesktop(desktop)
        .run(() -> BEANS.get(AssertRunContextHybridAction.class)
            .execute(
                "42",
                BEANS.get(AssertRunContextHybridActionDo.class)
                    .withDesktop(desktop)
                    .withOutline(desktopOutline)
                    .withForm(desktopForm),
                BEANS.get(HybridActionContextElements.class)));

    ClientRunContexts.empty()
        .withDesktop(desktop)
        .run(() -> BEANS.get(AssertRunContextHybridAction.class)
            .execute(
                "42",
                BEANS.get(AssertRunContextHybridActionDo.class)
                    .withDesktop(desktop)
                    .withOutline(outline)
                    .withForm(desktopForm),
                BEANS.get(HybridActionContextElements.class)
                    .withElement("runcontext.outline", outline)));

    ClientRunContexts.empty()
        .withDesktop(desktop)
        .run(() -> BEANS.get(AssertRunContextHybridAction.class)
            .execute(
                "42",
                BEANS.get(AssertRunContextHybridActionDo.class)
                    .withDesktop(desktop)
                    .withOutline(desktopOutline)
                    .withForm(form),
                BEANS.get(HybridActionContextElements.class)
                    .withElement("runcontext.form", form)));

    ClientRunContexts.empty()
        .withDesktop(desktop)
        .run(() -> BEANS.get(AssertRunContextHybridAction.class)
            .execute(
                "42",
                BEANS.get(AssertRunContextHybridActionDo.class)
                    .withDesktop(desktop)
                    .withOutline(outline)
                    .withForm(form),
                BEANS.get(HybridActionContextElements.class)
                    .withElement("runcontext.outline", outline)
                    .withElement("runcontext.form", form)));

    ClientRunContexts.empty()
        .withDesktop(desktop)
        .run(() -> BEANS.get(AssertRunContextHybridAction.class)
            .execute(
                "42",
                BEANS.get(AssertRunContextHybridActionDo.class)
                    .withDesktop(desktop)
                    .withOutline(desktopOutline)
                    .withForm(desktopForm),
                BEANS.get(HybridActionContextElements.class)
                    .withElement("runcontext.outline", form)
                    .withElement("runcontext.form", outline)));
  }

  protected static class MyDesktop extends AbstractDesktop {

    private IOutline m_outline;
    private IForm m_activeForm;

    public MyDesktop() {
      super(false);
    }

    public void setOutline(IOutline outline) {
      m_outline = outline;
    }

    @Override
    public IOutline getOutline() {
      return m_outline;
    }

    @Override
    public void setActiveForm(IForm form) {
      m_activeForm = form;
    }

    @Override
    public IForm getActiveForm() {
      return m_activeForm;
    }
  }

  protected static class MyOutline extends AbstractOutline {

    @Override
    protected IPage<?> execCreateRootPage() {
      return null;
    }
  }

  protected static class MyForm extends AbstractForm {

    public MyForm() {
      // do not initialize form as it tries to access the current desktop of the client session, but this test does not run inside a client session
      super(false);
    }
  }
}
