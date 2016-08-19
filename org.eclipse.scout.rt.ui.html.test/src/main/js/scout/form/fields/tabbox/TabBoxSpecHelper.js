/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TabBoxSpecHelper = function(session) {
  this.session = session;
  this.formHelper = new scout.FormSpecHelper(session);
};

scout.TabBoxSpecHelper.prototype.createTabBox = function(tabItems) {
  tabItems = scout.nvl(tabItems, []);
  // Form is necessary to make keystrokes work
  var form = this.formHelper.createFormWithOneField(this.session.desktop);
  var tabBox = this.formHelper.createField('TabBox', form, {
    selectedTab: 0,
    tabItems: tabItems
  });
  form.render(this.session.$entryPoint);
  return tabBox;
};

scout.TabBoxSpecHelper.prototype.createTabItem = function(modelProperties) {
  var model = $.extend({label: 'Foo'}, modelProperties);
  return this.formHelper.createField('TabItem', this.session.desktop, model);
};
