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
describe('scout.icons', function() {

  var icon;

  it('parses bitmap icons', function() {
    icon = scout.icons.parseIconId('foo.png');
    expect(icon.isBitmap()).toBe(true);
    expect(icon.iconUrl).toBe('foo.png');
    expect(icon.iconCharacter).toBe(undefined);
    expect(icon.font).toBe(undefined);
  });

  it('parses font icons (scoutIcons font)', function() {
    icon = scout.icons.parseIconId('font:x');
    expect(icon.isFontIcon()).toBe(true);
    expect(icon.iconUrl).toBe(undefined);
    expect(icon.iconCharacter).toBe('x');
    expect(icon.font).toBe('scoutIcons');
  });

  it('parses font icons (custom font)', function() {
    icon = scout.icons.parseIconId('font:widgetIcons x');
    expect(icon.isFontIcon()).toBe(true);
    expect(icon.iconUrl).toBe(undefined);
    expect(icon.iconCharacter).toBe('x');
    expect(icon.font).toBe('widgetIcons');
  });

  it('parses returns a CSS class for custom fonts', function() {
    icon = new scout.Icon();
    icon.iconType = scout.Icon.IconType.FONT_ICON;
    icon.font = 'widgetIcons';
    expect(icon.cssClass()).toBe('font-widgetIcons');
  });

  it('appends CSS class string with custom fonts', function() {
    icon = new scout.Icon();
    icon.iconType = scout.Icon.IconType.FONT_ICON;
    icon.font = 'widgetIcons';
    expect(icon.appendCssClass('font-icon')).toBe('font-icon font-widgetIcons');
  });

});
