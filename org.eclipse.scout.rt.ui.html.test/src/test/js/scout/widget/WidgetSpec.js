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
describe('Widget', function() {

  var session, widget, parent;

  var TestWidget = function() {
    TestWidget.parent.call(this);
  };
  scout.inherits(TestWidget, scout.NullWidget);
  TestWidget.prototype._render = function() {
    this.$container = this.$parent.appendDiv();
  };

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();

    widget = new TestWidget();
    parent = new TestWidget();
  });

  function createWidget(model) {
    var defaults = {
      parent: parent,
      session: session
    };
    model = $.extend({}, defaults, model);
    var widget = new TestWidget();
    widget.init(model);
    return widget;
  }

  describe('rendering', function() {

    it('should set rendering, rendered flags correctly', function() {
      widget.init({
        session: session,
        parent: parent
      });
      expect(widget.rendered).toBe(false);
      expect(widget.rendering).toBe(false);
      widget.render(session.$entryPoint);
      expect(widget.rendered).toBe(true);
      expect(widget.rendering).toBe(false);
    });

    it('should set rendering flag to true _while_ the component is rendering', function() {
      var rendering;
      widget._render = function() {
        rendering = this.rendering;
      };
      widget.init({
        session: session,
        parent: parent
      });
      widget.render(session.$entryPoint);
      expect(rendering).toBe(true);
    });

  });

  describe('attach/detach', function() {

    it('attached and rendered is false by default', function() {
      expect(widget.rendered).toBe(false);
      expect(widget.attached).toBe(false);
    });

    it('attached and rendered has the right value after render/remove and attach/detach', function() {
      var $parent = $('<div>');
      widget.init({
        session: session,
        parent: parent
      });
      widget.render($parent);
      expect(widget.rendered).toBe(true);
      expect(widget.attached).toBe(true);

      widget.detach();
      expect(widget.rendered).toBe(true);
      expect(widget.attached).toBe(false);

      widget.attach();
      expect(widget.rendered).toBe(true);
      expect(widget.attached).toBe(true);

      widget.remove();
      expect(widget.rendered).toBe(false);
      expect(widget.attached).toBe(false);
    });

  });

  describe('clone', function() {

    var model, widget, expectedProperties = ['id', 'session', 'objectType', 'parent', 'text'];

    beforeEach(function() {
      model = createSimpleModel('Menu', session);
      model.label = 'bar';
      widget = scout.create(model);
      widget.$container = 'dummy container property';
    });

    it('clones only properties marked as clone property', function() {
      var widgetClone = widget.clone({
        parent: widget.parent
      });
      // should contain the following properties:
      expectedProperties.forEach(function(propertyName) {
        expect(widgetClone[propertyName]).not.toBe(undefined);
      });
      // but not the $container property (which has been added later)
      expect(widgetClone.$container).toBe(undefined);
    });

    it('\'text\' must be recognized as clone property, but not \'$container\'', function() {
      expect(widget.isCloneProperty('text')).toBe(true);
      expect(widget.isCloneProperty('$container')).toBe(false);
    });

    it('prefers properties passed as modelOverride', function() {
      var widgetClone = widget.clone({
        parent: widget.parent,
        text: 'foo'
      });
      expect(widgetClone.text).toBe('foo');
    });

  });

  describe('init', function() {

    it('links widget properties with the widget', function() {
      var child = createWidget({
        parent: parent
      });
      var widget = createWidget({
        parent: parent,
        childWidget: child
      });

      expect(child.parent).toBe(widget);
      expect(child.owner).toBe(parent);
    });

  });

  describe('destroy', function() {
    it('destroys the widget', function() {
      var widget = createWidget({
        parent: parent
      });
      expect(widget.destroyed).toBe(false);

      widget.destroy();
      expect(widget.destroyed).toBe(true);
    });

    it('destroys the children', function() {
      var widget = createWidget({
        parent: parent
      });
      var child0 = createWidget({
        parent: widget
      });
      var child1 = createWidget({
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

    it('does only destroy children if the parent is the owner', function() {
      var widget = createWidget({
        parent: parent
      });
      var another = createWidget({
        parent: parent
      });
      var child0 = createWidget({
        parent: widget,
        owner: another
      });
      var child1 = createWidget({
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

    it('removes the link to parent and owner', function() {
      var widget = createWidget({
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
  });

  describe('setParent', function() {
    it('links the widget with the new parent', function() {
      var widget = createWidget({
        parent: parent
      });
      var another = createWidget({
        parent: parent
      });
      expect(widget.parent).toBe(parent);
      expect(another.parent).toBe(parent);

      another.setParent(widget);
      expect(widget.parent).toBe(parent);
      expect(another.parent).toBe(widget);
    });

    it('removes the widget from the old parent if the old is not the owner', function() {
      var widget = createWidget({
        parent: parent
      });
      var owner = createWidget({
        parent: new TestWidget()
      });
      var another = createWidget({
        parent: parent,
        owner: owner
      });
      expect(parent.children[0]).toBe(widget);
      expect(parent.children[1]).toBe(another);
      expect(widget.children.length).toBe(0);

      another.setParent(widget);
      expect(parent.children[0]).toBe(widget);
      expect(parent.children.length).toBe(1);
      expect(widget.children.length).toBe(1);
      expect(widget.children[0]).toBe(another);
    });

    it('does not remove the widget from the old parent if the old is the owner', function() {
      var widget = createWidget({
        parent: parent
      });
      var another = createWidget({
        parent: parent
      });
      expect(another.owner).toBe(parent);
      expect(parent.children[0]).toBe(widget);
      expect(parent.children[1]).toBe(another);
      expect(widget.children.length).toBe(0);

      // The reference to the owner must always be maintained so that the widget will be destroyed eventually
      another.setParent(widget);
      expect(parent.children[0]).toBe(widget);
      expect(parent.children[1]).toBe(another);
      expect(parent.children.length).toBe(2);
      expect(widget.children.length).toBe(1);
      expect(widget.children[0]).toBe(another);
    });

    it('relinks parent destroy listener to the new parent', function() {
      var widget = createWidget({
        parent: parent
      });
      var another = createWidget({
        parent: parent
      });
      expect(widget.parent).toBe(parent);
      expect(another.parent).toBe(parent);

      var widgetListenerCount = widget.events._eventListeners.length;
      var parentListenerCount = parent.events._eventListeners.length;
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
  });

  describe('remove', function() {
    it('removes the widget', function() {
      var widget = createWidget({
        parent: parent
      });
      widget.render(session.$entryPoint);
      expect(widget.rendered).toBe(true);
      expect(widget.$container).toBeDefined();

      widget.remove();
      expect(widget.rendered).toBe(false);
      expect(widget.$container).toBe(null);
    });

    it('removes the children', function() {
      var widget = createWidget({
        parent: parent
      });
      var child0 = createWidget({
        parent: widget
      });
      var child0_0 = createWidget({
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

    it('does not remove the children if owner is removed but parent is still rendered', function() {
      var widget = createWidget({
        parent: parent
      });
      var child0 = createWidget({
        parent: widget
      });
      var owner = createWidget({
        parent: new TestWidget()
      });
      var anotherChild = createWidget({
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
      // Otherwise the widget would be in a inconsistent state (destroyed, but still rendered)
      owner.destroy();
      expect(owner.rendered).toBe(false);
      expect(owner.destroyed).toBe(true);
      expect(anotherChild.rendered).toBe(false);
      expect(anotherChild.destroyed).toBe(true);
      expect(widget.rendered).toBe(true);
      expect(child0.rendered).toBe(true);
    });
  });

  describe('setProperty', function() {

    it('triggers a property change event if the value changes', function() {
      var propertyChangeEvent;
      var widget = createWidget();
      widget.on('propertyChange', function(event) {
        propertyChangeEvent = event;
      });
      widget.setProperty('selected', true);
      expect(propertyChangeEvent.type).toBe('propertyChange');
      expect(propertyChangeEvent.name).toBe('selected');
      expect(propertyChangeEvent.oldValue).toBe(undefined);
      expect(propertyChangeEvent.newValue).toBe(true);

      widget.setProperty('selected', false);
      expect(propertyChangeEvent.type).toBe('propertyChange');
      expect(propertyChangeEvent.name).toBe('selected');
      expect(propertyChangeEvent.oldValue).toBe(true);
      expect(propertyChangeEvent.newValue).toBe(false);
    });

    it('does not trigger a property change event if the value does not change', function() {
      var propertyChangeEvent;
      var widget = createWidget();
      widget.on('propertyChange', function(event) {
        propertyChangeEvent = event;
      });
      widget.selected = true;
      widget.setProperty('selected', true);
      expect(propertyChangeEvent).toBe(undefined);
    });

    describe('with widget property', function() {
      it('links the widget with the new child widget', function() {
        var widget = createWidget({
          parent: parent
        });
        var another = createWidget({
          parent: parent
        });
        var child = createWidget({
          parent: parent
        });

        widget.setChildWidget(child);
        expect(child.parent).toBe(widget);
        expect(child.owner).toBe(parent);

        another.setChildWidget(child);
        expect(child.parent).toBe(another);
        expect(child.owner).toBe(parent);
      });

      it('links the widget with the new child widgets if it is an array', function() {
        var widget = createWidget({
          parent: parent
        });
        var another = createWidget({
          parent: parent
        });
        var children = [
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

      it('does not fail if new widget is null', function() {
        var widget = createWidget({
          parent: parent
        });
        var another = createWidget({
          parent: parent
        });
        var child = createWidget({
          parent: parent
        });

        widget.setChildWidget(child);
        widget.setChildWidget(null);
      });
    });

    it('calls the _render* method if there is one for this property', function() {
      var widget = createWidget({
        parent: parent
      });
      widget.render(session.$entryPoint);

      // Must not fail, render is optional
      widget.setProperty('foo', 'xyz');

      // Add render method and set property again
      widget._renderFoo = function() {
        this.$container.text(this.foo);
      };
      widget.setProperty('foo', 'bar');
      expect(widget.$container.text()).toBe('bar');
    });

  });

  describe("property css class", function() {

    it("adds or removes custom css class", function() {
      var widget = createWidget({
        parent: parent
      });
      widget.render(session.$entryPoint);

      widget.setCssClass('custom-class');
      expect(widget.$container).toHaveClass('custom-class');

      widget.setCssClass('');
      expect(widget.$container).not.toHaveClass('custom-class');
    });

    it("does not accidentally remove other css classes on a property change", function() {
      var widget = createWidget({
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

    describe("addCssClass", function() {

      it("adds the cssClass to the existing classes", function() {
        var widget = createWidget({
          parent: parent
        });
        widget.render(session.$entryPoint);
        widget.addCssClass('custom-class');
        expect(widget.$container).toHaveClass('custom-class');

        widget.addCssClass('another-class');
        expect(widget.cssClass).toBe('custom-class another-class');
        expect(widget.$container).toHaveClass('custom-class');
        expect(widget.$container).toHaveClass('another-class');
      });

      it("does not add the same class multiple times", function() {
        var widget = createWidget({
          parent: parent
        });
        widget.render(session.$entryPoint);
        widget.addCssClass('custom-class');
        expect(widget.cssClass).toBe('custom-class');

        widget.addCssClass('custom-class');
        expect(widget.cssClass).toBe('custom-class');
      });

    });

    describe("removeCssClass", function() {

      it("removes the cssClass from the existing classes", function() {
        var widget = createWidget({
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
  });

});
