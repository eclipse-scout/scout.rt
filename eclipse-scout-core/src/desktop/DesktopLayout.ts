/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Desktop, DesktopNavigation, Dimension, HtmlComponent, ResponsiveManager, Tree} from '../index';

export class DesktopLayout extends AbstractLayout {
  desktop: Desktop;

  constructor(desktop: Desktop) {
    super();
    this.desktop = desktop;
  }

  override layout($container: JQuery) {
    let animationProps,
      navigationWidth = 0,
      headerHeight = 0,
      desktop = this.desktop,
      navigation = desktop.navigation,
      header = desktop.header,
      bench = desktop.bench,
      // Animation moves header and bench to the left when navigation gets invisible or moves bench to the right if bench gets invisible (used for mobile)
      animated = desktop.animateLayoutChange,
      containerSize = this.containerSize(),
      fullWidthNavigation = navigation && navigation.htmlComp.layoutData.fullWidth;

    ResponsiveManager.get().handleResponsive(desktop, $container.width());

    if (navigation) {
      navigationWidth = this.calculateNavigationWidth(containerSize);
      if (desktop.splitter) {
        desktop.splitter.setPosition(navigationWidth);
      }

      if (desktop.navigationVisible) {
        let htmlNavigation = navigation.htmlComp;
        let navigationSize = new Dimension(navigationWidth, containerSize.height)
          .subtract(htmlNavigation.margins());
        htmlNavigation.setSize(navigationSize);
      }
    }

    if (header) {
      let htmlHeader = header.htmlComp;
      headerHeight = htmlHeader.$comp.outerHeight(true);
      if (desktop.headerVisible) {
        // positioning
        if (!animated) {
          header.$container.cssLeft(navigationWidth);
        }

        // sizing
        let headerSize = new Dimension(containerSize.width - navigationWidth, headerHeight)
          .subtract(htmlHeader.margins());
        if (!animated || fullWidthNavigation) {
          htmlHeader.setSize(headerSize);
        }

        if (animated) {
          animationProps = {
            left: containerSize.width
          };
          prepareAnimate(animationProps, htmlHeader, headerSize);
          this._animate(animationProps, htmlHeader);
        }
      }
    }

    if (bench) {
      let htmlBench = bench.htmlComp;
      if (desktop.benchVisible) {
        // positioning
        bench.$container.cssTop(headerHeight);
        if (!animated) {
          bench.$container.cssLeft(navigationWidth);
        }

        // sizing
        let benchSize = new Dimension(containerSize.width - navigationWidth, containerSize.height - headerHeight)
          .subtract(htmlBench.margins());
        if (!animated || fullWidthNavigation) {
          let oldSize = htmlBench.size();
          htmlBench.setSize(benchSize);
          if (!htmlBench.size().equals(oldSize)) {
            desktop.repositionTooltips();
          }
        }

        if (animated) {
          animationProps = {
            left: containerSize.width
          };
          prepareAnimate(animationProps, htmlBench, benchSize);
          this._animate(animationProps, htmlBench);
        }
      }
    }

    function prepareAnimate(animationProps: any, htmlComp: HtmlComponent, size: Dimension) {
      if (fullWidthNavigation) {
        // Slide bench in from right to left, don't resize
        htmlComp.$comp.cssLeft(containerSize.width);
      } else {
        // Resize bench
        animationProps.width = size.width;
        // Layout once before animation begins
        // Resizing on every step/progress would result in poor performance (e.g. when a form is open in the bench)
        htmlComp.setSize(size);
      }
      // Move to new point (=0, if navigation is invisible)
      animationProps.left = navigationWidth;
    }
  }

  /**
   * Used to animate bench and header
   */
  protected _animate(animationProps: any, htmlComp: HtmlComponent) {
    // If animation is already running, stop the existing and don't use timeout to schedule the new to have a smoother transition
    // Concurrent animation of the same element is bad because jquery messes up the overflow style
    if (htmlComp.$comp.is(':animated')) {
      animate.call(this);
    } else {
      // schedule animation to have a smoother start
      setTimeout(animate.bind(this));
    }

    function animate() {
      htmlComp.$comp.stop().animate(animationProps, {
        complete: this.desktop.onLayoutAnimationComplete.bind(this.desktop),
        step: this.desktop.onLayoutAnimationStep.bind(this.desktop)
      });
    }
  }

  containerSize(): Dimension {
    let htmlContainer = this.desktop.htmlComp,
      containerSize = htmlContainer.availableSize({exact: true});
    return containerSize.subtract(htmlContainer.insets());
  }

  calculateNavigationWidth(containerSize: Dimension): number {
    if (!this.desktop.navigationVisible) {
      return 0;
    }
    let navigationLayoutData = this.desktop.navigation.htmlComp.layoutData;
    if (navigationLayoutData.fullWidth) {
      return containerSize.width;
    }
    let splitterPosition = 0;
    if (this.desktop.splitter) {
      splitterPosition = this.desktop.splitter.position;
    }
    let outline = this.desktop.outline;
    if (!this.desktop.resizing && outline && outline.toggleBreadcrumbStyleEnabled) {
      // If toggleBreadcrumbStyleEnabled is true, BREADCRUMB_STYLE_WIDTH triggers the toggling between the two modes.
      // This code ensures this rule is never violated (necessary if mode is toggled programmatically rather than by the user)
      if (outline.displayStyle === Tree.DisplayStyle.BREADCRUMB) {
        splitterPosition = DesktopNavigation.BREADCRUMB_STYLE_WIDTH;
      } else if (Math.floor(splitterPosition) <= DesktopNavigation.BREADCRUMB_STYLE_WIDTH) {
        splitterPosition = DesktopNavigation.DEFAULT_STYLE_WIDTH;
      }
    }
    return Math.max(splitterPosition, DesktopNavigation.MIN_WIDTH); // ensure newSize is not negative
  }
}
