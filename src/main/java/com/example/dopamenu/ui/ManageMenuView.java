package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuShare;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuItemService;
import com.example.dopamenu.service.MenuShareService;
import com.example.dopamenu.ui.component.CourseSection;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

@Route(value = "manage", layout = MainLayout.class)
@PageTitle("Edit menu · Dopa Menu")
@PermitAll
public class ManageMenuView extends VerticalLayout {

    private final MenuItemService menuItemService;
    private final MenuShareService shareService;
    private final CurrentUser currentUser;

    public ManageMenuView(MenuItemService menuItemService, MenuShareService shareService,
            CurrentUser currentUser) {
        this.menuItemService = menuItemService;
        this.shareService = shareService;
        this.currentUser = currentUser;
        addClassName("manage-view");
        setPadding(false);
        setSpacing(false);
        render();
    }

    private void render() {
        removeAll();
        List<MenuItem> items = menuItemService.ensureSeeded(currentUser.id());

        add(head());
        add(shareSection());
        for (Course course : Course.values()) {
            add(group(course, items.stream()
                    .filter(item -> item.getCourse() == course)
                    .toList()));
        }
        add(new SiteFooter());
    }

    private Div head() {
        Div head = new Div();
        Css.add(head, "page-head", "manage-head");

        Div copy = new Div();
        copy.addClassName("page-head-copy");
        Span eyebrow = new Span("Your kitchen");
        eyebrow.addClassName("eyebrow");
        H1 title = new H1("Edit the menu.");
        title.addClassName("page-title");
        Paragraph lede = new Paragraph(
                "A good menu is specific. Add the things that reliably work for you "
                        + "and retire the ones that only look good on paper.");
        lede.addClassName("page-lede");
        copy.add(eyebrow, title, lede);

        NativeButton add = new NativeButton();
        Css.add(add, "btn", "btn-primary", "manage-add");
        add.add(VaadinIcon.PLUS.create());
        add.add(new Span("New dish"));
        add.addClickListener(event -> Dialogs.open(new MenuItemFormDialog(menuItemService,
                currentUser.id(), null, this::render)));

        head.add(copy, add);
        return head;
    }

    private Div shareSection() {
        Div section = new Div();
        section.addClassName("manage-share");

        Div copy = new Div();
        copy.addClassName("manage-share-copy");
        Span eyebrow = new Span("Share");
        eyebrow.addClassName("eyebrow");
        H2 title = new H2("Let someone borrow your menu.");
        title.addClassName("manage-share-title");
        Paragraph lede = new Paragraph(
                "The link shows your menu without an account. Anyone who orders or copies "
                        + "a dish is asked to sign in first.");
        lede.addClassName("manage-share-lede");
        copy.add(eyebrow, title, lede);

        Div body = new Div();
        body.addClassName("manage-share-body");
        Optional<MenuShare> active = shareService.activeFor(currentUser.id());
        if (active.isPresent()) {
            body.add(activeShare(active.get()));
        } else {
            body.add(noShare());
        }

        section.add(copy, body);
        return section;
    }

    private Div noShare() {
        Div block = new Div();
        block.addClassName("share-setup");

        TextField name = authorNameField(currentUser.displayName());
        NativeButton create = new NativeButton();
        Css.add(create, "btn", "btn-primary");
        create.add(VaadinIcon.LINK.create());
        create.add(new Span("Create link"));
        create.addClickListener(event -> {
            if (!validName(name)) {
                return;
            }
            try {
                shareService.create(currentUser.id(), name.getValue());
                Notification.show("Your menu link is ready.");
                render();
            } catch (MenuShareService.ActiveShareException alreadyLive) {
                Notification.show("You already have a live link. Rotate it instead.");
                render();
            } catch (DataIntegrityViolationException race) {
                Notification.show("A link was just created. Refresh to see it.");
                render();
            } catch (IllegalArgumentException invalid) {
                name.setInvalid(true);
                name.setErrorMessage(invalid.getMessage());
            }
        });

        block.add(name, create);
        return block;
    }

    private Div activeShare(MenuShare share) {
        Div block = new Div();
        block.addClassName("share-active");

        TextField url = new TextField("Your link");
        url.addClassName("share-url");
        url.setValue(absoluteUrl(share));
        url.setReadOnly(true);

        Div linkRow = new Div();
        linkRow.addClassName("share-row");
        NativeButton copy = new NativeButton();
        Css.add(copy, "btn", "btn-ghost");
        copy.add(VaadinIcon.COPY.create());
        copy.add(new Span("Copy link"));
        copy.addClickListener(event -> {
            UI ui = UI.getCurrent();
            if (ui != null) {
                ui.getPage().executeJs("navigator.clipboard.writeText($0)", url.getValue());
            }
            Notification.show("Link copied.");
        });
        linkRow.add(url, copy);

        TextField name = authorNameField(share.getDisplayName());
        Div rotateRow = new Div();
        rotateRow.addClassName("share-row");
        NativeButton rotate = new NativeButton();
        Css.add(rotate, "btn", "btn-ghost");
        rotate.add(VaadinIcon.REFRESH.create());
        rotate.add(new Span("Rotate link"));
        rotate.addClickListener(event -> {
            if (!validName(name)) {
                return;
            }
            try {
                shareService.rotate(currentUser.id(), name.getValue());
                Notification.show("The old link is dead. Here’s the new one.");
                render();
            } catch (DataIntegrityViolationException race) {
                Notification.show("Another tab changed the link. Refresh to see it.");
                render();
            } catch (IllegalArgumentException invalid) {
                name.setInvalid(true);
                name.setErrorMessage(invalid.getMessage());
            }
        });
        rotateRow.add(name, rotate);

        NativeButton revoke = new NativeButton();
        Css.add(revoke, "btn", "btn-quiet", "is-danger");
        revoke.add(VaadinIcon.TRASH.create());
        revoke.add(new Span("Revoke link"));
        revoke.addClickListener(event -> {
            shareService.revoke(currentUser.id(), share.getId());
            Notification.show("The link is revoked. Anyone who has it sees the dead page.");
            render();
        });

        block.add(linkRow, rotateRow, revoke);
        return block;
    }

    private static TextField authorNameField(String value) {
        TextField name = new TextField("Your name on the link");
        name.addClassName("share-name");
        name.setValue(value);
        name.setMaxLength(MenuShareService.DISPLAY_NAME_LIMIT);
        name.setHelperText("Shown at the top of the public menu.");
        return name;
    }

    private static boolean validName(TextField name) {
        String value = name.getValue() == null ? "" : name.getValue().trim();
        if (value.isEmpty()) {
            name.setInvalid(true);
            name.setErrorMessage("Add the name you want on the link.");
            return false;
        }
        return true;
    }

    private static String absoluteUrl(MenuShare share) {
        String path = "/" + ShareRoutes.PREFIX + "/" + share.getToken();
        if (VaadinRequest.getCurrent() instanceof VaadinServletRequest request) {
            HttpServletRequest http = request.getHttpServletRequest();
            String scheme = http.getScheme();
            String host = http.getServerName();
            if (scheme != null && host != null && !host.isBlank()) {
                StringBuilder url = new StringBuilder(scheme).append("://").append(host);
                int port = http.getServerPort();
                boolean defaultPort = ("https".equals(scheme) && port == 443)
                        || ("http".equals(scheme) && port == 80);
                if (port > 0 && !defaultPort) {
                    url.append(':').append(port);
                }
                return url.append(path).toString();
            }
        }
        return path;
    }

    private Div group(Course course, List<MenuItem> items) {
        Div group = new Div();
        group.addClassName("manage-group");
        group.addClassName("course-" + course.cssName());

        Div head = new Div();
        head.addClassName("manage-group-head");
        Div iconWrap = new Div();
        Css.add(iconWrap, "course-icon", "course-icon-small");
        Icon icon = CourseSection.iconFor(course).create();
        iconWrap.add(icon);
        H2 title = new H2(course.label());
        title.addClassName("manage-group-title");
        Span count = new Span(items.size() + (items.size() == 1 ? " item" : " items"));
        count.addClassName("manage-count");
        head.add(iconWrap, title, count);
        group.add(head);

        if (items.isEmpty()) {
            Div empty = new Div();
            empty.addClassName("manage-empty");
            empty.add(new Span("Nothing on this course yet."));
            group.add(empty);
            return group;
        }

        for (MenuItem item : items) {
            group.add(row(item));
        }
        return group;
    }

    private Div row(MenuItem item) {
        Div row = new Div();
        row.addClassName("manage-row");

        Div main = new Div();
        main.addClassName("manage-main");
        Span title = new Span(item.getTitle());
        title.addClassName("manage-title");
        main.add(title);
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            Span description = new Span(item.getDescription());
            description.addClassName("manage-description");
            main.add(description);
        }

        Div meta = new Div();
        meta.addClassName("manage-meta");
        meta.add(new Span(UiText.minutes(item.getDurationMinutes())));
        meta.add(new Span(item.getDopamineLevel().label()));
        if (item.isTimerRequired()) {
            Span timer = new Span("Timer");
            timer.addClassName("mini-pill");
            meta.add(timer);
        }

        Div actions = new Div();
        actions.addClassName("manage-actions");
        NativeButton edit = iconButton(VaadinIcon.PENCIL, "Edit " + item.getTitle());
        edit.addClickListener(event -> Dialogs.open(new MenuItemFormDialog(menuItemService,
                currentUser.id(), item, this::render)));
        NativeButton remove = iconButton(VaadinIcon.TRASH, "Remove " + item.getTitle());
        remove.addClassName("is-danger");
        remove.addClickListener(event -> {
            menuItemService.archive(currentUser.id(), item.getId());
            Notification.show("Removed “" + item.getTitle() + "” from your menu.");
            render();
        });
        actions.add(edit, remove);

        row.add(main, meta, actions);
        return row;
    }

    private static NativeButton iconButton(VaadinIcon icon, String label) {
        NativeButton button = new NativeButton();
        button.addClassName("icon-button");
        button.setAriaLabel(label);
        button.setTitle(label);
        Icon graphic = icon.create();
        button.add(graphic);
        return button;
    }
}
