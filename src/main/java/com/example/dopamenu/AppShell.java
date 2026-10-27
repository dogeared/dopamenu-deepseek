package com.example.dopamenu;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * Global application shell. All styling lives in external CSS files: Lumo as
 * the component foundation and dopamenu.css for the application design.
 */
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles/dopamenu.css")
@ColorScheme(ColorScheme.Value.LIGHT)
public class AppShell implements AppShellConfigurator {
}
