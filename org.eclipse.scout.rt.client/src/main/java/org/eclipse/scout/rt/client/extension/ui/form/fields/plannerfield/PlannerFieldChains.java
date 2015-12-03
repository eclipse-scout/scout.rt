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
package org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.AbstractPlannerField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PlannerFieldChains {

  private PlannerFieldChains() {
  }

  protected abstract static class AbstractPlannerFieldChain<P extends IPlanner<RI, AI>, RI, AI>
      extends AbstractExtensionChain<IPlannerFieldExtension<? extends IPlanner<RI, AI>, RI, AI, ? extends AbstractPlannerField<? extends IPlanner<RI, AI>, RI, AI>>> {

    public AbstractPlannerFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IPlannerFieldExtension.class);
    }
  }

  public static class PlannerFieldLoadResourcesChain<P extends IPlanner<RI, AI>, RI, AI> extends AbstractPlannerFieldChain<P, RI, AI> {

    public PlannerFieldLoadResourcesChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public List<Resource<RI>> execLoadResourceTableData() {
      MethodInvocation<List<Resource<RI>>> methodInvocation = new MethodInvocation<List<Resource<RI>>>() {
        @Override
        protected void callMethod(IPlannerFieldExtension<? extends IPlanner<RI, AI>, RI, AI, ? extends AbstractPlannerField<? extends IPlanner<RI, AI>, RI, AI>> next) {
          setReturnValue(next.execLoadResources(PlannerFieldLoadResourcesChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class PlannerFieldPopulateResourcesChain<P extends IPlanner<RI, AI>, RI, AI> extends AbstractPlannerFieldChain<P, RI, AI> {

    public PlannerFieldPopulateResourcesChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPopulateResources() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerFieldExtension<? extends IPlanner<RI, AI>, RI, AI, ? extends AbstractPlannerField<? extends IPlanner<RI, AI>, RI, AI>> next) {
          next.execPopulateResources(PlannerFieldPopulateResourcesChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

}
