package io.github.dumijdev.belanova.gateway.admin.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class Card extends Div {

    private final H3 titleLabel;
    private final Icon icon;
    private final VerticalLayout content;

    public Card(String title, Icon icon) {
        addClassName("metric-card");

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("card-header");
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        this.titleLabel = new H3(title);
        this.titleLabel.addClassName("card-title");

        this.icon = icon;
        this.icon.addClassName("card-icon");

        header.add(this.titleLabel, this.icon);

        this.content = new VerticalLayout();
        this.content.addClassName("card-content");
        this.content.setSpacing(false);

        add(header, this.content);
    }

    public void addContent(Component... components) {
        this.content.add(components);
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }
}
