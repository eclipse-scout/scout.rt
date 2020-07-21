/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package com.bsiag.scout.rt.client.ui.basic.chart;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ChartChains {

  private ChartChains() {
  }

  protected abstract static class AbstractChartChain extends AbstractExtensionChain<IChartExtension<? extends AbstractChart>> {

    public AbstractChartChain(List<? extends IChartExtension<? extends AbstractChart>> extensions) {
      super(extensions, IChartExtension.class);
    }
  }

  public static class ChartValueClickChain extends AbstractChartChain {

    public ChartValueClickChain(List<? extends IChartExtension<? extends AbstractChart>> extensions) {
      super(extensions);
    }

    public void execValueClick(BigDecimal xIndex, BigDecimal yIndex, Integer datasetIndex) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IChartExtension<? extends AbstractChart> next) {
          next.execValueClick(ChartValueClickChain.this, xIndex, yIndex, datasetIndex);
        }
      };
      callChain(methodInvocation, xIndex, yIndex, datasetIndex);
    }
  }

}
