package ai.elaro.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Defines a screen/page that the plugin provides.
 * This metadata is sent to the Elaro platform for menu registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenDefinition {

    /**
     * The route path for this screen (e.g., "/invoices").
     */
    private String path;

    /**
     * The display title for this screen.
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
     * Parent menu path for nested navigation.
     */
    private String parent;

    /**
     * Required permissions to access this screen.
     */
    private List<String> permissions;
}
