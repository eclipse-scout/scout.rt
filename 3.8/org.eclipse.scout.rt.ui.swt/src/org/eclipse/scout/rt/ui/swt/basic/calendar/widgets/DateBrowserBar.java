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

import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Top calendar bar with date browsing buttons.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class DateBrowserBar extends Composite {

  /** parent ref */
  private SwtCalendar m_calendar;

  /** contained widgets */
  private Button m_fastbackward;
  private Button m_backward;
  private Label m_label;
  private Button m_forward;
  private Button m_fastforward;

  public DateBrowserBar(SwtCalendar parent, int style) {
    super(parent, style);

    // ref to parent
    m_calendar = parent;

    createControls();
    hookListeners();
  }

  protected void createControls() {

    GridData gd;

    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
    this.setLayoutData(gd);

    // create new grid layout
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    layout.numColumns = 5;
    this.setLayout(layout);

    // button 1a
    m_fastbackward = new Button(this, SWT.FLAT);
    m_fastbackward.setText("\u00AB" /*"«"*/);
    m_fastbackward.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.horizontalAlignment = GridData.BEGINNING;
    gd.heightHint = 20;
    gd.widthHint = 20;
    m_fastbackward.setLayoutData(gd);

    // button 1b
    m_backward = new Button(this, SWT.FLAT);
    m_backward.setText("\u2039" /*"‹"*/);
    m_backward.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.horizontalAlignment = GridData.BEGINNING;
    gd.heightHint = 20;
    gd.widthHint = 20;
    m_backward.setLayoutData(gd);

    // label
    m_label = new Label(this, SWT.CENTER);
    m_label.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.horizontalAlignment = GridData.CENTER;
    gd.grabExcessHorizontalSpace = true;
    m_label.setLayoutData(gd);

    // button 2a
    m_forward = new Button(this, SWT.FLAT);
    m_forward.setText("\u203A" /*"›"*/);
    m_forward.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.horizontalAlignment = GridData.END;
    gd.heightHint = 20;
    gd.widthHint = 20;
    m_forward.setLayoutData(gd);

    // button 2b
    m_fastforward = new Button(this, SWT.FLAT);
    m_fastforward.setText("\u00BB" /*"»"*/);
    m_fastforward.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.horizontalAlignment = GridData.END;
    gd.heightHint = 20;
    gd.widthHint = 20;
    m_fastforward.setLayoutData(gd);

    setBackground(SwtColors.getInstance().getWhite());
  }

  public void setHeaderText(String header) {
    m_label.setText(header);
  }

  protected void hookListeners() {

    // listeners for date browsing
    m_fastbackward.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_calendar.fastBackward();
      }
    });
    m_backward.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_calendar.backward();
      }
    });
    m_forward.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_calendar.forward();
      }
    });
    m_fastforward.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_calendar.fastForward();
      }
    });
  }

  @Override
  public void dispose() {
    if (m_fastbackward != null && !m_fastbackward.isDisposed()) {
      m_fastbackward.dispose();
    }

    if (m_backward != null && !m_backward.isDisposed()) {
      m_backward.dispose();
    }

    if (m_label != null && !m_label.isDisposed()) {
      m_label.dispose();
    }

    if (m_forward != null && !m_forward.isDisposed()) {
      m_forward.dispose();
    }

    if (m_fastforward != null && !m_fastforward.isDisposed()) {
      m_fastforward.dispose();
    }

    super.dispose();
  }

}
