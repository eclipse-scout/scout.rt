/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.form.fields.svgfield.internal;

import org.eclipse.scout.rt.shared.data.form.fields.svgfield.IScoutSVGElement;
import org.eclipse.scout.rt.shared.data.form.fields.svgfield.ScoutSVGModel;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Control;

public class DummySvgRenderer implements ISvgRenderer {
  private final ISwtEnvironment m_env;

  private Transform m_tx;

  public DummySvgRenderer(ScoutSVGModel svg, Control owner, ISwtEnvironment env) {
    m_env = env;
    owner.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });
    m_tx = new Transform(owner.getDisplay());
  }

  private void freeResources() {
    if (m_tx != null && !m_tx.isDisposed()) {
      System.out.println("free tx");
      m_tx.dispose();
      m_tx = null;
    }
  }

  @Override
  public double getWidth() {
    return 200;
  }

  @Override
  public double getHeight() {
    return 200;
  }

  @Override
  public void paint(GC g, int offsetX, int offsetY, double scalingFactor) {
    m_tx.identity();
    m_tx.translate(offsetX, offsetY);
    m_tx.scale((float) scalingFactor, (float) scalingFactor);
    g.setTransform(m_tx);
    g.setBackground(m_env.getColor(new RGB(255, 0, 0)));
    g.fillRectangle(10, 10, 100, 100);
    g.setForeground(m_env.getColor(new RGB(30, 30, 30)));
    g.drawString("TEXT", 80, 80);
  }

  @Override
  public IScoutSVGElement elementAtModelLocation(double modelX, double modelY, Boolean interactive) {
    return null;
  }

  @Override
  public void dispose() {
  }
}
