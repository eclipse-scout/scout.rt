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
package org.eclipse.scout.rt.shared.services.common.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link AbstractCode}
 */
@RunWith(PlatformTestRunner.class)
public class CodeTypeTest {

  @Test
  public void testOverwriteCode_overwriteValues() throws Exception {

    TestCodeType ct = new TestCodeType();
    ICode c = ct.getCode(TestCodeType.Test1Code.ID);
    assertEquals(TestCodeType.DYNAMIC_TEXT, c.getText());
    assertEquals(TestCodeType.CONFIGURED_ICON, c.getIconId());
    assertNull(c.getTooltipText());
    assertEquals(TestCodeType.CONFIGURED_BACKGROUND_COLOR, c.getBackgroundColor());
    assertEquals(TestCodeType.CONFIGURED_FOREGROUND_COLOR, c.getForegroundColor());
    assertEquals(TestCodeType.CONFIGURED_FONT, c.getFont().toPattern());
    assertEquals(TestCodeType.DYNAMIC_ENABLED, c.isEnabled());
    // parent key
    assertEquals(TestCodeType.DYNAMIC_ACTIVE, c.isActive());
    assertEquals(TestCodeType.CONFIGURED_EXT_KEY, c.getExtKey());
    assertEquals(TestCodeType.CONFIGURED_VALUE, c.getValue());
    assertEquals(TestCodeType.DYNAMIC_PARTITION_ID, c.getPartitionId());
  }

  @Test
  public void testOverwriteCode_keepValues() throws Exception {
    TestCodeType ct = new TestCodeType();
    ICode c = ct.getCode(TestCodeType.Test2Code.ID);
    assertEquals(TestCodeType.DYNAMIC_TEXT, c.getText());
    assertEquals(TestCodeType.DYNAMIC_ICON, c.getIconId());
    assertEquals(TestCodeType.DYNAMIC_TOOLTIP, c.getTooltipText());
    assertEquals(TestCodeType.DYNAMIC_BACKGROUND_COLOR, c.getBackgroundColor());
    assertEquals(TestCodeType.DYNAMIC_FOREGROUND_COLOR, c.getForegroundColor());
    assertEquals(TestCodeType.DYNAMIC_FONT, c.getFont().toPattern());
    assertEquals(TestCodeType.DYNAMIC_ENABLED, c.isEnabled());
    // parent key
    assertEquals(TestCodeType.DYNAMIC_ACTIVE, c.isActive());
    assertEquals(TestCodeType.DYNAMIC_EXT_KEY, c.getExtKey());
    assertEquals(TestCodeType.DYNAMIC_VALUE, c.getValue());
    assertEquals(TestCodeType.DYNAMIC_PARTITION_ID, c.getPartitionId());
  }

  private static class TestCodeType extends AbstractCodeType<String, String> {
    private static final long serialVersionUID = 1L;
    public static final String ID = "2";

    public static final String CONFIGURED_TEXT = "configuredText";
    public static final String CONFIGURED_ICON = "configuredIcon";
    public static final String CONFIGURED_TOOLTIP = "configuredTooltip";
    public static final String CONFIGURED_BACKGROUND_COLOR = "configuredBackgroundColor";
    public static final String CONFIGURED_FOREGROUND_COLOR = "configuredForegroundColor";
    public static final String CONFIGURED_FONT = "null-ITALIC-0";
    public static final boolean CONFIGURED_ENABLED = false;
    public static final boolean CONFIGURED_ACTIVE = false;
    public static final String CONFIGURED_EXT_KEY = "configuredExtKey";
    public static final double CONFIGURED_VALUE = 42d;

    public static final String DYNAMIC_TEXT = "dynamicText";
    public static final String DYNAMIC_ICON = "dynamicIcon";
    public static final String DYNAMIC_TOOLTIP = "dynamicTooltip";
    public static final String DYNAMIC_BACKGROUND_COLOR = "dynamicBackgroundColor";
    public static final String DYNAMIC_FOREGROUND_COLOR = "dynamicForegroundColor";
    public static final String DYNAMIC_FONT = "null-BOLD-0";
    public static final boolean DYNAMIC_ENABLED = true;
    public static final String DYNAMIC_PARENT_KEY = "dynamicParentKey";
    public static final boolean DYNAMIC_ACTIVE = true;
    public static final String DYNAMIC_EXT_KEY = "dynamicExtKey";
    public static final double DYNAMIC_VALUE = 10d;
    public static final long DYNAMIC_PARTITION_ID = 0L;

    @Override
    public String getId() {
      return ID;
    }

    @Override
    protected List<ICodeRow<String>> execLoadCodes(Class<? extends ICodeRow<String>> codeRowType) {
      List<ICodeRow<String>> codeRows = new ArrayList<ICodeRow<String>>();
      codeRows.add(new CodeRow<String>(
          Test1Code.ID,
          DYNAMIC_TEXT,
          null, // icon
          null, // tooltip
          null, // background color
          null, // foreground color
          null, // font
          DYNAMIC_ENABLED,
          DYNAMIC_PARENT_KEY,
          DYNAMIC_ACTIVE,
          null, // ext key
          null, // value
          DYNAMIC_PARTITION_ID));
      codeRows.add(
          new CodeRow<String>(
              Test2Code.ID,
              DYNAMIC_TEXT,
              DYNAMIC_ICON,
              DYNAMIC_TOOLTIP,
              DYNAMIC_BACKGROUND_COLOR,
              DYNAMIC_FOREGROUND_COLOR,
              FontSpec.parse(DYNAMIC_FONT),
              DYNAMIC_ENABLED,
              DYNAMIC_PARENT_KEY,
              DYNAMIC_ACTIVE,
              DYNAMIC_EXT_KEY,
              DYNAMIC_VALUE,
              DYNAMIC_PARTITION_ID));
      return codeRows;
    }

    @Order(10)
    public class RootCode extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "dynamicParentKey";

      @Override
      public String getId() {
        return ID;
      }

      @Override
      protected String getConfiguredText() {
        return "Root";
      }

    }

    @Order(10)
    public class Test1Code extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "Test1Code";

      @Override
      public String getId() {
        return ID;
      }

      @Override
      protected String getConfiguredText() {
        return CONFIGURED_TEXT;
      }

      @Override
      protected String getConfiguredIconId() {
        return CONFIGURED_ICON;
      }

      @Override
      protected String getConfiguredTooltipText() {
        return CONFIGURED_TOOLTIP;
      }

      @Override
      protected String getConfiguredBackgroundColor() {
        return CONFIGURED_BACKGROUND_COLOR;
      }

      @Override
      protected String getConfiguredForegroundColor() {
        return CONFIGURED_FOREGROUND_COLOR;
      }

      @Override
      protected String getConfiguredFont() {
        return CONFIGURED_FONT;
      }

      @Override
      protected boolean getConfiguredEnabled() {
        return CONFIGURED_ENABLED;
      }

      @Override
      protected boolean getConfiguredActive() {
        return CONFIGURED_ACTIVE;
      }

      @Override
      protected String getConfiguredExtKey() {
        return CONFIGURED_EXT_KEY;
      }

      @Override
      protected Double getConfiguredValue() {
        return CONFIGURED_VALUE;
      }
    }

    @Order(20)
    public class Test2Code extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "Test2Code";

      @Override
      public String getId() {
        return ID;
      }

      @Override
      protected String getConfiguredText() {
        return CONFIGURED_TEXT;
      }

      @Override
      protected String getConfiguredIconId() {
        return CONFIGURED_ICON;
      }

      @Override
      protected String getConfiguredTooltipText() {
        return CONFIGURED_TOOLTIP;
      }

      @Override
      protected String getConfiguredBackgroundColor() {
        return CONFIGURED_BACKGROUND_COLOR;
      }

      @Override
      protected String getConfiguredForegroundColor() {
        return CONFIGURED_FOREGROUND_COLOR;
      }

      @Override
      protected String getConfiguredFont() {
        return CONFIGURED_FONT;
      }

      @Override
      protected boolean getConfiguredEnabled() {
        return CONFIGURED_ENABLED;
      }

      @Override
      protected boolean getConfiguredActive() {
        return CONFIGURED_ACTIVE;
      }

      @Override
      protected String getConfiguredExtKey() {
        return CONFIGURED_EXT_KEY;
      }

      @Override
      protected Double getConfiguredValue() {
        return CONFIGURED_VALUE;
      }
    }
  }
}
