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
package org.eclipse.scout.rt.extension.client.ui.action.menu.internal;

import java.lang.reflect.Field;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.AbstractCalendarItemProvider;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField;
import org.eclipse.scout.rt.extension.client.ui.action.menu.internal.MenuContributionExtensionTest.P_CalendarField.Calendar;
import org.eclipse.scout.rt.extension.client.ui.action.menu.internal.MenuContributionExtensionTest.P_TestMenu.Constructor;
import org.eclipse.scout.rt.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.9.0
 */
@RunWith(ScoutClientTestRunner.class)
public class MenuContributionExtensionTest {

  @Test
  public void testOrder() {
    MenuContributionExtension extension = new MenuContributionExtension(P_TestMenu.class, null, 100d);
    Assert.assertEquals(100, extension.getOrder(), 0);
    //
    extension = new MenuContributionExtension(P_TestMenu.class, null, null);
    Assert.assertEquals(Double.MAX_VALUE, extension.getOrder(), 0);
  }

  @Test
  public void testCreateContributionNull() throws Exception {
    MenuContributionExtension extension = new MenuContributionExtension(P_TestMenu.class, null, 100d);
    try {
      extension.createContribution(null, null);
      Assert.fail("null anchor and container should not be supported");
    }
    catch (Exception e) {
    }
    try {
      extension.createContribution(new P_AnchorNodePage(), null);
      Assert.fail("null container should not be supported");
    }
    catch (Exception e) {
    }
    try {
      extension.createContribution(null, new P_AnchorNodePage());
      Assert.fail("null container should not be supported");
    }
    catch (Exception e) {
    }
  }

  @Test
  public void testCreateContributionAnchorAndContainerSame() throws Exception {
    MenuContributionExtension extension = new MenuContributionExtension(P_TestMenu.class, null, 100d);
    assertCreateContribution(Constructor.AnchorNodePage, extension, new P_AnchorNodePage());
    assertCreateContribution(Constructor.Page, extension, new P_OtherPage());
    assertCreateContribution(Constructor.Object, extension, "Test");
    assertCreateContribution(Constructor.Field, extension, new P_CalendarField());
    //
    extension = new MenuContributionExtension(P_OtherTestMenu.class, null, 100d);
    P_AnchorNodePage page = new P_AnchorNodePage();
    IMenu menu = extension.createContribution(page, page);
    Assert.assertNotNull(menu);
  }

  @Test
  public void testCreateContributionAnchorAndContainerDifferent() throws Exception {
    MenuContributionExtension extension = new MenuContributionExtension(P_TestMenu.class, null, 100d);
    P_CalendarField calendarField = new P_CalendarField();
    Calendar calendar = calendarField.getCalendar();
    assertCreateContribution(Constructor.CalendarFieldCalendar, extension, calendarField, calendar);
    //
    Field field = AbstractCalendar.class.getDeclaredField("m_providers");
    Assert.assertNotNull(field);
    field.setAccessible(true);
    ICalendarItemProvider[] providers = (ICalendarItemProvider[]) field.get(calendar);
    Assert.assertNotNull(providers);
    Assert.assertTrue(providers.length == 1);
    assertCreateContribution(Constructor.CalendarFieldCalendarItemProvider, extension, calendarField, providers[0]);
  }

  private static void assertCreateContribution(Constructor expectedConstructor, MenuContributionExtension extension, Object anchor) throws ProcessingException {
    assertCreateContribution(expectedConstructor, extension, anchor, anchor);
  }

  private static void assertCreateContribution(Constructor expectedConstructor, MenuContributionExtension extension, Object anchor, Object container) throws ProcessingException {
    IMenu menu = extension.createContribution(anchor, container);
    Assert.assertTrue(menu instanceof P_TestMenu);
    Assert.assertEquals(expectedConstructor, ((P_TestMenu) menu).m_constructor);
  }

  public static class P_TestMenu extends AbstractMenu {

    public enum Constructor {
      Default,
      Field,
      CalendarFieldCalendar,
      CalendarFieldCalendarItemProvider,
      AnchorNodePage,
      Page,
      Object,
      Invalid
    }

    private final Constructor m_constructor;

    public Constructor getConstructor() {
      return m_constructor;
    }

    public P_TestMenu() {
      m_constructor = Constructor.Default;
    }

    public P_TestMenu(P_CalendarField field) {
      m_constructor = Constructor.Field;
    }

    public P_TestMenu(P_CalendarField field, P_CalendarField.Calendar calendar) {
      m_constructor = Constructor.CalendarFieldCalendar;
    }

    public P_TestMenu(P_CalendarField field, P_CalendarField.Calendar.CalendarItemProvider itemProvider) {
      m_constructor = Constructor.CalendarFieldCalendarItemProvider;
    }

    public P_TestMenu(P_AnchorNodePage page) {
      m_constructor = Constructor.AnchorNodePage;
    }

    public P_TestMenu(P_AnchorNodePage page, Object o) {
      m_constructor = Constructor.Invalid;
    }

    public P_TestMenu(IPage page) {
      m_constructor = Constructor.Page;
    }

    public P_TestMenu(IPage page, Object o) {
      m_constructor = Constructor.Invalid;
    }

    public P_TestMenu(Object obj) {
      m_constructor = Constructor.Object;
    }

    public P_TestMenu(Object o1, Object o2) {
      m_constructor = Constructor.Invalid;
    }
  }

  public static class P_OtherTestMenu extends AbstractMenu {

    public P_OtherTestMenu(P_AnchorNodePage page, Object container) {
    }
  }

  public static class P_AnchorNodePage extends AbstractPageWithNodes {
  }

  public static class P_OtherPage extends AbstractPageWithNodes {
  }

  public static class P_CalendarField extends AbstractCalendarField<P_CalendarField.Calendar> {

    public class Calendar extends AbstractCalendar {

      public class CalendarItemProvider extends AbstractCalendarItemProvider {

      }
    }
  }
}
