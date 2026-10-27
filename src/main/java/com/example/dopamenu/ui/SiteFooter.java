package com.example.dopamenu.ui;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Span;

/**
 * The footer shown at the bottom of every page.
 */
public class SiteFooter extends Footer {

    public static final String GITHUB_URL = "https://github.com/dogeared/dopamenu-deepseek";

    public SiteFooter() {
        addClassName("view-footer");
        addClassName("site-footer");
        Span text = new Span("made with ❤️ by ");
        Anchor author = new Anchor(GITHUB_URL, "dogeared");
        author.setTarget("_blank");
        author.addClassName("inline-link");
        add(text, author);
    }
}
