package io.github.dumijdev.belanova.gateway.admin.ui.services;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@UIScope
public class TranslationService {

  private static final String LANGUAGE_SESSION_KEY = "app.language";
  private static final String DEFAULT_LANGUAGE = "en";

  private final Map<String, Map<String, String>> translations = new HashMap<>();

  public TranslationService() {
    loadTranslations();
  }

  private void loadTranslations() {
    // Load translations from properties files
    loadTranslationFile("en", "translations.properties");
    loadTranslationFile("pt", "translations_pt.properties");
    loadTranslationFile("es", "translations_es.properties");
    loadTranslationFile("fr", "translations_fr.properties");
  }

  private void loadTranslationFile(String language, String filename) {
    Map<String, String> languageTranslations = new HashMap<>();

    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
      if (inputStream != null) {
        Properties properties = new Properties();
        properties.load(inputStream);

        for (String key : properties.stringPropertyNames()) {
          languageTranslations.put(key, properties.getProperty(key));
        }
      }
    } catch (IOException e) {
      System.err.println("Error loading translation file: " + filename + " - " + e.getMessage());
    }

    translations.put(language, languageTranslations);
  }

  public String getCurrentLanguage() {
    VaadinSession session = VaadinSession.getCurrent();
    if (session != null) {
      String language = (String) session.getAttribute(LANGUAGE_SESSION_KEY);
      return language != null ? language : DEFAULT_LANGUAGE;
    }
    return DEFAULT_LANGUAGE;
  }

  public void setLanguage(String language) {
    VaadinSession session = VaadinSession.getCurrent();
    if (session != null && translations.containsKey(language)) {
      session.setAttribute(LANGUAGE_SESSION_KEY, language);
    }
  }

  public String getTranslation(String key) {
    String language = getCurrentLanguage();
    Map<String, String> languageMap = translations.get(language);

    if (languageMap != null && languageMap.containsKey(key)) {
      return languageMap.get(key);
    }

    // Fallback to English if key not found
    Map<String, String> englishMap = translations.get("en");
    if (englishMap != null && englishMap.containsKey(key)) {
      return englishMap.get(key);
    }

    // Return key if no translation found
    return key;
  }

  public Map<String, String> getAllTranslations() {
    String language = getCurrentLanguage();
    return translations.getOrDefault(language, translations.get("en"));
  }

  public String[] getSupportedLanguages() {
    return new String[]{"en", "pt", "es", "fr"};
  }

  public String getLanguageDisplayName(String languageCode) {
    switch (languageCode) {
      case "en": return "English";
      case "pt": return "Português";
      case "es": return "Español";
      case "fr": return "Français";
      default: return languageCode;
    }
  }

  public boolean isLanguageSupported(String language) {
    return translations.containsKey(language);
  }

  public String getTranslation(String key, Object... args) {
    String translation = getTranslation(key);
    if (args.length > 0) {
      return String.format(translation, args);
    }
    return translation;
  }

  public void refreshTranslations() {
    translations.clear();
    loadTranslations();
  }
}