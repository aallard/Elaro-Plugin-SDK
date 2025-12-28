package com.elaro.plugin.sdk.ui;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a plugin screen definition.
 * Screens annotated with this annotation will be automatically discovered
 * and registered with the Elaro platform.
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;PluginScreen(
 *     path = "/invoices",
 *     title = "Invoices",
 *     icon = "FileText",
 *     order = 10
 * )
 * public class InvoicesScreen {
 *     // Screen configuration or metadata
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface PluginScreen {

    /**
     * The route path for this screen (e.g., "/invoices").
     * This path will be prefixed with the plugin's base URL.
     */
    String path();

    /**
     * The display title for this screen in menus and tabs.
     */
    String title();

    /**
     * The Lucide icon name to display in the menu (e.g., "FileText", "Settings").
     * See https://lucide.dev/icons for available icons.
     */
    String icon() default "";

    /**
     * Sort order for menu display. Lower numbers appear first.
     */
    int order() default 100;

    /**
     * Parent menu path for nested navigation.
     * Leave empty for top-level menu items.
     */
    String parent() default "";

    /**
     * Required permissions to access this screen.
     * Users must have all specified permissions to see this screen.
     */
    String[] permissions() default {};
}
