/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('ObjectInfo', function() {

  describe('parse', function() {

    it('parses objectType only - without namespace', function() {
      var info = scout.TypeDescriptor.parse('StringField');
      expect(info.modelVariant).toBe(null);
      expect(info.objectType.name).toBe('StringField');
      expect(scout.arrays.empty(info.objectType.namespaces)).toBe(true);
    });

    it('parses objectType only - with namespace', function() {
      var info = scout.TypeDescriptor.parse('my.StringField');
      expect(info.modelVariant).toBe(null);
      expect(info.objectType.name).toBe('StringField');
      expect(info.objectType.namespaces).toEqual(['my']);
    });

    it('parses objectType and variant - without namespace', function() {
      var info = scout.TypeDescriptor.parse('StringField:Variant');
      expect(info.modelVariant.name).toBe('Variant');
      expect(scout.arrays.empty(info.modelVariant.namespaces)).toBe(true);
      expect(info.objectType.name).toBe('StringField');
      expect(scout.arrays.empty(info.objectType.namespaces)).toBe(true);
    });

    it('parses objectType and variant - with namespace on objectType (model variant inherits namespace from object type)', function() {
      var info = scout.TypeDescriptor.parse('my.StringField:Variant');
      expect(info.modelVariant.name).toBe('Variant');
      expect(info.modelVariant.namespaces).toEqual(['my']);
      expect(info.objectType.name).toBe('StringField');
      expect(info.objectType.namespaces).toEqual(['my']);
    });

    it('parses objectType and variant - different namespaces for model variant and  object type', function() {
      var info = scout.TypeDescriptor.parse('my.StringField:your.Variant');
      expect(info.modelVariant.name).toBe('Variant');
      expect(info.modelVariant.namespaces).toEqual(['your']);
      expect(info.objectType.name).toBe('StringField');
      expect(info.objectType.namespaces).toEqual(['my']);
    });

    it('parses objectType with multiple nested namespaces', function() {
      var info = scout.TypeDescriptor.parse('my.fair.StringField');
      expect(info.modelVariant).toBe(null);
      expect(info.objectType.name).toBe('StringField');
      expect(info.objectType.namespaces).toEqual(['my', 'fair']);
    });

  });

  describe('toString', function() {

    it('returns correct string', function() {
      var info = scout.TypeDescriptor.parse('my.pretty.StringField:your.Variant');
      expect(info.objectType.toString()).toBe('my.pretty.StringField');
      expect(info.modelVariant.toString()).toBe('your.Variant');
    });

  });

});