package com.example.dopamenu.ui;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Small helpers for driving the Vaadin component tree in plain JUnit tests.
 * Click listeners are component-event listeners, so firing a {@link ClickEvent}
 * exercises the same server-side path a browser click would.
 */
public final class UiTestSupport {

    // CurrentInstance stores weak references, so keep strong references for the
    // duration of a test to stop the GC from collecting the fake UI.
    private static UI currentUi;
    private static VaadinService currentService;

    private UiTestSupport() {
    }

    public static void installUi() {
        currentUi = new UI();
        UI.setCurrent(currentUi);
        RouteRegistry registry = mock(RouteRegistry.class);
        when(registry.getTargetUrl(any(), any())).thenReturn(Optional.of(""));
        Router router = mock(Router.class, RETURNS_DEEP_STUBS);
        when(router.getRegistry()).thenReturn(registry);
        currentService = mock(VaadinService.class, RETURNS_DEEP_STUBS);
        when(currentService.getRouter()).thenReturn(router);
        VaadinService.setCurrent(currentService);
    }

    public static void removeUi() {
        VaadinService.setCurrent(null);
        UI.setCurrent(null);
        currentUi = null;
        currentService = null;
    }

    public static void click(Component component) {
        ComponentUtil.fireEvent(component, new ClickEvent<>(component));
    }

    public static <T extends Component> List<T> findAll(Component root, Class<T> type) {
        List<T> found = new ArrayList<>();
        collect(root, type, found);
        return found;
    }

    public static <T extends Component> T findOne(Component root, Class<T> type) {
        List<T> found = findAll(root, type);
        if (found.isEmpty()) {
            throw new AssertionError("No " + type.getSimpleName() + " found");
        }
        return found.getFirst();
    }

    public static NativeButton button(Component root, String text) {
        return findAll(root, NativeButton.class).stream()
                .filter(button -> button.getElement().getTextRecursively().trim()
                        .equals(text))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No button with text '" + text + "' found in "
                                + root.getElement().getTextRecursively()));
    }

    public static NativeButton buttonByAriaLabel(Component root, String label) {
        return findAll(root, NativeButton.class).stream()
                .filter(button -> button.getAriaLabel().filter(label::equals).isPresent())
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No button with aria-label '" + label + "' found"));
    }

    public static boolean hasText(Component root, String text) {
        return root.getElement().getTextRecursively().contains(text);
    }

    public static void setField(Object target, String name, Object value) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                var field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException error) {
                throw new IllegalStateException(error);
            }
        }
        throw new IllegalArgumentException("No field named " + name);
    }

    private static <T extends Component> void collect(Component component, Class<T> type,
            List<T> found) {
        if (type.isInstance(component)) {
            found.add(type.cast(component));
        }
        component.getChildren().forEach(child -> collect(child, type, found));
    }
}
