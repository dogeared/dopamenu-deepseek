package com.example.dopamenu.ui;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CssAndDialogsTest {

    @BeforeEach
    void setUp() {
        UiTestSupport.installUi();
    }

    @AfterEach
    void tearDown() {
        UiTestSupport.removeUi();
    }

    @Test
    void cssAddsEveryClassName() {
        Div div = new Div();

        Css.add(div, "one", "two", "three");

        assertThat(div.getClassNames()).containsExactlyInAnyOrder("one", "two", "three");
    }

    @Test
    void openingADialogAttachesItToTheUi() {
        UI ui = UI.getCurrent();
        Dialog dialog = new Dialog();

        Dialogs.open(dialog);

        assertThat(dialog.isOpened()).isTrue();
        assertThat(ui.getChildren()).contains(dialog);
    }

    @Test
    void closingADialogDetachesItAgain() {
        UI ui = UI.getCurrent();
        Dialog dialog = new Dialog();
        Dialogs.open(dialog);

        // The client fires ClosedEvent when the user dismisses the overlay;
        // close() alone only flips the opened property.
        ComponentUtil.fireEvent(dialog, new Dialog.ClosedEvent(dialog, true));

        assertThat(ui.getChildren()).doesNotContain(dialog);
    }

    @Test
    void openingAnAlreadyAttachedDialogKeepsOneParent() {
        UI ui = UI.getCurrent();
        Dialog dialog = new Dialog();

        Dialogs.open(dialog);
        Dialogs.open(dialog);

        assertThat(ui.getChildren().filter(child -> child == dialog)).hasSize(1);
        assertThat(dialog.isOpened()).isTrue();
    }
}
