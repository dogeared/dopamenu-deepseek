package com.example.dopamenu.ui.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

/**
 * A five-point feeling picker built from native buttons, so it can be styled
 * with plain external CSS.
 */
public class MoodPicker extends Div {

    private static final String[] EMOJI = { "😖", "😕", "😐", "🙂", "😄" };
    private static final String[] WORDS = { "Rough", "Low", "Okay", "Good", "Great" };

    private final List<NativeButton> buttons = new ArrayList<>();
    private int value;

    public MoodPicker(String label, int initialValue) {
        addClassName("mood-picker");
        Span caption = new Span(label);
        caption.addClassName("field-label");
        add(caption);

        Div options = new Div();
        options.addClassName("mood-options");
        for (int i = 1; i <= 5; i++) {
            int mood = i;
            NativeButton button = new NativeButton();
            button.addClassName("mood-option");
            button.setAriaLabel(WORDS[i - 1]);
            button.setTitle(WORDS[i - 1]);
            Span emoji = new Span(EMOJI[i - 1]);
            emoji.addClassName("mood-emoji");
            Span word = new Span(WORDS[i - 1]);
            word.addClassName("mood-word");
            button.add(emoji, word);
            button.addClickListener(event -> setValue(mood));
            buttons.add(button);
            options.add(button);
        }
        add(options);
        setValue(initialValue);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = Math.max(1, Math.min(5, value));
        for (int i = 0; i < buttons.size(); i++) {
            NativeButton button = buttons.get(i);
            boolean selected = i + 1 == this.value;
            button.getElement().getClassList().set("is-selected", selected);
            button.getElement().setAttribute("aria-pressed", Boolean.toString(selected));
        }
    }
}
