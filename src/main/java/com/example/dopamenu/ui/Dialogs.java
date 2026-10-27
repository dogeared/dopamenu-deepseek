package com.example.dopamenu.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;

/**
 * Attaches a dialog to the UI before opening it. A Dialog must be part of the
 * component tree to be rendered; attaching it to the UI root, rather than the
 * view, keeps it alive while the view underneath re-renders.
 */
public final class Dialogs {

    private Dialogs() {
    }

    public static void open(Dialog dialog) {
        UI ui = UI.getCurrent();
        if (ui != null && dialog.getParent().isEmpty()) {
            ui.add(dialog);
            dialog.addClosedListener(event -> ui.remove(dialog));
        }
        dialog.open();
    }
}
