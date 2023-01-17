/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.stringfield;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Basic string field test
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractStringFieldTest extends AbstractStringField {

  private AtomicInteger m_displayTextChangedCounter;
  private List<String> m_displayTextChangedHistory;

  @Before
  public void setup() {
    m_displayTextChangedCounter = new AtomicInteger();
    m_displayTextChangedHistory = new ArrayList<>();
    addPropertyChangeListener(evt -> {
      if (IValueField.PROP_DISPLAY_TEXT.equals(evt.getPropertyName())) {
        m_displayTextChangedCounter.incrementAndGet();
        m_displayTextChangedHistory.add((String) evt.getNewValue());
      }
    });
  }

  @Test
  public void testDisplayTextInitialState() {
    assertEquals("", getDisplayText());
    assertEquals(0, m_displayTextChangedCounter.get());
    assertArrayEquals(new String[]{}, m_displayTextChangedHistory.toArray());
  }

  @Test
  public void testDisplayTextSameTextTwice() {
    getUIFacade().parseAndSetValueFromUI("Test");
    assertEquals("Test", getDisplayText());
    getUIFacade().parseAndSetValueFromUI("Test");
    assertEquals("Test", getDisplayText());

    assertEquals(1, m_displayTextChangedCounter.get());
    assertArrayEquals(new String[]{"Test"}, m_displayTextChangedHistory.toArray());
  }

  @Test
  public void testDisplayTextNoValueChangeOnEmptyText() {
    getUIFacade().parseAndSetValueFromUI("Test");
    assertEquals("Test", getDisplayText());
    getUIFacade().parseAndSetValueFromUI("");
    assertEquals("", getDisplayText());
    getUIFacade().parseAndSetValueFromUI("");
    assertEquals("", getDisplayText());
    getUIFacade().parseAndSetValueFromUI(null);
    assertEquals("", getDisplayText());

    assertEquals(2, m_displayTextChangedCounter.get());
    assertArrayEquals(new String[]{"Test", ""}, m_displayTextChangedHistory.toArray());
  }

  @Test
  public void testMaxLength() {
    int initialMaxLength = getMaxLength();
    assertEquals(getConfiguredMaxLength(), initialMaxLength);
    setMaxLength(1234);
    assertEquals(1234, getMaxLength());
    setMaxLength(0);
    assertEquals(0, getMaxLength());
    setMaxLength(-2);
    assertEquals(0, getMaxLength());

    // set value
    setValue("the clown has a red nose");
    assertEquals(null, getValue());
    setMaxLength(9);
    setValue("the clown has a red nose");
    assertEquals("the clown", getValue());
    setMaxLength(4);
    assertEquals("the", getValue());
  }

  @Test
  public void testTrimText() {
    setMultilineText(true);

    setTrimText(true);
    setValue("  a  b  ");
    assertEquals("a  b", getValue());
    setValue("\n  a \n b  \n");
    assertEquals("a \n b", getValue());
    setValue(null);
    assertEquals(null, getValue());

    setTrimText(false);
    setValue("  a  b  ");
    assertEquals("  a  b  ", getValue());
    setValue("\n  a \n b  \n");
    assertEquals("\n  a \n b  \n", getValue());
    setValue(null);
    assertEquals(null, getValue());

    // set value
    setValue("  a  b  ");
    assertEquals("  a  b  ", getValue());
    setTrimText(true);
    assertEquals("a  b", getValue());
  }

  @Test
  public void testMultilineText() {
    setMultilineText(false);

    setValue("a\n\nb");
    assertEquals("a  b", getValue());
    setValue(null);
    assertEquals(null, getValue());

    setMultilineText(true);
    setValue("a\n\nb");
    assertEquals("a\n\nb", getValue());
    setValue(null);
    assertEquals(null, getValue());

    // set value
    setMultilineText(true);
    setValue("a\nb");
    assertEquals("a\nb", getValue());
    setMultilineText(false);
    assertEquals("a b", getValue());
  }
}
