/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {IconDesc, icons} from '../../src/index';

describe('icons', () => {

  let icon: IconDesc;

  it('parses bitmap icons', () => {
    icon = icons.parseIconId('foo.png');
    expect(icon.isBitmap()).toBe(true);
    expect(icon.iconUrl).toBe('foo.png');
    expect(icon.iconCharacter).toBeNull();
    expect(icon.font).toBeNull();
  });

  it('parses font icons (scoutIcons font)', () => {
    icon = icons.parseIconId('font:x');
    expect(icon.isFontIcon()).toBe(true);
    expect(icon.iconUrl).toBeNull();
    expect(icon.iconCharacter).toBe('x');
    expect(icon.font).toBe('scoutIcons');
  });

  it('parses font icons (custom font)', () => {
    icon = icons.parseIconId('font:widgetIcons x');
    expect(icon.isFontIcon()).toBe(true);
    expect(icon.iconUrl).toBeNull();
    expect(icon.iconCharacter).toBe('x');
    expect(icon.font).toBe('widgetIcons');
  });

  it('parses returns a CSS class for custom fonts', () => {
    icon = new IconDesc();
    icon.iconType = IconDesc.IconType.FONT_ICON;
    icon.font = 'widgetIcons';
    expect(icon.cssClass()).toBe('font-widgetIcons');
  });

  it('appends CSS class string with custom fonts', () => {
    icon = new IconDesc();
    icon.iconType = IconDesc.IconType.FONT_ICON;
    icon.font = 'widgetIcons';
    expect(icon.appendCssClass('font-icon')).toBe('font-icon font-widgetIcons');
  });

  it('resolves iconId', () => {
    expect(icons.resolveIconId('${iconId:ANGLE_UP}')).toBe(icons.ANGLE_UP);
    expect(icons.resolveIconId('${iconId:_DOES_NOT_EXIST_}')).toBe(undefined);
    expect(icons.resolveIconId('foo ${iconId:ANGLE_UP}')).toBe('foo ${iconId:ANGLE_UP}');
    expect(icons.resolveIconId('bar')).toBe('bar');
    expect(icons.resolveIconId(icons.INFO)).toBe(icons.INFO);

    window['testIconsSpec'] = {icons: {FOO: 'bar'}}; // dummy namespace
    expect(icons.resolveIconId('${iconId:testIconsSpec.FOO}')).toBe('bar');
    delete window['testIconsSpec'];
  });

  it('resolves iconId property', () => {
    let obj = {
      iconId: '${iconId:ANGLE_UP}',
      name: 'xyz',
      foo: '${iconId:ANGLE_UP}',
      bar: icons.ANGLE_UP
    };

    icons.resolveIconProperty(obj);
    expect(obj.iconId).toBe(icons.ANGLE_UP);
    expect(obj.name).toBe('xyz');
    expect(obj.foo).toBe('${iconId:ANGLE_UP}');
    expect(obj.bar).toBe(icons.ANGLE_UP);

    icons.resolveIconProperty(obj, 'name');
    expect(obj.iconId).toBe(icons.ANGLE_UP);
    expect(obj.name).toBe('xyz');
    expect(obj.foo).toBe('${iconId:ANGLE_UP}');
    expect(obj.bar).toBe(icons.ANGLE_UP);

    icons.resolveIconProperty(obj, 'foo');
    expect(obj.iconId).toBe(icons.ANGLE_UP);
    expect(obj.name).toBe('xyz');
    expect(obj.foo).toBe(icons.ANGLE_UP);
    expect(obj.bar).toBe(icons.ANGLE_UP);

    icons.resolveIconProperty(obj, 'bar');
    expect(obj.iconId).toBe(icons.ANGLE_UP);
    expect(obj.name).toBe('xyz');
    expect(obj.foo).toBe(icons.ANGLE_UP);
    expect(obj.bar).toBe(icons.ANGLE_UP);

    icons.resolveIconProperty(obj, 'doesNotExist');
    expect(obj.iconId).toBe(icons.ANGLE_UP);
    expect(obj.name).toBe('xyz');
    expect(obj.foo).toBe(icons.ANGLE_UP);
    expect(obj.bar).toBe(icons.ANGLE_UP);
  });
});
