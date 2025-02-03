/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup;

import static java.util.Collections.unmodifiableList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.internal.RadioButtonGroupGrid;
import org.junit.Test;

public class RadioButtonGroupGridTest {

  @Test
  public void testSingleButtonNoConfig() {
    assertGrid(1, 1, createGrid(1, 0, IRadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT, 0));
  }

  @Test
  public void testSingleButtonWithHeightAndColConfig() {
    assertGrid(1, 1, createGrid(1, 0, 4, 3));
  }

  @Test
  public void testSingleButtonWithColConfig() {
    assertGrid(1, 1, createGrid(1, 0, 4, 0));
  }

  @Test
  public void testMultipleButtonsWithHeightConfigLargetThanNumButtons() {
    assertGrid(1, 1, createGrid(1, 4, IRadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT, 4));
  }

  @Test
  public void testMultipleButtonsWithHeightConfig() {
    assertGrid(2, 2, createGrid(4, 4, IRadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT, 2));
  }

  @Test
  public void testMultipleButtonsWithHeightAndColConfig() {
    assertGrid(3, 2, createGrid(4, 4, 3, 2));
  }

  /**
   * Tests that by default the buttons use a vertical layout
   */
  @Test
  public void testMultipleButtonsNoConfig() {
    assertGrid(1, 8, createGrid(8, 1, IRadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT, 0));
  }

  @Test
  public void testNoButtonsNoConfig() {
    assertGrid(1, 0, createGrid(0, 3, IRadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT, 0));
  }

  @Test
  public void testNoButtonsWithHeightConfig() {
    assertGrid(1, 0, createGrid(0, 3, IRadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT, 3));
  }

  @Test
  public void testMultipleButtonsWithZeroColConfig() {
    assertGrid(1, 5, createGrid(5, 3, 0, 0));
  }

  @Test
  public void testNoButtonsWithHeightAndColConfig() {
    assertGrid(1, 0, createGrid(0, 3, 4, 3));
  }

  private void assertGrid(int expectedColumnCount, int expectedRowCount, RadioButtonGroupGrid grid) {
    assertEquals(expectedColumnCount, grid.getGridColumnCount());
    assertEquals(expectedRowCount, grid.getGridRowCount());
  }

  private RadioButtonGroupGrid createGrid(int numVisibleButtons, int numHiddenButtons, int configuredGridColumnCount, int configuredGridH) {
    RadioButtonGroupGrid grid = new RadioButtonGroupGrid();
    IRadioButtonGroup radioButtonGroup = mock(IRadioButtonGroup.class);

    // setup child buttons
    List<IFormField> fields = new ArrayList<>(numHiddenButtons + numVisibleButtons);
    for (int i = 0; i < numVisibleButtons + numHiddenButtons; i++) {
      IRadioButton button = mock(IRadioButton.class);
      when(button.isVisible()).thenReturn(i < numVisibleButtons);
      when(button.getGridDataHints()).thenReturn(new GridData(0, 0, 0, 0, 0, 0));
      fields.add(button);
    }

    System.out.println("  visible: " + fields.stream().filter(IFormField::isVisible).count());
    System.out.println("invisible: " + fields.stream().filter(f -> !f.isVisible()).count());
    System.out.println();

    // setup gridData
    GridData gridData = new GridData(0, 0, 0, configuredGridH, 1.0, 1.0);

    when(radioButtonGroup.getFields())
        .thenReturn(unmodifiableList(fields));
    when(radioButtonGroup.getGridColumnCount())
        .thenReturn(configuredGridColumnCount);
    when(radioButtonGroup.getGridData())
        .thenReturn(gridData);
    grid.validate(radioButtonGroup);
    return grid;
  }
}
