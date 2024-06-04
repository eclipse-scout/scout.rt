/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EventListener, EventSupport, Form, GroupBox, HtmlComponent, Menu, NullWidget, ObjectFactory, scout, StringField, TableRow, TreeVisitResult, Widget} from '../../src/index';
import {InitModelOf} from '../../src/scout';

describe('Widget', () => {

  let session: SandboxSession, parent: TestWidget;

  class TestWidget extends NullWidget {
    declare events: EventSupport & { _eventListeners: EventListener[] };
    selected: true;

    override _render() {
      this.$container = this.$parent.appendDiv();
      this.$container.setTabbable(true);
      this.htmlComp = HtmlComponent.install(this.$container, this.session);
      this.htmlComp.getParent = () => {
        return null; // Detach from parent because our parent does not lay out children
      };
    }

    override _remove() {
      super._remove();
    }
  }

  class SubTestWidget extends TestWidget {
  }

  class AnotherTestWidget extends NullWidget {
  }

  class ScrollableWidget extends NullWidget {
    $elem: JQuery;

    override _render() {
      this.$container = this.$parent.appendDiv();
      this.htmlComp = HtmlComponent.install(this.$container, this.session);
      this.htmlComp.getParent = () => {
        return null; // Detach from parent because our parent does not lay out children
      };
      this.$container.css({
        position: 'absolute',
        minHeight: 50,
        minWidth: 50
      });
      this.$elem = this.$container.appendDiv();
      this.$elem.css({
        display: 'inline-block',
        position: 'absolute',
        minHeight: 100,
        minWidth: 100
      });
      this._installScrollbars({
        axis: 'both'
      });
    }

    override _uninstallScrollbars() {
      super._uninstallScrollbars();
    }
  }

  class ComputedWidget extends Widget {
    _computed: boolean;

    constructor() {
      super();
      this._computed = true;
      this._addComputedProperties(['computed']);
      this._addCloneProperties(['computed']);
    }

    get computed() {
      return this._computed;
    }
  }

  ObjectFactory.get().registerNamespace('testns', {ComputedWidget});

  function appendAnimateRemoveStyle() {
    $(`<style>
      @keyframes nop { 0% { opacity: 1; } 100% { opacity: 1; } }
      .animate-remove { animation: nop; animation-duration: 1s;}
      </style>`).appendTo($('#sandbox'));
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();

    parent = new TestWidget();
    parent.init({
      parent: session.root,
      session: session
    });
  });

  function createWidget(model?: any): TestWidget {
    let defaults = {
      parent: parent,
      session: session
    };
    model = $.extend({}, defaults, model);
    let widget = new TestWidget();
    widget.init(model);
    return widget;
  }

  describe('visitChildren', () => {
    let child1, child2, grandChild1, grandChild2, grandChild2_1;

    function createVisitStructure() {
      child1 = new TestWidget();
      child1.init({
        id: 'child1',
        parent: parent,
        wasVisited: false
      });
      child2 = new TestWidget();
      child2.init({
        id: 'child2',
        parent: parent,
        wasVisited: false
      });
      grandChild1 = new TestWidget();
      grandChild1.init({
        id: 'grandChild1',
        parent: child1,
        wasVisited: false
      });
      grandChild2 = new TestWidget();
      grandChild2.init({
        id: 'grandChild2',
        parent: child1,
        wasVisited: false
      });
      grandChild2_1 = new TestWidget();
      grandChild2_1.init({
        parent: child2,
        wasVisited: false
      });
    }

    it('visits all descendants', () => {
      createWidget({
        parent: parent
      });
      let counter = 0;
      parent.visitChildren(item => {
        counter++;
      });
      expect(counter).toBe(1); /* parent itself is not visited, only children */
    });

    it('can be aborted when returning true', () => {
      createVisitStructure();

      // Abort at grandChild1 -> don't visit siblings of grandChild1 and siblings of parent
      parent.visitChildren((child: any) => {
        child.wasVisited = true;
        return child === grandChild1;
      });
      expect(child1.wasVisited).toBe(true);
      expect(child2.wasVisited).toBe(false);
      expect(grandChild1.wasVisited).toBe(true);
      expect(grandChild2.wasVisited).toBe(false);
      expect(grandChild2_1.wasVisited).toBe(false);

      // Reset wasVisited flag
      parent.visitChildren((child: any) => {
        child.wasVisited = false;
        return false;
      });
      expect(child1.wasVisited).toBe(false);
      expect(child2.wasVisited).toBe(false);
      expect(grandChild1.wasVisited).toBe(false);
      expect(grandChild2.wasVisited).toBe(false);
      expect(grandChild2_1.wasVisited).toBe(false);

      // Abort at child2 -> don't visit children of child2
      parent.visitChildren((child: any) => {
        child.wasVisited = true;
        return child === child2;
      });
      expect(child1.wasVisited).toBe(true);
      expect(child2.wasVisited).toBe(true);
      expect(grandChild1.wasVisited).toBe(true);
      expect(grandChild2.wasVisited).toBe(true);
      expect(grandChild2_1.wasVisited).toBe(false);
    });

    it('can be aborted when returning TreeVisitResult.TERMINATE', () => {
      createVisitStructure();

      // Abort at grandChild1 -> don't visit siblings of grandChild1 and siblings of parent
      parent.visitChildren((child: any) => {
        child.wasVisited = true;
        if (child === grandChild1) {
          return TreeVisitResult.TERMINATE;
        }
      });
      expect(child1.wasVisited).toBe(true);
      expect(child2.wasVisited).toBe(false);
      expect(grandChild1.wasVisited).toBe(true);
      expect(grandChild2.wasVisited).toBe(false);
      expect(grandChild2_1.wasVisited).toBe(false);

      // Reset wasVisited flag
      parent.visitChildren((child: any) => {
        child.wasVisited = false;
        return false;
      });
      expect(child1.wasVisited).toBe(false);
      expect(child2.wasVisited).toBe(false);
      expect(grandChild1.wasVisited).toBe(false);
      expect(grandChild2.wasVisited).toBe(false);
      expect(grandChild2_1.wasVisited).toBe(false);

      // Abort at child2 -> don't visit children of child2
      parent.visitChildren((child: any) => {
        child.wasVisited = true;
        if (child === child2) {
          return TreeVisitResult.TERMINATE;
        }
      });
      expect(child1.wasVisited).toBe(true);
      expect(child2.wasVisited).toBe(true);
      expect(grandChild1.wasVisited).toBe(true);
      expect(grandChild2.wasVisited).toBe(true);
      expect(grandChild2_1.wasVisited).toBe(false);
    });

    it('can skip a subtree when returning TreeVisitResult.SKIP_SUBTREE', () => {
      createVisitStructure();

      // Abort at grandChild1 -> don't visit siblings of grandChild1 and siblings of parent
      parent.visitChildren((child: any) => {
        child.wasVisited = true;
        if (child === child1) {
          return TreeVisitResult.SKIP_SUBTREE;
        }
      });
      expect(child1.wasVisited).toBe(true);
      expect(child2.wasVisited).toBe(true);
      expect(grandChild1.wasVisited).toBe(false);
      expect(grandChild2.wasVisited).toBe(false);
      expect(grandChild2_1.wasVisited).toBe(true);

      // Reset wasVisited flag
      parent.visitChildren((child: any) => {
        child.wasVisited = false;
        return false;
      });
      expect(child1.wasVisited).toBe(false);
      expect(child2.wasVisited).toBe(false);
      expect(grandChild1.wasVisited).toBe(false);
      expect(grandChild2.wasVisited).toBe(false);
      expect(grandChild2_1.wasVisited).toBe(false);

      // Abort at child2 -> don't visit children of child2
      parent.visitChildren((child: any) => {
        child.wasVisited = true;
        if (child === child2) {
          return TreeVisitResult.SKIP_SUBTREE;
        }
      });
      expect(child1.wasVisited).toBe(true);
      expect(child2.wasVisited).toBe(true);
      expect(grandChild1.wasVisited).toBe(true);
      expect(grandChild2.wasVisited).toBe(true);
      expect(grandChild2_1.wasVisited).toBe(false);
    });
  });

  describe('widget', () => {
    it('finds a child with the given widget id', () => {
      let child1 = new TestWidget();
      child1.init({
        id: 'child1',
        parent: parent
      });
      let child2 = new TestWidget();
      child2.init({
        id: 'child2',
        parent: parent
      });
      let grandChild1 = new TestWidget();
      grandChild1.init({
        id: 'grandChild1',
        parent: child1
      });
      let grandChild2 = new TestWidget();
      grandChild2.init({
        id: 'grandChild2',
        parent: child1
      });
      expect(parent.widget('child1')).toBe(child1);
      expect(parent.widget('child2')).toBe(child2);
      expect(parent.widget('grandChild1')).toBe(grandChild1);
      expect(parent.widget('grandChild2')).toBe(grandChild2);
    });

    it('does not visit other children if the child has been found', () => {
      let child1 = new TestWidget();
      child1.init({
        id: 'child1',
        parent: parent
      });
      let child2 = new TestWidget();
      child2.init({
        id: 'child2',
        parent: parent
      });
      let grandChild1 = new TestWidget();
      grandChild1.init({
        id: 'grandChild1',
        parent: child1
      });
      let grandChild2 = new TestWidget();
      grandChild2.init({
        id: 'grandChild2',
        parent: child1
      });
      let visitChildrenSpy = spyOn(parent, 'visitChildren').and.callThrough();
      expect(visitChildrenSpy.calls.count()).toBe(0);

      expect(parent.widget('child1')).toBe(child1);
      expect(visitChildrenSpy.calls.count()).toBe(1); // Only called once
    });
  });

  describe('nearestWidget', () => {
    it('finds the nearest widget when multiple widgets have the same id', () => {
      // p           parent
      // + w1        child1
      //   + w2      grandChild1         same ID
      // + w2        child2              same ID
      //   + w3      grandChild2
      //   + w2      grandChild3         same ID
      let child1 = new TestWidget();
      child1.init({
        id: 'w1',
        parent: parent
      });
      let child2 = new TestWidget();
      child2.init({
        id: 'w2',
        parent: parent
      });
      let grandChild1 = new TestWidget();
      grandChild1.init({
        id: 'w2',
        parent: child1
      });
      let grandChild2 = new TestWidget();
      grandChild2.init({
        id: 'w3',
        parent: child2
      });
      let grandChild3 = new TestWidget();
      grandChild3.init({
        id: 'w2',
        parent: child2
      });
      expect(parent.widget('w1')).toBe(child1);
      expect(parent.widget('w2')).toBe(grandChild1); // does not find "child2"
      expect(parent.widget('w3')).toBe(grandChild2);
      expect(parent.nearestWidget('w1')).toBe(child1); // same result as widget()
      expect(parent.nearestWidget('w2')).toBe(child2); // unlike widget(), this does find child2
      expect(parent.nearestWidget('w3')).toBe(null); // only checks one level by default
      expect(parent.nearestWidget('w3', true)).toBe(grandChild2); // "deep = true" searches the whole tree
      expect(child2.widget('w2')).toBe(child2); // finds itself
      expect(child2.nearestWidget('w2')).toBe(child2); // finds itself
      expect(child2.nearestWidget('w2', true)).toBe(child2); // finds itself
    });
  });

  describe('findParent', () => {
    it('finds the first ancestor which has the given type', () => {
      let child = new SubTestWidget();
      child.init({
        id: 'child',
        parent: parent
      });
      let grandChild = new AnotherTestWidget();
      grandChild.init({
        id: 'grandChild',
        parent: child
      });
      let grandGrandChild = new AnotherTestWidget();
      grandGrandChild.init({
        id: 'grandGrandChild',
        parent: grandChild
      });
      expect(grandGrandChild.findParent(Widget)).toBe(grandChild);
      expect(grandGrandChild.findParent(AnotherTestWidget)).toBe(grandChild);
      expect(grandGrandChild.findParent(SubTestWidget)).toBe(child);
      expect(grandGrandChild.findParent(TestWidget)).toBe(child);

      let thrown;
      try {
        // @ts-expect-error TableRow does not extend widget -> it will be treated as predicate which won't work (TypeError: Class constructor TableRow cannot be invoked without 'new')
        grandGrandChild.findParent(TableRow);
      } catch (error) {
        thrown = true;
      }
      expect(thrown).toBe(true);
    });

    it('finds the first ancestor that is accepted by the given predicate', () => {
      let child = new SubTestWidget();
      child.init({
        id: 'child',
        parent: parent
      });
      let grandChild = new AnotherTestWidget();
      grandChild.init({
        id: 'grandChild',
        parent: child
      });
      let grandGrandChild = new AnotherTestWidget();
      grandGrandChild.init({
        id: 'grandGrandChild',
        parent: grandChild
      });
      expect(grandGrandChild.findParent(parent => parent.id === 'grandChild')).toBe(grandChild);
      expect(grandGrandChild.findParent(parent => parent.id === 'child')).toBe(child);
      expect(grandGrandChild.findParent(parent => false)).toBe(null);
    });
  });

  describe('findChild', () => {
    it('finds the first child which has the given type', () => {
      let child = new SubTestWidget();
      child.init({
        id: 'child',
        parent: parent
      });
      let grandChild1 = new AnotherTestWidget();
      grandChild1.init({
        id: 'grandChild1',
        parent: child
      });
      let grandChild2 = new AnotherTestWidget();
      grandChild2.init({
        id: 'grandChild2',
        parent: child
      });
      let grandGrandChild = new AnotherTestWidget();
      grandGrandChild.init({
        id: 'grandGrandChild',
        parent: grandChild1
      });
      expect(parent.findChild(Widget)).toBe(child);
      expect(parent.findChild(SubTestWidget)).toBe(child);
      expect(parent.findChild(AnotherTestWidget)).toBe(grandChild1);
      expect(child.findChild(AnotherTestWidget)).toBe(grandChild1);
      expect(grandChild1.findChild(AnotherTestWidget)).toBe(grandGrandChild);
      expect(child.findChild(TestWidget)).toBe(null);

      let thrown;
      try {
        // @ts-expect-error TableRow does not extend widget -> it will be treated as predicate which won't work (TypeError: Class constructor TableRow cannot be invoked without 'new')
        parent.findParent(TableRow);
      } catch (error) {
        thrown = true;
      }
      expect(thrown).toBe(true);
    });

    it('finds the first child that is accepted by the given predicate', () => {
      let child = new SubTestWidget();
      child.init({
        id: 'child',
        parent: parent
      });
      let grandChild1 = new AnotherTestWidget();
      grandChild1.init({
        id: 'grandChild1',
        parent: child
      });
      let grandChild2 = new AnotherTestWidget();
      grandChild2.init({
        id: 'grandChild2',
        parent: child
      });
      let grandGrandChild = new AnotherTestWidget();
      grandGrandChild.init({
        id: 'grandGrandChild',
        parent: grandChild1
      });
      expect(parent.findChild(child => child.id === 'child')).toBe(child);
      expect(parent.findChild(child => child.id === 'grandChild1')).toBe(grandChild1);
      expect(parent.findChild(child => child.id === 'grandChild2')).toBe(grandChild2);
      expect(parent.findChild(child => child.id === 'grandGrandChild')).toBe(grandGrandChild);
      expect(parent.findChild(child => false)).toBe(null);
    });
  });

  describe('enabled', () => {
    it('should be propagated correctly', () => {
      let widget = createWidget({
        parent: parent
      });
      // check setup
      expect(widget.inheritAccessibility).toBe(true);
      expect(widget.parent.inheritAccessibility).toBe(true);
      expect(widget.enabled).toBe(true);
      expect(widget.enabledComputed).toBe(true);
      expect(widget.parent.enabled).toBe(true);
      expect(widget.parent.enabledComputed).toBe(true);

      // check change on widget itself
      widget.setEnabled(false);
      expect(widget.enabled).toBe(false);
      expect(widget.enabledComputed).toBe(false);
      expect(widget.parent.enabled).toBe(true);
      expect(widget.parent.enabledComputed).toBe(true);

      // check that child-propagation works and resets the enabled state
      widget.parent.setEnabled(true, false, true);
      expect(widget.enabled).toBe(true);
      expect(widget.enabledComputed).toBe(true);
      expect(widget.parent.enabled).toBe(true);
      expect(widget.parent.enabledComputed).toBe(true);

      // check that inheritance works
      widget.parent.setEnabled(false, false, false);
      expect(widget.enabled).toBe(true);
      expect(widget.enabledComputed).toBe(false);
      expect(widget.parent.enabled).toBe(false);
      expect(widget.parent.enabledComputed).toBe(false);

      // check that parent-propagation works
      widget.setEnabled(true, true, false);
      expect(widget.enabled).toBe(true);
      expect(widget.enabledComputed).toBe(true);
      expect(widget.parent.enabled).toBe(true);
      expect(widget.parent.enabledComputed).toBe(true);
    });

    it('should not be inherited if inheritAccessibility is disabled', () => {
      let widget = createWidget({
        parent: parent
      });
      // check setup
      widget.setInheritAccessibility(false);
      expect(widget.inheritAccessibility).toBe(false);
      expect(widget.parent.inheritAccessibility).toBe(true);
      expect(widget.enabled).toBe(true);
      expect(widget.enabledComputed).toBe(true);
      expect(widget.parent.enabled).toBe(true);
      expect(widget.parent.enabledComputed).toBe(true);

      // change enabled of parent and verify that it has no effect on child because inheritance is disabled.
      widget.parent.setEnabled(false);
      expect(widget.enabled).toBe(true);
      expect(widget.enabledComputed).toBe(true);
      expect(widget.parent.enabled).toBe(false);
      expect(widget.parent.enabledComputed).toBe(false);
    });

    it('recomputeEnabled should be called for all widgets at least once', () => {
      let widget = scout.create(Form, {
        parent: parent,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: StringField
          }, {
            objectType: StringField,
            inheritAccessibility: false
          }, {
            objectType: StringField,
            enabled: false,
            inheritAccessibility: false
          }],
          menus: [{
            objectType: Menu
          }]
        }
      });

      // check setup
      expect(widget.parent.initialized).toBe(true);
      expect(widget.parent.enabledComputed).toBe(true);
      expect(widget.rootGroupBox.fields.length).toBe(3);
      expect(widget.rootGroupBox.menus.length).toBe(1);
      expect(widget.rootGroupBox.fields[0].enabled).toBe(true);
      expect(widget.rootGroupBox.fields[1].enabled).toBe(true);
      expect(widget.rootGroupBox.fields[2].enabled).toBe(false);
      expect(widget.rootGroupBox.menus[0].enabled).toBe(true);

      // check that enabled
      expect(widget.rootGroupBox.fields[0].enabledComputed).toBe(true);
      expect(widget.rootGroupBox.fields[1].enabledComputed).toBe(true);
      expect(widget.rootGroupBox.fields[2].enabledComputed).toBe(false);
      expect(widget.rootGroupBox.menus[0].enabledComputed).toBe(true);
    });

    it('should correctly recalculate enabled state when adding a new field', () => {
      let widget = createWidget({
        parent: parent
      });
      // check setup
      parent.setEnabled(false);
      expect(widget.enabled).toBe(true);
      expect(widget.enabledComputed).toBe(false);
      expect(widget.parent.enabled).toBe(false);
      expect(widget.parent.enabledComputed).toBe(false);

      // add a new field which itself is enabled
      let additionalWidget = new TestWidget();
      additionalWidget.init({
        parent: session.root
      });

      expect(additionalWidget.enabled).toBe(true);
      expect(additionalWidget.enabledComputed).toBe(true);
      additionalWidget.setParent(widget.parent);

      // check that the new widget is disabled now
      expect(additionalWidget.enabled).toBe(true);
      expect(additionalWidget.enabledComputed).toBe(false);
    });

    it('can be passed as boolean or object', () => {
      let widget = createWidget({
        parent: parent,
        enabled: true
      });
      expect(widget.enabled).toBe(true);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(true);

      widget = createWidget({
        parent: parent,
        enabled: false
      });
      expect(widget.enabled).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(false);

      // Object contains the dimensions
      widget = createWidget({
        parent: parent,
        enabled: {
          default: true
        }
      });
      expect(widget.enabled).toBe(true);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(true);

      widget = createWidget({
        parent: parent,
        enabled: {
          default: false
        }
      });
      expect(widget.enabled).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(false);
    });

    it('always returns false if enabledGranted is false', () => {
      let widget = createWidget({
        parent: parent
      });
      expect(widget.enabled).toBe(true);
      expect(widget.enabledComputed).toBe(true);
      expect(widget.enabledGranted).toBe(true);
      expect(widget.getPropertyDimension('enabled', 'granted')).toBe(true);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(true);

      widget.setEnabledGranted(false);
      expect(widget.enabled).toBe(false);
      expect(widget.enabledComputed).toBe(false);
      expect(widget.enabledGranted).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'granted')).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(true);

      widget = createWidget({
        parent: parent,
        enabled: {
          default: false,
          granted: false
        }
      });
      expect(widget.enabled).toBe(false);
      expect(widget.enabledComputed).toBe(false);
      expect(widget.enabledGranted).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'granted')).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(false);

      widget.setEnabled(true);
      expect(widget.enabled).toBe(false);
      expect(widget.enabledComputed).toBe(false);
      expect(widget.enabledGranted).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'granted')).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(true);

      // EnabledGranted alias wins
      widget = createWidget({
        parent: parent,
        enabledGranted: false,
        enabled: {
          granted: true
        }
      });
      expect(widget.enabled).toBe(false);
      expect(widget.enabledComputed).toBe(false);
      expect(widget.enabledGranted).toBe(false);
    });

    it('considers custom dimensions', () => {
      let widget = createWidget({
        parent: parent,
        enabled: {
          zzz: false
        }
      });
      expect(widget.enabled).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(true); // Must not be modified accidentally
      expect(widget.getPropertyDimension('enabled', 'zzz')).toBe(false); // Passed by the constructor
      expect(widget.getPropertyDimension('enabled', 'xyz')).toBe(true); // Dimension unknown so far, defaults to true

      widget.setPropertyDimension('enabled', 'zzz', true);
      expect(widget.enabled).toBe(true);
      expect(widget.getPropertyDimension('enabled', 'zzz')).toBe(true);

      widget.setPropertyDimension('enabled', 'xyz', false);
      expect(widget.enabled).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'xyz')).toBe(false);
    });

    it('supports initializing default value in constructor', () => {
      class SubWidget extends TestWidget {
        constructor() {
          super();
          this.enabled = false;
        }
      }

      let widget = scout.create(SubWidget, {
        parent: parent
      });
      expect(widget.enabled).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(false);

      let widget2 = scout.create(SubWidget, {
        parent: parent,
        enabled: true
      });
      expect(widget2.enabled).toBe(true);
      expect(widget2.getPropertyDimension('enabled', 'default')).toBe(true);

      let widget3 = scout.create(SubWidget, {
        parent: parent,
        'enabled-default': true
      });
      expect(widget3.enabled).toBe(true);
      expect(widget3.getPropertyDimension('enabled', 'default')).toBe(true);

      let widget4 = scout.create(SubWidget, {
        parent: parent,
        enabled: {
          default: true
        }
      });
      expect(widget4.enabled).toBe(true);
      expect(widget4.getPropertyDimension('enabled', 'default')).toBe(true);
    });
  });

  describe('rendering', () => {

    it('should set rendering, rendered flags correctly', () => {
      let widget = createWidget({
        parent: parent
      });
      expect(widget.rendered).toBe(false);
      expect(widget.rendering).toBe(false);
      widget.render(session.$entryPoint);
      expect(widget.rendered).toBe(true);
      expect(widget.rendering).toBe(false);
    });

    it('should set rendering flag to true _while_ the component is rendering', () => {
      let rendering;
      let widget = createWidget();
      widget._render = function() {
        rendering = this.rendering;
      };
      widget.init({
        parent: parent
      });
      widget.render(session.$entryPoint);
      expect(rendering).toBe(true);
    });

  });

  describe('clone', () => {
    let model, widget, expectedProperties = ['id', 'session', 'objectType', 'parent', 'text'];

    beforeEach(() => {
      model = createSimpleModel('Menu', session);
      model.label = 'bar';
      widget = scout.create(model);
      widget.$container = 'dummy container property';
    });

    it('clones only properties marked as clone property', () => {
      let widgetClone = widget.clone({
        parent: widget.parent
      });
      // should contain the following properties:
      expectedProperties.forEach(propertyName => {
        expect(widgetClone[propertyName]).not.toBe(undefined);
      });
      // but not the $container property (which has been added later)
      expect(widgetClone.$container).toBeNull();
    });

    it('\'text\' must be recognized as clone property, but not \'$container\'', () => {
      expect(widget.isCloneProperty('text')).toBe(true);
      expect(widget.isCloneProperty('$container')).toBe(false);
    });

    it('prefers properties passed as modelOverride', () => {
      let widgetClone = widget.clone({
        parent: widget.parent,
        text: 'foo'
      });
      expect(widgetClone.text).toBe('foo');
    });

    it('considers excludePropertiesToClone', () => {
      widget._addCloneProperties(['notExcluded', 'excluded']);
      widget.setProperty('notExcluded', 'foo');
      widget.setProperty('excluded', 'foo');

      const widgetClone = widget.clone(
        {
          parent: widget.parent
        }, {
          excludePropertiesToClone: ['excluded'],
          delegateAllPropertiesToClone: true
        }
      );

      expect(widgetClone.notExcluded).toBe('foo');
      expect(widgetClone.excluded).toBe('foo');

      widget.setProperty('notExcluded', 'bar');
      widget.setProperty('excluded', 'bar');

      expect(widgetClone.notExcluded).toBe('bar');
      expect(widgetClone.excluded).toBe('foo');
    });

    it('considers computed properties', () => {
      let widget = scout.create(ComputedWidget, {
        parent: session.desktop
      });
      let widgetClone = widget.clone({
        parent: widget.parent
      });
      expect(widget.getProperty('computed')).toBe(true);
      expect(widget._computed).toBe(true);
      expect(widgetClone.getProperty('computed')).toBe(true);
      expect(widgetClone._computed).toBe(true);
    });

    it('considers dimensional properties', () => {
      let widget = scout.create(Widget, {
        parent: session.desktop
      });
      widget.setEnabledGranted(false);
      widget.setPropertyDimension('enabled', 'xyz', false);
      expect(widget.enabled).toBe(false);
      expect(widget.enabledComputed).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(true);
      expect(widget.getPropertyDimension('enabled', 'granted')).toBe(false);
      expect(widget.getPropertyDimension('enabled', 'xyz')).toBe(false);

      let widgetClone = widget.clone({
        parent: widget.parent
      });
      expect(widgetClone.enabled).toBe(false);
      expect(widgetClone.enabledComputed).toBe(false);
      expect(widgetClone.getPropertyDimension('enabled', 'default')).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'granted')).toBe(false);
      expect(widgetClone.getPropertyDimension('enabled', 'xyz')).toBe(false);

      // Modifications on original dimensions must not be reflected to the clone unless explicitly requested (see next spec)
      widget.setEnabledGranted(true);
      widget.setPropertyDimension('enabled', 'xyz', true);
      expect(widgetClone.enabled).toBe(false);
      expect(widgetClone.enabledComputed).toBe(false);
      expect(widgetClone.getPropertyDimension('enabled', 'default')).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'granted')).toBe(false);
      expect(widgetClone.getPropertyDimension('enabled', 'xyz')).toBe(false);
    });

    it('can mirror dimensional properties', () => {
      let widget = scout.create(Widget, {
        parent: session.desktop
      });
      widget.setEnabledGranted(false);
      widget.setPropertyDimension('enabled', 'xyz', false);

      let widgetClone = widget.clone({parent: widget.parent}, {delegateAllPropertiesToClone: true});
      widget.setEnabledGranted(true);
      widget.setPropertyDimension('enabled', 'xyz', true);
      expect(widgetClone.enabled).toBe(true);
      expect(widgetClone.enabledComputed).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'default')).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'granted')).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'xyz')).toBe(true);
    });

    it('can mirror individual dimensional properties', () => {
      let widget = scout.create(Widget, {
        parent: session.desktop
      });
      widget.setEnabledGranted(false);
      widget.setPropertyDimension('enabled', 'xyz', false);

      let widgetClone = widget.clone({parent: widget.parent}, {delegatePropertiesToClone: ['enabled']});
      widget.setEnabledGranted(true);
      widget.setPropertyDimension('enabled', 'xyz', true);
      expect(widgetClone.enabled).toBe(true);
      expect(widgetClone.enabledComputed).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'default')).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'granted')).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'xyz')).toBe(true);
    });

    it('can mirror individual property dimensions', () => {
      let widget = scout.create(Widget, {
        parent: session.desktop
      });
      widget.setEnabledGranted(false);
      widget.setPropertyDimension('enabled', 'xyz', true);
      widget.setPropertyDimension('enabled', 'zzz', false);

      let widgetClone = widget.clone({parent: widget.parent}, {delegatePropertiesToClone: ['enabledGranted', 'enabled-zzz']}); // enabledGranted is an alias
      widget.setEnabledGranted(true);
      widget.setPropertyDimension('enabled', 'zzz', true);
      widget.setPropertyDimension('enabled', 'xyz', false); // not mirrored
      expect(widgetClone.enabled).toBe(true);
      expect(widgetClone.enabledComputed).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'default')).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'granted')).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'xyz')).toBe(true);
      expect(widgetClone.getPropertyDimension('enabled', 'zzz')).toBe(true);
    });

    it('does not mirror if individual dimensional properties are excluded', () => {
      let widget = scout.create(Widget, {
        parent: session.desktop
      });
      widget.setEnabledGranted(false);
      widget.setPropertyDimension('enabled', 'xyz', false);

      let widgetClone = widget.clone({parent: widget.parent}, {delegateAllPropertiesToOriginal: true, excludePropertiesToOriginal: ['enabled']});
      widgetClone.setEnabledGranted(true);
      widgetClone.setPropertyDimension('enabled', 'xyz', true);
      expect(widget.enabled).toBe(true);
      expect(widget.enabledComputed).toBe(true);
      expect(widget.getPropertyDimension('enabled', 'default')).toBe(true);
      expect(widget.getPropertyDimension('enabled', 'granted')).toBe(true);
      expect(widget.getPropertyDimension('enabled', 'xyz')).toBe(true);
    });
  });

  describe('init', () => {

    it('links widget properties with the widget', () => {
      let child = createWidget({
        parent: parent
      });
      let widget = createWidget({
        parent: parent,
        childWidget: child
      });

      expect(child.parent).toBe(widget);
      expect(child.owner).toBe(parent);
    });

  });

  describe('destroy', () => {
    it('destroys the widget', () => {
      let widget = createWidget({
        parent: parent
      });
      expect(widget.destroyed).toBe(false);

      widget.destroy();
      expect(widget.destroyed).toBe(true);
    });

    it('destroys the children', () => {
      let widget = createWidget({
        parent: parent
      });
      let child0 = createWidget({
        parent: widget
      });
      let child1 = createWidget({
        parent: widget
      });
      expect(widget.destroyed).toBe(false);
      expect(child0.destroyed).toBe(false);
      expect(child1.destroyed).toBe(false);

      widget.destroy();
      expect(widget.destroyed).toBe(true);
      expect(child0.destroyed).toBe(true);
      expect(child1.destroyed).toBe(true);
    });

    it('does only destroy children if the parent is the owner', () => {
      let widget = createWidget({
        parent: parent
      });
      let another = createWidget({
        parent: parent
      });
      let child0 = createWidget({
        parent: widget,
        owner: another
      });
      let child1 = createWidget({
        parent: widget
      });
      expect(widget.destroyed).toBe(false);
      expect(another.destroyed).toBe(false);
      expect(child0.destroyed).toBe(false);
      expect(child1.destroyed).toBe(false);

      widget.destroy();
      expect(widget.destroyed).toBe(true);
      expect(another.destroyed).toBe(false);
      expect(child0.destroyed).toBe(false);
      expect(child1.destroyed).toBe(true);

      another.destroy();
      expect(another.destroyed).toBe(true);
      expect(child0.destroyed).toBe(true);
    });

    it('removes the link to parent and owner', () => {
      let widget = createWidget({
        parent: parent
      });
      expect(widget.parent).toBe(parent);
      expect(widget.owner).toBe(parent);
      expect(parent.children[0]).toBe(widget);

      widget.destroy();
      expect(widget.parent).toBe(null);
      expect(widget.owner).toBe(null);
      expect(parent.children.length).toBe(0);
    });

    it('reconnects to owner if parent is destroyed', () => {
      let widget = createWidget({
        parent: parent
      });
      let another = createWidget({
        parent: parent
      });
      let child = createWidget({
        parent: widget,
        owner: another
      });
      expect(child.parent).toBe(widget);
      expect(child.owner).toBe(another);
      expect(widget.children[0]).toBe(child);

      widget.destroy();
      expect(child.parent).toBe(another);
      expect(child.owner).toBe(another);
      expect(widget.children.length).toBe(0);
    });
  });

  describe('setParent', () => {
    it('links the widget with the new parent', () => {
      let widget = createWidget({
        parent: parent
      });
      let another = createWidget({
        parent: parent
      });
      expect(widget.parent).toBe(parent);
      expect(another.parent).toBe(parent);

      another.setParent(widget);
      expect(widget.parent).toBe(parent);
      expect(another.parent).toBe(widget);
    });

    it('removes the widget from the old parent if the old is not the owner', () => {
      let widget = createWidget({
        parent: parent
      });
      let widget2 = createWidget({
        parent: parent
      });
      let child = createWidget({
        parent: widget,
        owner: widget2
      });
      expect(child.parent).toBe(widget);
      expect(child.owner).toBe(widget2);
      expect(widget.children[0]).toBe(child);
      expect(widget.children.length).toBe(1);
      expect(widget2.children[0]).toBe(child);
      expect(widget2.children.length).toBe(1);

      child.setParent(widget2);
      expect(child.parent).toBe(widget2);
      expect(child.owner).toBe(widget2);
      expect(widget2.children[0]).toBe(child);
      expect(widget2.children.length).toBe(1);
      expect(widget.children.length).toBe(0);
    });

    it('does not remove the widget from the old parent if the old is the owner', () => {
      let widget = createWidget({
        parent: parent
      });
      let widget2 = createWidget({
        parent: parent
      });
      let child = createWidget({
        parent: widget,
        owner: widget
      });
      expect(child.parent).toBe(widget);
      expect(child.owner).toBe(widget);
      expect(widget.children[0]).toBe(child);
      expect(widget.children.length).toBe(1);
      expect(widget2.children.length).toBe(0);

      child.setParent(widget2);
      expect(child.parent).toBe(widget2);
      expect(child.owner).toBe(widget);
      expect(widget2.children[0]).toBe(child);
      expect(widget2.children.length).toBe(1);
      expect(widget.children[0]).toBe(child);
      expect(widget.children.length).toBe(1);
    });

    it('relinks parent destroy listener to the new parent', () => {
      let widget = createWidget({
        parent: parent
      });
      let another = createWidget({
        parent: parent
      });
      expect(widget.parent).toBe(parent);
      expect(another.parent).toBe(parent);

      let widgetListenerCount = widget.events._eventListeners.length;
      let parentListenerCount = parent.events._eventListeners.length;
      another.setParent(widget);
      expect(parent.events._eventListeners.length).toBe(parentListenerCount - 1);
      expect(widget.events._eventListeners.length).toBe(widgetListenerCount + 1);

      another.setParent(parent);
      expect(parent.events._eventListeners.length).toBe(parentListenerCount);
      expect(widget.events._eventListeners.length).toBe(widgetListenerCount);

      // Ensure parent destroy listener is removed on destroy
      another.destroy();
      expect(parent.events._eventListeners.length).toBe(parentListenerCount - 1);
    });

    it('triggers hierarchyChange event when parent changes', () => {
      let widget = createWidget({
        parent: parent
      });
      let newParent = createWidget({
        parent: parent
      });
      let event = null;
      widget.on('hierarchyChange', event0 => {
        event = event0;
      });
      widget.setParent(newParent);
      expect(event.oldParent).toBe(parent);
      expect(event.parent).toBe(newParent);
    });
  });

  describe('setOwner', () => {

    it('removes the widget from the old owner if the old is not the parent', () => {
      let widget = createWidget({
        parent: parent
      });
      let widget2 = createWidget({
        parent: parent
      });
      let child = createWidget({
        parent: widget2,
        owner: widget
      });
      expect(child.parent).toBe(widget2);
      expect(child.owner).toBe(widget);
      expect(widget.children[0]).toBe(child);
      expect(widget.children.length).toBe(1);
      expect(widget2.children[0]).toBe(child);
      expect(widget2.children.length).toBe(1);

      child.setOwner(widget2);
      expect(child.parent).toBe(widget2);
      expect(child.owner).toBe(widget2);
      expect(widget2.children[0]).toBe(child);
      expect(widget2.children.length).toBe(1);
      expect(widget.children.length).toBe(0);
    });

    it('does not remove the widget from the old owner if the old is the parent', () => {
      let widget = createWidget({
        parent: parent
      });
      let widget2 = createWidget({
        parent: parent
      });
      let child = createWidget({
        parent: widget,
        owner: widget
      });
      expect(child.parent).toBe(widget);
      expect(child.owner).toBe(widget);
      expect(widget.children[0]).toBe(child);
      expect(widget.children.length).toBe(1);
      expect(widget2.children.length).toBe(0);

      child.setOwner(widget2);
      expect(child.parent).toBe(widget);
      expect(child.owner).toBe(widget2);
      expect(widget2.children[0]).toBe(child);
      expect(widget2.children.length).toBe(1);
      expect(widget.children[0]).toBe(child);
      expect(widget.children.length).toBe(1);
    });
  });

  describe('remove', () => {
    it('removes the widget', () => {
      let widget = createWidget({
        parent: parent
      });
      widget.render(session.$entryPoint);
      expect(widget.rendered).toBe(true);
      expect(widget.$container).toBeDefined();

      widget.remove();
      expect(widget.rendered).toBe(false);
      expect(widget.$container).toBe(null);
    });

    it('removes the children', () => {
      let widget = createWidget({
        parent: parent
      });
      let child0 = createWidget({
        parent: widget
      });
      let child0_0 = createWidget({
        parent: child0
      });
      widget.render(session.$entryPoint);
      child0.render(widget.$container);
      child0_0.render(child0.$container);
      expect(widget.rendered).toBe(true);
      expect(child0.rendered).toBe(true);
      expect(child0_0.rendered).toBe(true);

      widget.remove();
      expect(widget.rendered).toBe(false);
      expect(child0.rendered).toBe(false);
      expect(child0_0.rendered).toBe(false);
    });

    it('does not remove the children if owner is removed but parent is still rendered', () => {
      let widget = createWidget({
        parent: parent
      });
      let child0 = createWidget({
        parent: widget
      });
      let owner = createWidget({
        parent: new TestWidget()
      });
      let anotherChild = createWidget({
        parent: widget,
        owner: owner
      });
      widget.render(session.$entryPoint);
      owner.render(session.$entryPoint);
      child0.render(widget.$container);
      anotherChild.render(widget.$container);
      expect(anotherChild.parent).toBe(widget);
      expect(anotherChild.owner).toBe(owner);

      owner.remove();
      expect(owner.rendered).toBe(false);
      expect(anotherChild.rendered).toBe(true);
      expect(widget.rendered).toBe(true);
      expect(child0.rendered).toBe(true);

      // If the owner is destroyed, the widget has to be removed even if another widget is currently the parent
      // Otherwise the widget would be in an inconsistent state (destroyed, but still rendered)
      owner.destroy();
      expect(owner.rendered).toBe(false);
      expect(owner.destroyed).toBe(true);
      expect(anotherChild.rendered).toBe(false);
      expect(anotherChild.destroyed).toBe(true);
      expect(widget.rendered).toBe(true);
      expect(child0.rendered).toBe(true);
    });

    it('removes the widget if removing is animated but parent is removed while animation is running', () => {
      let removeOrder = [];
      let parentWidget = createWidget({
        parent: parent
      });
      let origRemove = parentWidget._remove;
      parentWidget._remove = function() {
        origRemove.call(this);
        removeOrder.push('parent');
      };
      let widget = createWidget({
        parent: parentWidget,
        animateRemoval: true
      });
      origRemove = widget._remove;
      widget._remove = function() {
        origRemove.call(this);
        removeOrder.push('child');
      };
      parentWidget.render(session.$entryPoint);
      widget.render();
      expect(widget._rendered).toBe(true);
      expect(widget.$container).toBeDefined();

      widget.remove();
      expect(widget._rendered).toBe(true);
      expect(widget.$container).toBeDefined();
      expect(widget.removalPending).toBe(true);

      // Even though animation has not run the widget needs to be removed because parent is removed
      parentWidget.remove();
      expect(parentWidget._rendered).toBe(false);
      expect(widget._rendered).toBe(false);
      expect(widget.$container).toBe(null);
      expect(widget.removalPending).toBe(false);

      // Expect that child is removed before parent
      expect(removeOrder).toEqual(['child', 'parent']);
    });

    it('removes after the animation if removing is animated', done => {
      appendAnimateRemoveStyle();
      let widget = createWidget({
        parent: parent,
        animateRemoval: true
      });
      widget.render(session.$entryPoint);

      widget.remove();
      expect(widget._rendered).toBe(true);
      expect(widget.$container).toBeDefined();
      expect(widget.removalPending).toBe(true);

      widget.one('remove', () => {
        expect(widget._rendered).toBe(false);
        expect(widget.$container).toBe(null);
        expect(widget.removalPending).toBe(false);
        done();
      });
    });

    it('removes the widget immediately if removing is animated but parent is being removed', () => {
      let parentWidget = createWidget({
        parent: parent
      });
      let widget = createWidget({
        parent: parentWidget,
        animateRemoval: true
      });
      parentWidget.render(session.$entryPoint);
      widget.render();
      expect(widget._rendered).toBe(true);
      expect(widget.$container).toBeDefined();

      parentWidget.remove();
      expect(parentWidget._rendered).toBe(false);
      expect(widget._rendered).toBe(false);
      expect(widget.$container).toBe(null);
      expect(widget.removalPending).toBe(false);
    });
  });

  describe('setProperty', () => {

    describe('with widget property', () => {
      it('links the widget with the new child widget', () => {
        let widget = createWidget({
          parent: parent
        });
        let another = createWidget({
          parent: parent
        });
        let child = createWidget({
          parent: parent
        });

        widget.setChildWidget(child);
        expect(child.parent).toBe(widget);
        expect(child.owner).toBe(parent);

        another.setChildWidget(child);
        expect(child.parent).toBe(another);
        expect(child.owner).toBe(parent);
      });

      it('links the widget with the new child widgets if it is an array', () => {
        let widget = createWidget({
          parent: parent
        });
        let another = createWidget({
          parent: parent
        });
        let children = [
          createWidget({
            parent: parent
          }),
          createWidget({
            parent: parent
          })
        ];

        widget.setChildWidget(children);
        expect(children[0].parent).toBe(widget);
        expect(children[0].owner).toBe(parent);
        expect(children[1].parent).toBe(widget);
        expect(children[1].owner).toBe(parent);

        another.setChildWidget(children);
        expect(children[0].parent).toBe(another);
        expect(children[0].owner).toBe(parent);
        expect(children[1].parent).toBe(another);
        expect(children[1].owner).toBe(parent);
      });

      it('does not fail if new widget is null', () => {
        let widget = createWidget({
          parent: parent
        });
        let child = createWidget({
          parent: parent
        });

        widget.setChildWidget(child);
        widget.setChildWidget(null);
        expect().nothing();
      });

      it('destroys the child when setting to null if the widget is the owner', () => {
        let widget = createWidget({
          parent: parent
        });
        let child = createWidget({
          parent: widget
        });

        widget.setChildWidget(child);
        expect(child.parent).toBe(widget);
        expect(child.owner).toBe(widget);

        widget.setChildWidget(null);
        expect(child.destroyed).toBe(true);
        expect(child.parent).toBe(null);
        expect(child.owner).toBe(null);
      });

      it('disconnects the from the old parent when setting to null if it was not the owner', () => {
        let widget = createWidget({
          parent: parent
        });
        let another = createWidget({
          parent: parent
        });
        let child = createWidget({
          parent: parent,
          owner: another
        });

        widget.setChildWidget(child);
        expect(child.parent).toBe(widget);
        expect(child.owner).toBe(another);

        widget.setChildWidget(null);
        expect(child.destroyed).toBe(false);
        expect(child.parent).toBe(another);
        expect(child.owner).toBe(another);
      });
    });

    it('calls the _render* method if there is one for this property', () => {
      let widget = createWidget({
        parent: parent
      });
      widget.render(session.$entryPoint);

      // Must not fail, render is optional
      widget.setProperty('foo', 'xyz');

      // Add render method and set property again
      // @ts-expect-error
      widget._renderFoo = function() {
        this.$container.text(this.foo);
      };
      widget.setProperty('foo', 'bar');
      expect(widget.$container.text()).toBe('bar');
    });
  });

  describe('on', () => {
    it('supports propertyChange:propertyName', () => {
      let type1ExecCount = 0;
      let type1Type = null;
      let type2ExecCount = 0;
      let type2Type = null;
      let noTypeExecCount = 0;
      let noTypeType = null;
      let widget = createWidget({
        parent: parent
      });
      widget.on('propertyChange:type1', event => {
        type1ExecCount++;
        type1Type = event['propertyName'];
      });
      widget.on('propertyChange:type2', event => {
        type2ExecCount++;
        type2Type = event['propertyName'];
      });
      widget.on('propertyChange', event => {
        noTypeExecCount++;
        noTypeType = event.propertyName;
      });
      widget.triggerPropertyChange('type1', 'old', 'new');
      expect(type1ExecCount).toBe(1);
      expect(type1Type).toBe('type1');
      expect(type2ExecCount).toBe(0);
      expect(noTypeExecCount).toBe(1);
      expect(noTypeType).toBe('type1');

      type1ExecCount = 0;
      type1Type = null;
      type2ExecCount = 0;
      type2Type = null;
      noTypeExecCount = 0;
      noTypeType = null;
      widget.triggerPropertyChange('type2', 'old', 'new');
      expect(type1ExecCount).toBe(0);
      expect(type2ExecCount).toBe(1);
      expect(type2Type).toBe('type2');
      expect(noTypeExecCount).toBe(1);
      expect(noTypeType).toBe('type2');

      type1ExecCount = 0;
      type1Type = null;
      type2ExecCount = 0;
      type2Type = null;
      noTypeExecCount = 0;
      noTypeType = null;
      widget.triggerPropertyChange('abc', 'old', 'new');
      expect(type1ExecCount).toBe(0);
      expect(type2ExecCount).toBe(0);
      expect(noTypeExecCount).toBe(1);
      expect(noTypeType).toBe('abc');
    });
  });

  describe('one', () => {
    it('supports propertyChange:propertyName', () => {
      let type1ExecCount = 0;
      let type1Type = null;
      let type2ExecCount = 0;
      let type2Type = null;
      let noTypeExecCount = 0;
      let noTypeType = null;
      let widget = createWidget({
        parent: parent
      });
      widget.one('propertyChange:type1', event => {
        type1ExecCount++;
        type1Type = event['propertyName'];
      });
      widget.one('propertyChange:type2', event => {
        type2ExecCount++;
        type2Type = event['propertyName'];

      });
      widget.one('propertyChange', event => {
        noTypeExecCount++;
        noTypeType = event.propertyName;
      });
      widget.triggerPropertyChange('type1', 'old', 'new');
      expect(type1ExecCount).toBe(1);
      expect(type1Type).toBe('type1');
      expect(type2ExecCount).toBe(0);
      expect(noTypeExecCount).toBe(1);
      expect(noTypeType).toBe('type1');

      // Do the same -> no handler must be executed
      widget.triggerPropertyChange('type1', 'old', 'new');
      expect(type1ExecCount).toBe(1);
      expect(type2ExecCount).toBe(0);
      expect(noTypeExecCount).toBe(1);

      // Trigger type2 -> since it was not executed yet, it will be executed now
      widget.triggerPropertyChange('type2', 'old', 'new');
      expect(type1ExecCount).toBe(1);
      expect(type2ExecCount).toBe(1);
      expect(noTypeExecCount).toBe(1);
    });
  });

  describe('off', () => {
    it('supports propertyChange:propertyName', () => {
      let type1ExecCount = 0;
      let type2ExecCount = 0;
      let noTypeExecCount = 0;
      let widget = createWidget({
        parent: parent
      });
      let func1 = event => {
        type1ExecCount++;
      };
      widget.on('propertyChange:type1', func1);
      let func2 = event => {
        type2ExecCount++;
      };
      widget.on('propertyChange:type2', func2);
      let noFunc = event => {
        noTypeExecCount++;
      };
      widget.on('propertyChange', noFunc);
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      expect(type1ExecCount).toBe(1);
      expect(type2ExecCount).toBe(1);
      expect(noTypeExecCount).toBe(2);

      widget.off('propertyChange:type1', func1);
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      expect(type1ExecCount).toBe(1);
      expect(type2ExecCount).toBe(2);
      expect(noTypeExecCount).toBe(4);

      widget.off('propertyChange', noFunc);
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      expect(type1ExecCount).toBe(1);
      expect(type2ExecCount).toBe(3);
      expect(noTypeExecCount).toBe(4);

      widget.off('propertyChange:type2', func2);
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      expect(type1ExecCount).toBe(1);
      expect(type2ExecCount).toBe(3);
      expect(noTypeExecCount).toBe(4);
    });

    it('supports propertyChange:propertyName also when only using type to detach', () => {
      let type1ExecCount = 0;
      let type2ExecCount = 0;
      let noTypeExecCount = 0;
      let widget = createWidget({
        parent: parent
      });
      let func1 = event => {
        type1ExecCount++;
      };
      let func2 = event => {
        type2ExecCount++;
      };
      let noFunc = event => {
        noTypeExecCount++;
      };
      widget.on('propertyChange:type1', func1);
      widget.on('propertyChange:type2', func2);
      widget.on('propertyChange:type2a', func2);
      widget.on('propertyChange', noFunc);
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      widget.triggerPropertyChange('type2a', 'old', 'new');
      expect(type1ExecCount).toBe(1);
      expect(type2ExecCount).toBe(2);
      expect(noTypeExecCount).toBe(3);

      widget.off('propertyChange');
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      widget.triggerPropertyChange('type2a', 'old', 'new');
      expect(type1ExecCount).toBe(2);
      expect(type2ExecCount).toBe(4);
      expect(noTypeExecCount).toBe(3);

      widget.off('propertyChange:type1');
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      widget.triggerPropertyChange('type2a', 'old', 'new');
      expect(type1ExecCount).toBe(2);
      expect(type2ExecCount).toBe(6);
      expect(noTypeExecCount).toBe(3);

      widget.off('propertyChange:type2');
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      widget.triggerPropertyChange('type2a', 'old', 'new');
      expect(type1ExecCount).toBe(2);
      expect(type2ExecCount).toBe(7);
      expect(noTypeExecCount).toBe(3);
    });

    it('supports propertyChange:propertyName also when only using func to detach', () => {
      let type1ExecCount = 0;
      let type2ExecCount = 0;
      let noTypeExecCount = 0;
      let widget = createWidget({
        parent: parent
      });
      let func1 = event => {
        type1ExecCount++;
      };
      let func2 = event => {
        type2ExecCount++;
      };
      let noFunc = event => {
        noTypeExecCount++;
      };
      widget.on('propertyChange:type1', func1);
      widget.on('propertyChange:type2', func2);
      widget.on('propertyChange:type2a', func2);
      widget.on('propertyChange', noFunc);
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      widget.triggerPropertyChange('type2a', 'old', 'new');
      expect(type1ExecCount).toBe(1);
      expect(type2ExecCount).toBe(2);
      expect(noTypeExecCount).toBe(3);

      widget.off(null, noFunc);
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      widget.triggerPropertyChange('type2a', 'old', 'new');
      expect(type1ExecCount).toBe(2);
      expect(type2ExecCount).toBe(4);
      expect(noTypeExecCount).toBe(3);

      widget.off(null, func1);
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      widget.triggerPropertyChange('type2a', 'old', 'new');
      expect(type1ExecCount).toBe(2);
      expect(type2ExecCount).toBe(6);
      expect(noTypeExecCount).toBe(3);

      widget.off(null, func2);
      widget.triggerPropertyChange('type1', 'old', 'new');
      widget.triggerPropertyChange('type2', 'old', 'new');
      widget.triggerPropertyChange('type2a', 'old', 'new');
      expect(type1ExecCount).toBe(2);
      expect(type2ExecCount).toBe(6);
      expect(noTypeExecCount).toBe(3);
    });
  });

  describe('property css class', () => {

    it('adds or removes custom css class', () => {
      let widget = createWidget({
        parent: parent
      });
      widget.render(session.$entryPoint);

      widget.setCssClass('custom-class');
      expect(widget.$container).toHaveClass('custom-class');

      widget.setCssClass('');
      expect(widget.$container).not.toHaveClass('custom-class');
    });

    it('does not accidentally remove other css classes on a property change', () => {
      let widget = createWidget({
        parent: parent
      });
      widget.render(session.$entryPoint);
      widget.$container.addClass('xy');
      expect(widget.$container).toHaveClass('xy');

      widget.setCssClass('custom-class');
      expect(widget.$container).toHaveClass('custom-class');
      expect(widget.$container).toHaveClass('xy');

      widget.setCssClass('');
      expect(widget.$container).not.toHaveClass('custom-class');
      expect(widget.$container).toHaveClass('xy');
    });

    describe('addCssClass', () => {

      it('adds the cssClass to the existing classes', () => {
        let widget = createWidget({
          parent: parent
        });
        widget.render(session.$entryPoint);
        widget.addCssClass('custom-class');
        expect(widget.$container).toHaveClass('custom-class');

        widget.addCssClass('another-class1 another-class2');
        expect(widget.cssClass).toBe('custom-class another-class1 another-class2');
        expect(widget.$container).toHaveClass('custom-class');
        expect(widget.$container).toHaveClass('another-class1');
        expect(widget.$container).toHaveClass('another-class2');
      });

      it('does not add the same class multiple times', () => {
        let widget = createWidget({
          parent: parent
        });
        widget.render(session.$entryPoint);
        widget.addCssClass('custom-class');
        expect(widget.cssClass).toBe('custom-class');

        widget.addCssClass('custom-class');
        expect(widget.cssClass).toBe('custom-class');

        widget.addCssClass('custom-class custom-class');
        expect(widget.cssClass).toBe('custom-class');
      });

    });

    describe('removeCssClass', () => {

      it('removes the cssClass from the existing classes', () => {
        let widget = createWidget({
          parent: parent
        });
        widget.render(session.$entryPoint);
        widget.setCssClass('cls1 cls2 cls3');
        expect(widget.$container).toHaveClass('cls1');
        expect(widget.$container).toHaveClass('cls2');
        expect(widget.$container).toHaveClass('cls3');

        widget.removeCssClass('cls2');
        expect(widget.cssClass).toBe('cls1 cls3');
        expect(widget.$container).toHaveClass('cls1');
        expect(widget.$container).not.toHaveClass('cls2');
        expect(widget.$container).toHaveClass('cls3');

        widget.removeCssClass('cls1 cls3');
        expect(widget.cssClass).toBe('');
      });

    });

    describe('toggleCssClass', () => {

      it('toggles the cssClass based on a predicate', () => {
        let widget = createWidget({
          parent: parent
        });
        widget.render(session.$entryPoint);
        widget.setCssClass('cls1 cls2 cls3');
        expect(widget.$container).toHaveClass('cls1');
        expect(widget.$container).toHaveClass('cls2');
        expect(widget.$container).toHaveClass('cls3');

        widget.toggleCssClass('cls2', false);
        expect(widget.cssClass).toBe('cls1 cls3');
        expect(widget.$container).toHaveClass('cls1');
        expect(widget.$container).not.toHaveClass('cls2');
        expect(widget.$container).toHaveClass('cls3');

        widget.toggleCssClass('cls2', true);
        expect(widget.cssClass).toBe('cls1 cls3 cls2');
        expect(widget.$container).toHaveClass('cls1');
        expect(widget.$container).toHaveClass('cls2');
        expect(widget.$container).toHaveClass('cls3');
      });

    });
  });

  describe('focus', () => {

    it('sets the focus on the container', () => {
      let widget = createWidget({
        parent: parent
      });
      widget.render(session.$entryPoint);
      widget.focus();
      expect(document.activeElement).toBe(widget.$container[0]);
    });

    it('schedules the focus request if the widget is not rendered', () => {
      let widget = createWidget({
        parent: parent
      });
      widget.focus();
      widget.render(session.$entryPoint);
      widget.validateLayoutTree(); // <-- this triggers the focus to be set
      expect(document.activeElement).toBe(widget.$container[0]);
    });
  });

  describe('prepareModel', () => {
    it('default case', () => {
      let model = {};

      class TestClass extends Widget {
        override _init(model0: InitModelOf<this>) {
          // @ts-expect-error
          expect(model0).toBe(model);
        }

        override _prepareModel(model: InitModelOf<this>): InitModelOf<this> {
          return super._prepareModel(model);
        }
      }

      let widget = new TestClass();

      let _prepareModelSpy = spyOn(widget, '_prepareModel').and.callThrough();
      // @ts-expect-error
      widget.init(model);
      expect(_prepareModelSpy.calls.count()).toBe(1);
    });

    it('changes the model before _init', () => {
      class TestClass extends Widget {
        message: string;

        override _prepareModel(model) {
          model.message = 'B';
          return model;
        }
      }

      let widget = new TestClass();
      widget.init({
        parent: parent,
        message: 'A'
      });

      expect(widget.message).toBe('B');
    });
  });

  describe('Widget properties', () => {

    it('automatically resolves referenced widgets', () => {
      class ComplexTestWidget extends Widget {
        items: TestItem[];
        selectedItem: TestItem;

        constructor() {
          super();
          this._addWidgetProperties(['items', 'selectedItem']);
          this._addPreserveOnPropertyChangeProperties(['selectedItem']);
        }
      }

      class TestItem extends Widget {
        name: string;
        linkedItem: Widget;

        constructor() {
          super();
          this._addWidgetProperties(['linkedItem']);
        }
      }

      ObjectFactory.get().registerNamespace('testns', {ComplexTestWidget, TestItem});

      // Create an instance
      let model1 = {
        parent: parent,
        items: [{
          objectType: TestItem,
          id: 'TI1',
          name: 'Item #1'
        }, {
          objectType: TestItem,
          id: 'TI2',
          name: 'Item #2'
        }],
        selectedItem: 'TI2'
      };
      let ctw1 = scout.create(ComplexTestWidget, model1);
      expect(ctw1.items.length).toBe(2);
      expect(ctw1.items[1].name).toBe('Item #2');
      expect(ctw1.selectedItem).toBe(ctw1.items[1]);

      // Create another instance with an invalid reference
      let model2 = {
        parent: parent,
        objectType: ComplexTestWidget,
        items: [{
          objectType: TestItem,
          id: 'TI1',
          name: 'Item #1'
        }],
        selectedItem: 'TI77'
      };
      expect(() => {
        scout.create(model2);
      }).toThrow(new Error('Referenced widget not found: TI77'));
      // fix it
      delete model2.selectedItem;
      let ctw2 = scout.create(model2) as ComplexTestWidget;
      expect(ctw2['items'].length).toBe(1);
      expect(ctw2['items'][0].name).toBe('Item #1');
      expect(ctw2['items'][0]).not.toBe(ctw1.items[0]); // not same!

      // Create another instance with unsupported references (same level)
      let model3 = {
        parent: parent,
        items: [{
          objectType: TestItem,
          id: 'TI1',
          name: 'Item #1',
          linkedItem: 'TI2'
        }, {
          objectType: TestItem,
          id: 'TI2',
          name: 'Item #2'
        }]
      };
      expect(() => {
        scout.create(ComplexTestWidget, model3);
      }).toThrow(new Error('Referenced widget not found: TI2'));
      // fix it
      delete model3.items[0].linkedItem;
      let ctw3 = scout.create(ComplexTestWidget, model3);
      ctw3.items[0].setProperty('linkedItem', ctw3.items[1]);
    });
  });

  describe('scrollTop', () => {
    beforeEach(() => {
      jasmine.clock().install();
    });

    afterEach(() => {
      jasmine.clock().uninstall();
    });

    it('is stored on scroll if scrollbars are installed', () => {
      let widget = new ScrollableWidget();
      widget.init({
        parent: parent,
        session: session
      });
      widget.render(session.$entryPoint);
      widget.$container[0].scrollTop = 40;
      widget.$container.trigger('scroll'); // Is executed later, trigger manually for testing
      expect(widget.scrollTop).toBe(40);
    });

    it('is not stored on scroll if scrollbars are not installed', () => {
      let widget = createWidget({
        parent: parent
      });
      expect(widget.scrollTop).toBe(null);

      widget.render(session.$entryPoint);
      widget.$container[0].scrollTop = 40;
      expect(widget.$container[0].scrollTop).toBe(0);
      widget.$container.trigger('scroll');
      expect(widget.scrollTop).toBe(null);
    });

    it('is applied again on render after remove', () => {
      let widget = new ScrollableWidget();
      widget.init({
        parent: parent,
        session: session
      });
      widget.render(session.$entryPoint);
      expect(widget.$container[0].scrollTop).toBe(0);
      expect(widget.$container[0].scrollHeight).toBe(100);

      widget.$container[0].scrollTop = 40;
      widget.$container.trigger('scroll');
      jasmine.clock().tick(500);
      expect(widget.scrollTop).toBe(40);

      widget.remove();
      widget.render(session.$entryPoint);
      expect(widget.scrollTop).toBe(40);
      expect(widget.$container[0].scrollTop).toBe(0);
      widget.revalidateLayoutTree(); // Scroll top will be rendered after the layout
      expect(widget.$container[0].scrollTop).toBe(40);
    });

    it('is set to null if scrollbars are not installed', () => {
      let widget = createWidget({
        parent: parent
      });
      expect(widget.scrollTop).toBe(null);

      let _renderScrollTopSpy = spyOn(widget, '_renderScrollTop').and.callThrough();
      widget.render(session.$entryPoint);
      expect(_renderScrollTopSpy.calls.count()).toBe(1);
      widget.revalidateLayoutTree(); // Scroll top will be rendered after the layout
      expect(_renderScrollTopSpy.calls.count()).toBe(1); // Must not be executed again for non-scrollable widgets
    });

    it('is set to null if scrollbars are uninstalled on the fly', () => {
      let widget = new ScrollableWidget();
      widget.init({
        parent: parent,
        scrollTop: 40
      });
      expect(widget.scrollTop).toBe(40);

      let _renderScrollTopSpy = spyOn(widget, '_renderScrollTop').and.callThrough();
      widget.render(session.$entryPoint);
      expect(_renderScrollTopSpy.calls.count()).toBe(1);
      widget.revalidateLayoutTree(); // Scroll top will be rendered after the layout
      expect(_renderScrollTopSpy.calls.count()).toBe(2); // Is executed again after layout
      expect(widget.$container[0].scrollTop).toBe(40);

      widget._uninstallScrollbars();
      expect(widget.scrollTop).toBe(null);

      widget.remove();
      widget.render(session.$entryPoint);
      expect(_renderScrollTopSpy.calls.count()).toBe(3);
      widget.revalidateLayoutTree();
      expect(_renderScrollTopSpy.calls.count()).toBe(3); // Must not be executed again
      expect(widget.$container[0].scrollTop).toBe(0);
    });
  });

  describe('isEveryParentVisible', () => {

    let parentWidget1, parentWidget2, parentWidget3, testWidget;

    beforeEach(() => {
      parentWidget1 = createWidget();

      parentWidget2 = createWidget({
        parent: parentWidget1
      });

      parentWidget3 = createWidget({
        parent: parentWidget2
      });

      testWidget = createWidget({
        parent: parentWidget3
      });
    });

    it('should correctly calculate the parents visible state if all parents are visible', () => {
      expect(testWidget.isEveryParentVisible()).toBe(true);
    });

    it('should correctly calculate the parents visible state if one parent is invisible', () => {
      parentWidget1.setVisible(false);

      expect(testWidget.isEveryParentVisible()).toBe(false);

      parentWidget1.setVisible(true);
      parentWidget2.setVisible(false);

      expect(testWidget.isEveryParentVisible()).toBe(false);

      parentWidget2.setVisible(true);
      parentWidget3.setVisible(false);

      expect(testWidget.isEveryParentVisible()).toBe(false);
    });

    it('should correctly calculate the parents visible state if several parents are invisible', () => {
      parentWidget1.setVisible(false);
      parentWidget2.setVisible(false);

      // parent 1 and 2 are invisible
      expect(testWidget.isEveryParentVisible()).toBe(false);

      parentWidget2.setVisible(true);
      parentWidget3.setVisible(false);

      // parent 1 and 3 are invisible
      expect(testWidget.isEveryParentVisible()).toBe(false);

      parentWidget1.setVisible(true);
      parentWidget2.setVisible(false);

      // parent 2 and 3 are invisible
      expect(testWidget.isEveryParentVisible()).toBe(false);
    });

    it('should correctly calculate the parents visible state if all parents are invisible', () => {
      parentWidget1.setVisible(false);
      parentWidget2.setVisible(false);
      parentWidget3.setVisible(false);
      expect(testWidget.isEveryParentVisible()).toBe(false);
    });
  });

  describe('uuid', () => {
    it('uuidPath for remote widget includes parent', () => {
      // remote widgets contain their parent classIds already (see AbstractWidget.classId, AbstractTable.classId, AbstractFormField.classId).
      const w4 = createRemoteWidgetTree();
      expect(w4.parent.uuidPath()).toBe('w3-class-id_w2-class-id_w1-class-id_root-class-id');
      expect(w4.uuidPath()).toBe('w4-class-id_w3-class-id_w2-class-id_w1-class-id_root-class-id');
    });

    function createRemoteWidgetTree(): Widget {
      const root = scout.create(Widget, {parent: session.root, classId: 'root-class-id'});
      const w1 = scout.create(Widget, {parent: root, classId: 'w1-class-id_root-class-id'});
      const w2 = scout.create(Widget, {parent: w1, classId: 'w2-class-id_w1-class-id_root-class-id'});
      const w3 = scout.create(Widget, {parent: w2, classId: 'w3-class-id_w2-class-id_w1-class-id_root-class-id'});
      return scout.create(Widget, {parent: w3, classId: 'w4-class-id_w3-class-id_w2-class-id_w1-class-id_root-class-id'});
    }

    it('uuidPath for local widget includes parent', () => {
      // local widgets do not contain their parent classIds already.
      const w4 = createLocalWidgetTree();
      expect(w4.parent.uuidPath()).toBe('w3-uuid|w2-uuid|w1-class-id_root-class-id');
      expect(w4.uuidPath()).toBe('w4-uuid|w3-uuid|w2-uuid|w1-class-id_root-class-id');
    });

    function createLocalWidgetTree(): Widget {
      const root = scout.create(Widget, {parent: session.root, classId: 'root-class-id'});
      const w1 = scout.create(Widget, {parent: root, classId: 'w1-class-id_root-class-id'});
      const w2 = scout.create(Widget, {parent: w1, uuid: 'w2-uuid'});
      const w3 = scout.create(Widget, {parent: w2, uuid: 'w3-uuid'});
      return scout.create(Widget, {parent: w3, uuid: 'w4-uuid'});
    }

    it('BookmarkAdapter.buildId returns id with parents for local and remote case', () => {
      const remote = createRemoteWidgetTree();
      expect(remote.parent.getBookmarkAdapter().buildId()).toBe('w3-class-id_w2-class-id_w1-class-id_root-class-id');
      expect(remote.getBookmarkAdapter().buildId()).toBe('w4-class-id_w3-class-id_w2-class-id_w1-class-id_root-class-id');

      const local = createLocalWidgetTree();
      expect(local.parent.getBookmarkAdapter().buildId()).toBe('w3-uuid|w2-uuid|w1-class-id_root-class-id');
      expect(local.getBookmarkAdapter().buildId()).toBe('w4-uuid|w3-uuid|w2-uuid|w1-class-id_root-class-id');
    });
  });
});
