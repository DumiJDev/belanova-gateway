// ThemeService.java
package io.github.dumijdev.belanova.gateway.admin.ui.services;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.stereotype.Service;

@Service
@UIScope
public class ThemeService {

  private static final String THEME_SESSION_KEY = "app.theme";
  private static final String DEFAULT_THEME = Lumo.LIGHT;

  public String getCurrentTheme() {
    VaadinSession session = VaadinSession.getCurrent();
    if (session != null) {
      String theme = (String) session.getAttribute(THEME_SESSION_KEY);
      return theme != null ? theme : DEFAULT_THEME;
    }
    return DEFAULT_THEME;
  }

  public void setTheme(String theme) {
    VaadinSession session = VaadinSession.getCurrent();
    if (session != null) {
      session.setAttribute(THEME_SESSION_KEY, theme);
    }
  }

  public boolean isDarkMode() {
    return Lumo.DARK.equals(getCurrentTheme());
  }

  public void toggleTheme() {
    setTheme(isDarkMode() ? Lumo.LIGHT : Lumo.DARK);
  }
}