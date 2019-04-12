import * as $ from 'jquery';
import NullLayout from './NullLayout';
import Scout from '../Scout';
import Graphics from '../Utils/Graphics';

export default class HtmlComponent {


    constructor($comp, session){
        if (!session) {
            throw new Error('session must be defined for ' + this.debug());
        }
        this.$comp = $comp;
        this.layout = new NullLayout();
        this.layoutData;
        this.valid = false;

        /**
         * Flag to indicate that the component has been layouted at least once. Invalidation should NOT reset this flag.
         */
        this.layouted = false;

        /**
         * Flag to indicate that the component is being layouted.
         */
        this.layouting = false;

        /**
         * May be set to temporarily disable invalidation (e.g. if the component gets modified during the layouting process)
         */
        this.suppressInvalidate = false;

        /**
         * Set pixelBasedSizing to false if your component automatically adjusts its size,
         * e.g. by using CSS styling -> setSize won't be called.
         */
        this.pixelBasedSizing = true;
        this.sizeCached = null;

        /**
         * Object which stores the computed preferred size. Key is a string containing the width and height hints.
         * @see #computePrefSizeKey(options);
         */
        this.prefSizeCached = {};
        this.session = session;
    }




    /**
     * Returns the parent or $comp or null when $comp has no parent.
     * Creates a new instance of HtmlComponent if the parent DOM element has no linked instance yet.
     */
    getParent() {
        var $parent = this.$comp.parent();
        if ($parent.length === 0) {
            return null;
        } else {
            return HtmlComponent.optGet($parent);
        }
    };

    /**
     * @returns true if the given htmlComponent is an ancestor, false if not
     */
    isDescendantOf(htmlComp) {
        var $parent = this.$comp.parent();
        while ($parent.length > 0) {
            if (HtmlComponent.optGet($parent) === htmlComp) {
                return true;
            }
            $parent = $parent.parent();
        }
        return false;
    };

    /**
     * Computes the preferred height if the component is scrollable and returns it if it is greater than the actual size.
     * If it is not scrollable, the actual height is returned.
     * <p>
     * The returned size contains insets (padding and border) but no margin. The width is always the actual width because there are no horizontal scrollbars.
     *
     * OPTION    DEFAULT VALUE   DESCRIPTION
     * -------------------------------------
     * exact     false           When set to true the returned dimensions may contain fractional digits, otherwise the sizes are rounded up.
     *
     * @param (options) may contain the options of the above table
     */
    availableSize(options) {
        options = options || {};
        var size = this.size({
            exact: options.exact
        });

        if (this.scrollable) {
            var prefSize = this.prefSize({
                widthHint: size.width,
                removeMarginFromHints: false // Since the width of this component is used as hint, the margin must not be removed
            });
            if (prefSize.height > size.height) {
                size.height = prefSize.height;
            }
        }

        return size;
    };

    invalidateLayout(htmlSource) {
        this.valid = false;
        this.prefSizeCached = {};
        if (this.layout) {
            this.layout.invalidate(htmlSource);
        }
    };

    /**
     * Calls the layout of the component to layout its children but only if the component is not valid.
     * @exception when component has no layout
     * @return true if validation was successful, false if it could not be executed (e.g. because the element is invisible or detached)
     */
    validateLayout() {
        if (!this.layout) {
            throw new Error('Called layout() but component has no layout');
        }
        // Don't layout components which don't exist anymore, are invisible or are detached from the DOM
        if (!this.isAttachedAndVisible()) {
            return false;
        }
        var parent = this.getParent();
        // Check the visibility of the parents as well.
        // This check loops to the top of the DOM tree.
        // To improve performance, the check (isEveryParentVisible) is not executed if the parent already did it, which is the case if parent is being layouted.
        if ((!parent || !parent.layouting) && !this.$comp.isEveryParentVisible()) {
            return false;
        }
        if (!this.valid) {
            this.layouting = true;
            this.layout.layout(this.$comp);
            this.layouting = false;
            this.layouted = true;
            // Save size for later use (necessary if pixelBasedSizing is set to false)
            this.sizeCached = this.size();
            this.valid = true;
        }
        return true;
    };

    /**
     * Performs invalidateLayout() and validateLayout() subsequently.
     */
    revalidateLayout() {
        this.invalidateLayout();
        this.validateLayout();
    };

    /**
     * Invalidates the component-tree up to the next validate root, but only if invalidateParents is set to true.
     */
    invalidateLayoutTree(invalidateParents) {
        if (this.suppressInvalidate) {
            return;
        }
        if (Scout.nvl(invalidateParents, true)) {
            this.session.layoutValidator.invalidateTree(this); // will call invalidateLayout(), which sets this.valid = false
        } else {
            this.valid = false;
            this.session.layoutValidator.invalidate(this);
        }
    };

    /**
     * Layouts all invalid components
     */
    validateLayoutTree() {
        this.session.layoutValidator.validate();
    };

    /**
     * Performs invalidateLayoutTree() and validateLayoutTree() subsequently.
     */
    revalidateLayoutTree(invalidateParents) {
        this.invalidateLayoutTree(invalidateParents);
        this.validateLayoutTree();
    };

    /**
     * Marks the end of the parent invalidation. <p>
     * A component is a validate root if its size does not depend on the visibility or bounds of its children.<p>
     * Example: It is not necessary to relayout the whole form if just the label of a form field gets invisible.
     * Only the form field container needs to be relayouted. In this case the form field container is the validate root.
     */
    isValidateRoot() {
        if (this.validateRoot) {
            return true;
        }
        if (!this.layoutData || !this.layoutData.isValidateRoot) {
            return false;
        }
        return this.layoutData.isValidateRoot();
    };

    /**
     * Sets the given layout.
     */
    setLayout(layout) {
        this.layout = layout;
        if (layout.cssClass) {
            this.$comp.addClass(layout.cssClass);
        }
    };

    /**
     * Returns the preferred size of the component, insets included, margin excluded<p>
     * The preferred size is cached until the component will be invalidated.
     * Hence, subsequent calls to this function will return the cached preferred size unless the component is invalidated.
     * <p>
     *
     * OPTION                  DEFAULT VALUE   DESCRIPTION
     * --------------------------------------------------
     * includeMargin           false           Whether to include the margin in the returned size.
     * widthHint               null            When set, horizontal padding, border and margin are removed from it so that the actual layout does not need to take care of it.
     * heightHint              null            When set, vertical padding, border and margin are removed from it so that the actual layout does not need to take care of it.
     * removeMarginFromHints   true            Whether or not to automatically remove the margin from the hints.
     *
     * @param (options) an optional options object. Short-hand version: If a boolean is passed instead of an object, the value is automatically converted to the option 'includeMargin'.
     *                  May contain the options of the above table. All other options are passed as they are to the layout when @{link layout.preferredLayoutSize()} is called.
     *                  Possible options may be found at @{link scout.graphics.prefSize()}, but it depends on the actual layout if these options have an effect or not.
     * @exception When component has no layout
     */
    prefSize(options) {
        if (typeof options === 'boolean') {
            options = {
                includeMargin: options
            };
        } else {
            // Create a copy to not modify the original options
            options = $.extend({}, options);
        }
        var includeMargin = Scout.nvl(options.includeMargin, false);
        options.includeMargin = null;
        if (!this.layout) {
            throw new Error('Called prefSize() but component has no layout');
        }

        var prefSizeCacheKey = this.computePrefSizeKey(options);
        var prefSizeCached = this.prefSizeCached[prefSizeCacheKey];
        if (this.valid && !Scout.isNullOrUndefined(prefSizeCached)) {
            if (includeMargin) {
                prefSizeCached.add(this.margins());
            }
            return prefSizeCached;
        }

        if (options.widthHint || options.heightHint) {
            this._adjustSizeHintsForPrefSize(options);
        }

        var prefSize = this.layout.preferredLayoutSize(this.$comp, options);
        this._adjustPrefSizeWithMinMaxSize(prefSize);
        this.prefSizeCached[prefSizeCacheKey] = prefSize;

        if (includeMargin) {
            prefSize = prefSize.add(this.margins());
        }
        return prefSize;
    };

    computePrefSizeKey(options) {
        return 'wHint' + Scout.nvl(options.widthHint, '-1') + 'hHint' + Scout.nvl(options.heightHint, '-1');
    };

    _adjustSizeHintsForPrefSize(options) {
        // Remove padding, border and margin from the width and heightHint so that the actual layout does not need to take care of it
        var removeMargin = Scout.nvl(options.removeMarginFromHints, true);
        options.removeMarginFromHints = null;
        if (options.widthHint) {
            options.widthHint -= this.insets(removeMargin).horizontal();
        }
        if (options.heightHint) {
            options.heightHint -= this.insets(removeMargin).vertical();
        }
    };

    _adjustPrefSizeWithMinMaxSize(prefSize) {
        // Component may define a min or max height/height -> adjust the pref size accordingly
        var minHeight = this.$comp.cssMinHeight();
        var maxHeight = this.$comp.cssMaxHeight();
        var minWidth = this.$comp.cssMinWidth();
        var maxWidth = this.$comp.cssMaxWidth();
        prefSize.height = Math.max(prefSize.height, minHeight);
        prefSize.height = Math.min(prefSize.height, maxHeight);
        prefSize.width = Math.max(prefSize.width, minWidth);
        prefSize.width = Math.min(prefSize.width, maxWidth);
    };

    /**
     * Returns the inset-dimensions of the component (padding and border, no margin).
     */
    insets(options) {
        return Graphics.insets(this.$comp, options);
    };

    margins() {
        return Graphics.margins(this.$comp);
    };

    size(options) {
        return Graphics.size(this.$comp, options);
    };

    /**
     * Sets the size of the component, insets included. Which means: the method subtracts the components insets
     * from the given size before setting the width/height of the component.
     */
    setSize(size) {
        if (!this.isAttachedAndVisible()) {
            // don't invalidate the layout if component is invisible because sizes may not be read correctly and therefore prefSize will be wrong
            return;
        }
        var oldSize = this.sizeCached;
        if (!size.equals(oldSize)) {
            this.invalidateLayout();
        }
        if (this.pixelBasedSizing) {
            Graphics.setSize(this.$comp, size);
        }
        this.validateLayout();
    };

    bounds(options) {
        return Graphics.bounds(this.$comp, options);
    };

    position() {
        return Graphics.position(this.$comp);
    };

    offsetBounds(options) {
        return Graphics.offsetBounds(this.$comp, options);
    };

    offset() {
        return Graphics.offset(this.$comp);
    };

    /**
     * Delegation to scout.graphics.setLocation
     * @param location scout.Point
     */
    setLocation(location) {
        Graphics.setLocation(this.$comp, location);
    };

    location() {
        return Graphics.location(this.$comp);
    };

    setBounds(bounds) {
        if (!this.isAttachedAndVisible()) {
            // don't invalidate the layout if component is invisible because sizes may not be read correctly and therefore prefSize will be wrong
            return;
        }
        var oldBounds = this.offsetBounds();
        if (!oldBounds.dimension().equals(bounds.dimension())) {
            this.invalidateLayout();
        }
        Graphics.setBounds(this.$comp, bounds);
        this.validateLayout();
    };

    /**
     * Sets the component to its preferred size.
     */
    pack() {
        var preferredSize = this.prefSize();
        this.setSize(preferredSize);
    };

    /**
     * Checks whether $comp is in the DOM or has been removed or detached.<br>
     * Also returns false if the $comp does not belong to a window (defaultView) anymore. This may happen if it belonged to a popup window which is now closed
     */
    isAttached() {
        return this.$comp.isAttached() && this.$comp.window(true);
    };

    isVisible() {
        return this.$comp.isVisible();
    };

    isAttachedAndVisible() {
        return this.isAttached() && this.isVisible();
    };

    debug() {
        return null;//scout.graphics.debugOutput(this.$comp);
    };



    static install($comp, session) {
        if (!$comp) {
            throw new Error('Missing argument "$comp"');
        }
        if (!session) {
            throw new Error('Missing argument "session"');
        }

        var htmlComp = new HtmlComponent($comp, session);
        // link DOM element with the new instance
        $comp.data('htmlComponent', htmlComp);

        return htmlComp;
    };

    static get($comp) {
        var htmlComp = this.optGet($comp);
        if (!htmlComp) {
            var details = '';
            if ($comp) {
                details = '\nClass: ' + $comp.attr('class');
                details += '\nId: ' + $comp.attr('id');
                details += '\nAttached: ' + $comp.isAttached();
            }
            throw new Error('data "htmlComponent" is undefined.' + details);
        }
        return htmlComp;
    };

    static optGet($comp) {
        return $comp && $comp.data('htmlComponent');
    };

}

