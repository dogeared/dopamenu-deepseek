package com.example.dopamenu.ui;

import com.vaadin.flow.component.HasStyle;

/**
 * Tiny helper for applying several CSS class names in one statement.
 */
public final class Css {

    private Css() {
    }

    public static void add(HasStyle component, String... classNames) {
        for (String className : classNames) {
            component.addClassName(className);
        }
    }
}
