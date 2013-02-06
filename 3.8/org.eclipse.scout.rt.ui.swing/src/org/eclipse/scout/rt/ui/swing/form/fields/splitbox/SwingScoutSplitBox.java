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
package org.eclipse.scout.rt.ui.swing.form.fields.splitbox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JSplitPane;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;

/**
 * <h3>SwingScoutSplitBox</h3> ...
 * 
 * @since 3.1.13 17.07.2008
 */
public class SwingScoutSplitBox extends SwingScoutFieldComposite<ISplitBox> implements ISwingScoutSplitBox {

  private static IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutSplitBox.class);
  private OptimisticLock lockSplitter;

  public SwingScoutSplitBox() {
    lockSplitter = new OptimisticLock();
  }

  @Override
  protected void initializeSwing() {
    JSplitPane container = new JSplitPane(getScoutObject().isSplitHorizontal() ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT, true);
    container.setName(getScoutObject().getClass().getSimpleName() + ".container");
    container.setBorder(null);
    IFormField[] scoutFields = getScoutObject().getFields();
    if (scoutFields.length == 2) {
      ISwingScoutFormField swingScoutComposite = getSwingEnvironment().createFormField(container, scoutFields[0]);
      container.add(swingScoutComposite.getSwingContainer());
      ISwingScoutFormField swingScoutComposite1 = getSwingEnvironment().createFormField(container, scoutFields[1]);
      container.add(swingScoutComposite1.getSwingContainer());
      container.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          setSplitterPositionFromSwing();
        }
      });
    }
    else {
      LOG.error("SplitBox allows exact 2 inner fields.");
    }
    setSwingContainer(container);
  }

  @Override
  protected void attachScout() {
    super.attachScout();

    if (getScoutObject().isCacheSplitterPosition()) {
      setCachedSplitterPosition();
    }
    else {
      setSplitterPositionFromScout();
    }
  }

  protected void setCachedSplitterPosition() {
    int[] a = ClientUIPreferences.getInstance(getSwingEnvironment().getScoutSession()).getSplitterPosition(getScoutObject());
    if (a != null && a.length == 2) {
      setSplitterPosition(a[0], a[1]);
    }
    else {
      setSplitterPositionFromScout();
    }
  }

  protected void setSplitterPositionFromScout() {
    try {
      if (lockSplitter.acquire()) {
        double p = getScoutObject().getSplitterPosition();
        if (p > 0 && p < 1.0) {
          getSwingContainer().setDividerLocation(p);
          getSwingContainer().setResizeWeight(p);
        }
      }
    }
    finally {
      lockSplitter.release();
    }
  }

  protected void setSplitterPosition(int leftWidth, int rightWidth) {
    try {
      if (lockSplitter.acquire()) {
        double total = leftWidth + rightWidth;
        if (total > 0 && leftWidth > 0) {
          double d = leftWidth / total;
          // getSwingContainer().setDividerLocation(d);
          // hmu 2010.03.24
          getSwingContainer().setDividerLocation(leftWidth);
          getSwingContainer().setResizeWeight(d);
        }
      }
    }
    finally {
      lockSplitter.release();
    }
  }

  protected void setSplitterPositionFromSwing() {
    if (getScoutObject().isCacheSplitterPosition()) {
      int total = (getSwingContainer().getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? getSwingContainer().getWidth() : getSwingContainer().getHeight()) - getSwingContainer().getDividerSize();
      if (total > 32) {
        int left = getSwingContainer().getDividerLocation();
        int right = total - left;
        cacheSplitterPosition(new int[]{left, right});
      }
    }
    else {
      cacheSplitterPosition(null);
    }
  }

  protected void cacheSplitterPosition(final int[] weights) {
    ClientUIPreferences.getInstance(getSwingEnvironment().getScoutSession()).setSplitterPosition(getScoutObject(), weights);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (ISplitBox.PROP_SPLITTER_ENABLED.equals(name)) {
    }
    else if (ISplitBox.PROP_SPLITTER_POSITION.equals(name)) {
      setSplitterPositionFromScout();
    }
    super.handleScoutPropertyChange(name, newValue);
  }

  @Override
  public JSplitPane getSwingContainer() {
    return (JSplitPane) super.getSwingContainer();
  }

}
