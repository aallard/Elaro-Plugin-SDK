package com.elaro.plugin.sdk;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Main annotation to enable Elaro Plugin functionality.
 * Apply this annotation to your Spring Boot application class to register
 * your service as an Elaro plugin.
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;SpringBootApplication
 * &#64;ElaroPlugin(
 *     id = "com.acme.billing",
 *     name = "Billing Service",
 *     vendor = "Acme Corp"
 * )
 * public class BillingServiceApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(BillingServiceApplication.class, args);
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ElaroPluginAutoConfiguration.class)
public @interface ElaroPlugin {

    /**
     * Unique identifier for this plugin (e.g., "com.acme.billing").
     * This should be a reverse-domain style identifier unique across all plugins.
     */
    String id();

    /**
     * Human-readable display name for this plugin.
     */
    String name();

    /**
     * The vendor or company name that created this plugin.
     */
    String vendor();

    /**
     * Optional description of what this plugin does.
     */
    String description() default "";
}
