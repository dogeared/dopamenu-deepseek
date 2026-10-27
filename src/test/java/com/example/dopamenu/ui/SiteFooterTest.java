package com.example.dopamenu.ui;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Footer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.dopamenu.ui.UiTestSupport.findAll;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;

class SiteFooterTest {

    @BeforeEach
    void setUp() {
        UiTestSupport.installUi();
    }

    @AfterEach
    void tearDown() {
        UiTestSupport.removeUi();
    }

    @Test
    void showsTheCreditAndLinksDogearedToGithub() {
        SiteFooter footer = new SiteFooter();

        assertThat(hasText(footer, "made with ❤️ by dogeared")).isTrue();
        assertThat(footer.getClassNames()).contains("view-footer", "site-footer");

        Anchor author = findAll(footer, Anchor.class).getFirst();
        assertThat(author.getText()).isEqualTo("dogeared");
        assertThat(author.getHref()).isEqualTo(SiteFooter.GITHUB_URL);
        assertThat(author.getTarget()).contains("_blank");
        assertThat(author.getClassNames()).contains("inline-link");
    }

    @Test
    void isAFooterElement() {
        assertThat(new SiteFooter()).isInstanceOf(Footer.class);
    }
}
