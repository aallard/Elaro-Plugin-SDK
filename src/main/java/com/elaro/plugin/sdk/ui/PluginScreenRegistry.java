package com.elaro.plugin.sdk.ui;

import com.elaro.plugin.sdk.model.ScreenDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registry that discovers and collects all @PluginScreen annotated classes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PluginScreenRegistry {

    private final ApplicationContext applicationContext;

    /**
     * Get all registered screens sorted by order.
     *
     * @return List of screen definitions
     */
    public List<ScreenDefinition> getScreens() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(PluginScreen.class);

        List<ScreenDefinition> screens = beans.values().stream()
            .map(bean -> {
                Class<?> beanClass = bean.getClass();
                // Handle Spring proxies
                if (beanClass.getName().contains("$$")) {
                    beanClass = beanClass.getSuperclass();
                }

                PluginScreen annotation = beanClass.getAnnotation(PluginScreen.class);
                if (annotation == null) {
                    log.warn("Could not find @PluginScreen annotation on {}", beanClass.getName());
                    return null;
                }

                return ScreenDefinition.builder()
                    .path(annotation.path())
                    .title(annotation.title())
                    .icon(annotation.icon())
                    .order(annotation.order())
                    .parent(annotation.parent())
                    .permissions(Arrays.asList(annotation.permissions()))
                    .build();
            })
            .filter(screen -> screen != null)
            .sorted(Comparator.comparingInt(ScreenDefinition::getOrder))
            .collect(Collectors.toList());

        log.debug("Discovered {} plugin screens", screens.size());
        return screens;
    }

    /**
     * Get screens filtered by parent path.
     *
     * @param parent The parent path to filter by (empty for top-level)
     * @return List of screen definitions with matching parent
     */
    public List<ScreenDefinition> getScreensByParent(String parent) {
        return getScreens().stream()
            .filter(screen -> {
                String screenParent = screen.getParent();
                if (parent == null || parent.isEmpty()) {
                    return screenParent == null || screenParent.isEmpty();
                }
                return parent.equals(screenParent);
            })
            .collect(Collectors.toList());
    }

    /**
     * Get the total count of registered screens.
     */
    public int getScreenCount() {
        return getScreens().size();
    }
}
