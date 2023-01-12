/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.listbox;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public final class ListBoxChains {

  private ListBoxChains() {
  }

  protected abstract static class AbstractListBoxChain<KEY> extends AbstractExtensionChain<IListBoxExtension<KEY, ? extends AbstractListBox<KEY>>> {

    public AbstractListBoxChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IListBoxExtension.class);
    }
  }

  public static class ListBoxPopulateTableChain<KEY> extends AbstractListBoxChain<KEY> {

    public ListBoxPopulateTableChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPopulateTable() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IListBoxExtension<KEY, ? extends AbstractListBox<KEY>> next) {
          next.execPopulateTable(ListBoxPopulateTableChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class ListBoxLoadTableDataChain<KEY> extends AbstractListBoxChain<KEY> {

    public ListBoxLoadTableDataChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public List<? extends ILookupRow<KEY>> execLoadTableData() {
      MethodInvocation<List<? extends ILookupRow<KEY>>> methodInvocation = new MethodInvocation<List<? extends ILookupRow<KEY>>>() {
        @Override
        protected void callMethod(IListBoxExtension<KEY, ? extends AbstractListBox<KEY>> next) {
          setReturnValue(next.execLoadTableData(ListBoxLoadTableDataChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ListBoxFilterLookupResultChain<KEY> extends AbstractListBoxChain<KEY> {

    public ListBoxFilterLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterLookupResult(final ILookupCall<KEY> call, final List<ILookupRow<KEY>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IListBoxExtension<KEY, ? extends AbstractListBox<KEY>> next) {
          next.execFilterLookupResult(ListBoxFilterLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class ListBoxPrepareLookupChain<KEY> extends AbstractListBoxChain<KEY> {

    public ListBoxPrepareLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareLookup(final ILookupCall<KEY> call) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IListBoxExtension<KEY, ? extends AbstractListBox<KEY>> next) {
          next.execPrepareLookup(ListBoxPrepareLookupChain.this, call);
        }
      };
      callChain(methodInvocation);
    }
  }
}
