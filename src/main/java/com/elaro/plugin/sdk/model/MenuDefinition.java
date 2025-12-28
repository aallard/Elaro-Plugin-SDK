package com.elaro.plugin.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Defines a menu structure for plugin navigation.
 * Menus can contain nested children for hierarchical navigation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDefinition {

    /**
     * Unique identifier for this menu item.
     */
    private String id;

    /**
     * Display title for the menu item.
     */
    private String title;

    /**
     * The Lucide icon name for the menu.
     */
    private String icon;

    /**
     * Sort order for menu display.
     */
    private int order;

    /**
     * The route path if this is a navigable menu item.
     */
    private String path;

    /**
     * Child menu items for hierarchical navigation.
     */
    private List<MenuDefinition> children;

    /**
     * Required permissions to see this menu item.
     */
    private List<String> permissions;
}
