/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('SimpleTabArea', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  it('renders the tabs in the correct order', function() {
    var tabBox = scout.create('SimpleTabBox', {
      parent: session.desktop
    });
    var view1 = scout.create('GroupBox', {
      parent: tabBox,
      title: 'One'
    });
    var view2 = scout.create('GroupBox', {
      parent: tabBox,
      title: 'Two'
    });
    var view3 = scout.create('GroupBox', {
      parent: tabBox,
      title: 'Three'
    });
    tabBox.addView(view1);
    tabBox.addView(view2, false);
    tabBox.addView(view3, false);

    expect(tabBox.tabArea.tabs.length).toBe(3);
    expect(tabBox.tabArea.tabs[0].view).toBe(view1);
    expect(tabBox.tabArea.tabs[1].view).toBe(view2);
    expect(tabBox.tabArea.tabs[2].view).toBe(view3);
    expect(tabBox.tabArea.tabs[0].title).toBe('One');
    expect(tabBox.tabArea.tabs[1].title).toBe('Two');
    expect(tabBox.tabArea.tabs[2].title).toBe('Three');
    expect(tabBox.tabArea.tabs[0].selected).toBe(true);
    expect(tabBox.tabArea.tabs[1].selected).toBe(false);
    expect(tabBox.tabArea.tabs[2].selected).toBe(false);

    // -----

    tabBox.render();
    expect(tabBox.$tabArea).toBeTruthy();
    expect(tabBox.$tabArea.children().length).toBe(3);
    expect(tabBox.$tabArea.children().eq(0)).toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(0).text().trim()).toBe('One');
    expect(tabBox.$tabArea.children().eq(1)).not.toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(1).text().trim()).toBe('Two');
    expect(tabBox.$tabArea.children().eq(2)).not.toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(2).text().trim()).toBe('Three');

    tabBox.remove();
    expect(tabBox.$tabArea).not.toBeTruthy;

    tabBox.render();
    expect(tabBox.$tabArea).toBeTruthy();
    expect(tabBox.$tabArea.children().length).toBe(3);
    expect(tabBox.$tabArea.children().eq(0)).toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(0).text().trim()).toBe('One');
    expect(tabBox.$tabArea.children().eq(1)).not.toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(1).text().trim()).toBe('Two');
    expect(tabBox.$tabArea.children().eq(2)).not.toHaveClass('selected');
    expect(tabBox.$tabArea.children().eq(2).text().trim()).toBe('Three');
  });

});
