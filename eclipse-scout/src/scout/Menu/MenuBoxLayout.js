import AbstractLayout from '../Layout/AbstractLayout';
import Graphics from '../Utils/Graphics';

export default class MenuBoxLayout extends AbstractLayout {
    constructor(menuBox) {
        super();
        this.menuBox = menuBox;
        // References to prevent too many DOM updates
        this.firstMenu = null;
        this.lastMenu = null;
    }

    /**
     * @override AbstractLayout.js
     */
    layout($container) {
        var htmlContainer = this.menuBox.htmlComp,
            containerSize = htmlContainer.size(),
            menus = this.visibleMenus(),
            menusWidth;

        // Make sure open popups are at the correct position after layouting
        this.menuBox.session.layoutValidator.schedulePostValidateFunction(function() {
            menus.forEach(function(menu) {
                if (menu.popup) {
                    menu.popup.position();
                }
            });
        });

        this.updateFirstAndLastMenuMarker(menus);
        this.undoCollapse(menus);
        this.undoCompact(menus);
        this.undoShrink(menus);
        menusWidth = this.actualPrefSize(menus).width;
        if (menusWidth <= containerSize.width) {
            // OK, every menu fits into container
            return;
        }

        // Menus don't fit

        // First approach: Set menuBox into compact mode
        this.compact(menus);
        menusWidth = this.actualPrefSize(menus).width;
        if (menusWidth <= containerSize.width) {
            // OK, every menu fits into container
            return;
        }

        // Second approach: Make text invisible and only show the icon (if available)
        this.shrink(menus);
        menusWidth = this.actualPrefSize(menus).width;
        if (menusWidth <= containerSize.width) {
            // OK, every menu fits into container
            return;
        }

        // Third approach: Create ellipsis and move overflown menus into it
        this.collapse(menus, containerSize, menusWidth);
    };

    preferredLayoutSize($container) {
        var menus = this.visibleMenus();

        this.updateFirstAndLastMenuMarker(menus);
        this.undoCollapse(menus);
        this.undoCompact(menus);
        this.undoShrink(menus);

        return this.actualPrefSize();
    };

    compact(menus) {
        if (this.menuBox.compactOrig === undefined) {
            this.menuBox.compactOrig = this.menuBox.compact;
            this.menuBox.htmlComp.suppressInvalidate = true;
            this.menuBox.setCompact(true);
            this.menuBox.htmlComp.suppressInvalidate = false;
        }

        this.compactMenus(menus);
    };

    undoCompact(menus) {
        if (this.menuBox.compactOrig !== undefined) {
            this.menuBox.htmlComp.suppressInvalidate = true;
            this.menuBox.setCompact(this.menuBox.compactOrig);
            this.menuBox.htmlComp.suppressInvalidate = false;
            this.menuBox.compactOrig = undefined;
        }

        this.undoCompactMenus(menus);
    };

    /**
     * Sets all menus into compact mode.
     */
    compactMenus(menus) {
        menus = menus || this.visibleMenus();
        menus.forEach(function(menu) {
            if (menu.compactOrig !== undefined) {
                // already done
                return;
            }
            menu.compactOrig = menu.compact;
            menu.htmlComp.suppressInvalidate = true;
            menu.setCompact(true);
            menu.htmlComp.suppressInvalidate = false;
        }, this);

        if (this._ellipsis) {
            this._ellipsis.setCompact(true);
        }
    };

    /**
     * Restores to the previous state of the compact property.
     */
    undoCompactMenus(menus) {
        menus = menus || this.visibleMenus();
        menus.forEach(function(menu) {
            if (menu.compactOrig === undefined) {
                return;
            }
            // Restore old compact state
            menu.htmlComp.suppressInvalidate = true;
            menu.setCompact(menu.compactOrig);
            menu.htmlComp.suppressInvalidate = false;
            menu.compactOrig = undefined;
        }, this);

        if (this._ellipsis) {
            this._ellipsis.setCompact(false);
        }
    };

    shrink(menus) {
        this.shrinkMenus(menus);
    };

    /**
     * Makes the text invisible of all menus with an icon.
     */
    shrinkMenus(menus) {
        menus = menus || this.visibleMenus();
        menus.forEach(function(menu) {
            if (menu.textVisibleOrig !== undefined) {
                // already done
                return;
            }
            if (menu.iconId) {
                menu.textVisibleOrig = menu.textVisible;
                menu.htmlComp.suppressInvalidate = true;
                menu.setTextVisible(false);
                menu.htmlComp.suppressInvalidate = false;
            }
        }, this);
    };

    undoShrink(menus) {
        this.undoShrinkMenus(menus);
    };

    undoShrinkMenus(menus) {
        menus = menus || this.visibleMenus();
        menus.forEach(function(menu) {
            if (menu.textVisibleOrig === undefined) {
                return;
            }
            // Restore old text visible state
            menu.htmlComp.suppressInvalidate = true;
            menu.setTextVisible(menu.textVisibleOrig);
            menu.htmlComp.suppressInvalidate = false;
            menu.textVisibleOrig = undefined;
        }, this);
    };

    collapse(menus, containerSize, menusWidth) {
        this._createAndRenderEllipsis(this.menuBox.$container);
        var collapsedMenus = this._moveOverflowMenusIntoEllipsis(containerSize, menusWidth);
        this.updateFirstAndLastMenuMarker(collapsedMenus);
    };

    /**
     * Undoes the collapsing by removing ellipsis and rendering non rendered menus.
     */
    undoCollapse(menus) {
        menus = menus || this.visibleMenus();
        this._destroyEllipsis();
        this._removeMenusFromEllipsis(menus);
    };

    _createAndRenderEllipsis($container) {
        var ellipsis = scout.menus.createEllipsisMenu({
            parent: this.menuBox,
            horizontalAlignment: 1,
            compact: this.menuBox.compact
        });
        ellipsis.uiCssClass = this.menuBox.uiMenuCssClass;
        ellipsis.render($container);
        this._ellipsis = ellipsis;
    };

    _destroyEllipsis() {
        if (this._ellipsis) {
            this._ellipsis.destroy();
            this._ellipsis = null;
        }
    };

    /**
     * Moves every menu which doesn't fit into the container into the ellipsis menu.
     * Returns the list of 'surviving' menus (with the ellipsis menu being the last element).
     */
    _moveOverflowMenusIntoEllipsis(containerSize, menusWidth) {
        var collapsedMenus = [this._ellipsis];
        var ellipsisSize = Graphics.size(this._ellipsis.$container, true);
        menusWidth += ellipsisSize.width;
        this.visibleMenus().slice().reverse().forEach(function(menu) {
            var menuSize;
            if (menusWidth > containerSize.width) {
                // Menu does not fit -> move to ellipsis menu
                menuSize = Graphics.size(menu.$container, true);
                menusWidth -= menuSize.width;
                scout.menus.moveMenuIntoEllipsis(menu, this._ellipsis);
            } else {
                collapsedMenus.unshift(menu); // add as first element
            }
        }, this);
        return collapsedMenus;
    };

    _removeMenusFromEllipsis(menus) {
        menus = menus || this.visibleMenus();
        menus.forEach(function(menu) {
            scout.menus.removeMenuFromEllipsis(menu, this.menuBox.$container);
        }, this);
    };

    actualPrefSize(menus) {
        var menusWidth, prefSize;

        menus = menus || this.visibleMenus();
        menusWidth = this._menusWidth(menus);
        prefSize = Graphics.prefSize(this.menuBox.$container, {
            includeMargin: true,
            useCssSize: true
        });
        prefSize.width = menusWidth + this.menuBox.htmlComp.insets().horizontal();

        return prefSize;
    };

    /**
     * @return the current width of all menus incl. the ellipsis
     */
    _menusWidth(menus) {
        var menusWidth = 0;
        menus = menus || this.visibleMenus();
        menus.forEach(function(menu) {
            if (menu.rendered) {
                menusWidth += menu.$container.outerWidth(true);
            }
        }, this);
        if (this._ellipsis) {
            menusWidth += this._ellipsis.$container.outerWidth(true);
        }
        return menusWidth;
    };

    compactPrefSize(menus) {
        menus = menus || this.visibleMenus();

        this.updateFirstAndLastMenuMarker(menus);
        this.undoCollapse(menus);
        this.undoShrink(menus);
        this.compact(menus);

        return this.actualPrefSize();
    };

    shrinkPrefSize(menus) {
        menus = menus || this.visibleMenus();

        this.updateFirstAndLastMenuMarker(menus);
        this.undoCollapse(menus);
        this.compact(menus);
        this.shrink(menus);

        return this.actualPrefSize();
    };

    visibleMenus() {
        return this.menuBox.menus.filter(function(menu) {
            return menu.visible;
        }, this);
    };

    updateFirstAndLastMenuMarker(menus) {
        // Find first and last rendered menu
        var firstMenu = null;
        var lastMenu = null;
        (menus || []).forEach(function(menu) {
            if (menu.rendered) {
                if (!firstMenu) {
                    firstMenu = menu;
                }
                lastMenu = menu;
            }
        });

        // Check if first or last menu has changed (prevents unnecessary DOM updates)
        if (firstMenu !== this.firstMenu || lastMenu !== this.lastMenu) {
            // Remove existing markers
            if (this.firstMenu && this.firstMenu.rendered) {
                this.firstMenu.$container.removeClass('first');
            }
            if (this.lastMenu && this.lastMenu.rendered) {
                this.lastMenu.$container.removeClass('last');
            }
            // Remember found menus
            this.firstMenu = firstMenu;
            this.lastMenu = lastMenu;
            // Add markers to found menus
            if (this.firstMenu) {
                this.firstMenu.$container.addClass('first');
            }
            if (this.lastMenu) {
                this.lastMenu.$container.addClass('last');
            }
        }
    };

}

