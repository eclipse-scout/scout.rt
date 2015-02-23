/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.basic.calendar.widgets;

import java.util.ArrayList;

import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Label + combo widget.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class LabelledCombo extends Composite {

  // included widgets
  private Label label;
  private Combo combo;

  public LabelledCombo(Composite parent, int style) {
    super(parent, style);

    createControls();
    hookListeners();
  }

  protected void createControls() {

    GridData gd;

    gd = new GridData();
    gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
    this.setLayoutData(gd);

    // create new grid layout
    GridLayout layout = new GridLayout();
    layout.horizontalSpacing = 3;
    layout.numColumns = 2;
    this.setLayout(layout);

    // create label
    label = new Label(this, SWT.NONE);
    label.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
    label.setLayoutData(gd);

    // create combo
    combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
    gd = new GridData();
    gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
    combo.setLayoutData(gd);

  }

  public void comboAdd(String entry) {
    combo.add(entry);
  }

  public void comboSelect(int position) {
    combo.select(position);
  }

  public int comboGetSelection() {
    return combo.getSelectionIndex();
  }

  public void labelSetText(String text) {
    label.setText(text);
  }

  public void comboSetVisibleItemCount(int count) {
    combo.setVisibleItemCount(count);
  }

  /** list of combo's selection listeners */
  private ArrayList<SelectionListener> listeners = new ArrayList<SelectionListener>();

  /** redirect the selection listener of the combo to the registered listeners */
  public void hookListeners() {
    combo.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        for (SelectionListener l : listeners) {
          if (l != null) {
            l.widgetSelected(e);
          }
        }
      }
    });
  }

  /** add combo selection listener */
  public void addSelectionListener(SelectionListener listener) {
    if (listener != null) {
      listeners.add(listener);
    }
  }

  /** remove combo selection listener */
  public void removeSelectionListener(SelectionListener listener) {
    listeners.remove(listener);
  }

}
