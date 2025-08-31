package io.github.dumijdev.belanova.gateway.admin.ui.services;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@UIScope
public class TranslationService {

  private static final String LANGUAGE_SESSION_KEY = "app.language";
  private static final String DEFAULT_LANGUAGE = "en";

  private final Map<String, Map<String, String>> translations = new HashMap<>();

  public TranslationService() {
    initializeTranslations();
  }

  private void initializeTranslations() {
    // English translations
    Map<String, String> en = new HashMap<>();
    en.put("app.name", "Belanova Gateway");
    en.put("menu.toggle", "Toggle menu");
    en.put("menu.user", "User menu");
    en.put("theme.light", "Switch to light mode");
    en.put("theme.dark", "Switch to dark mode");
    en.put("language.en", "English");
    en.put("language.pt", "Português");
    en.put("language.es", "Español");
    en.put("language.fr", "Français");
    en.put("navigation.title", "Navigation");
    en.put("navigation.dashboard", "Dashboard");
    en.put("navigation.backends", "Backends");
    en.put("navigation.services", "Services");
    en.put("navigation.upstreams", "Upstreams");
    en.put("navigation.routes", "Routes");
    en.put("navigation.health", "Health Checks");
    en.put("navigation.plugins", "Plugins");
    en.put("navigation.monitoring", "Monitoring");
    en.put("navigation.settings", "Settings");
    en.put("common.add", "Add");
    en.put("common.edit", "Edit");
    en.put("common.delete", "Delete");
    en.put("common.save", "Save");
    en.put("common.cancel", "Cancel");
    en.put("common.search", "Search");
    en.put("common.refresh", "Refresh");
    en.put("common.export", "Export");
    en.put("common.import", "Import");
    en.put("common.loading", "Loading...");
    en.put("common.no.data", "No data available");
    en.put("status.healthy", "Healthy");
    en.put("status.unhealthy", "Unhealthy");
    en.put("status.enabled", "Enabled");
    en.put("status.disabled", "Disabled");

    // Portuguese translations
    Map<String, String> pt = new HashMap<>();
    pt.put("app.name", "Belanova Gateway");
    pt.put("menu.toggle", "Alternar menu");
    pt.put("menu.user", "Menu do usuário");
    pt.put("theme.light", "Mudar para modo claro");
    pt.put("theme.dark", "Mudar para modo escuro");
    pt.put("language.en", "English");
    pt.put("language.pt", "Português");
    pt.put("language.es", "Español");
    pt.put("language.fr", "Français");
    pt.put("navigation.title", "Navegação");
    pt.put("navigation.dashboard", "Dashboard");
    pt.put("navigation.backends", "Backends");
    pt.put("navigation.services", "Serviços");
    pt.put("navigation.upstreams", "Upstreams");
    pt.put("navigation.routes", "Rotas");
    pt.put("navigation.health", "Verificações de Saúde");
    pt.put("navigation.plugins", "Plugins");
    pt.put("navigation.monitoring", "Monitoramento");
    pt.put("navigation.settings", "Configurações");
    pt.put("common.add", "Adicionar");
    pt.put("common.edit", "Editar");
    pt.put("common.delete", "Excluir");
    pt.put("common.save", "Salvar");
    pt.put("common.cancel", "Cancelar");
    pt.put("common.search", "Pesquisar");
    pt.put("common.refresh", "Atualizar");
    pt.put("common.export", "Exportar");
    pt.put("common.import", "Importar");
    pt.put("common.loading", "Carregando...");
    pt.put("common.no.data", "Nenhum dado disponível");
    pt.put("status.healthy", "Saudável");
    pt.put("status.unhealthy", "Não saudável");
    pt.put("status.enabled", "Habilitado");
    pt.put("status.disabled", "Desabilitado");

    // Spanish translations
    Map<String, String> es = new HashMap<>();
    es.put("app.name", "Belanova Gateway");
    es.put("menu.toggle", "Alternar menú");
    es.put("menu.user", "Menú de usuario");
    es.put("theme.light", "Cambiar a modo claro");
    es.put("theme.dark", "Cambiar a modo oscuro");
    es.put("language.en", "English");
    es.put("language.pt", "Português");
    es.put("language.es", "Español");
    es.put("language.fr", "Français");
    es.put("navigation.title", "Navegación");
    es.put("navigation.dashboard", "Panel");
    es.put("navigation.backends", "Backends");
    es.put("navigation.services", "Servicios");
    es.put("navigation.upstreams", "Upstreams");
    es.put("navigation.routes", "Rutas");
    es.put("navigation.health", "Verificaciones de Salud");
    es.put("navigation.plugins", "Plugins");
    es.put("navigation.monitoring", "Monitoreo");
    es.put("navigation.settings", "Configuraciones");
    es.put("common.add", "Agregar");
    es.put("common.edit", "Editar");
    es.put("common.delete", "Eliminar");
    es.put("common.save", "Guardar");
    es.put("common.cancel", "Cancelar");
    es.put("common.search", "Buscar");
    es.put("common.refresh", "Actualizar");
    es.put("common.export", "Exportar");
    es.put("common.import", "Importar");
    es.put("common.loading", "Cargando...");
    es.put("common.no.data", "No hay datos disponibles");
    es.put("status.healthy", "Saludable");
    es.put("status.unhealthy", "No saludable");
    es.put("status.enabled", "Habilitado");
    es.put("status.disabled", "Deshabilitado");

    // French translations
    Map<String, String> fr = new HashMap<>();
    fr.put("app.name", "Belanova Gateway");
    fr.put("menu.toggle", "Basculer le menu");
    fr.put("menu.user", "Menu utilisateur");
    fr.put("theme.light", "Passer en mode clair");
    fr.put("theme.dark", "Passer en mode sombre");
    fr.put("language.en", "English");
    fr.put("language.pt", "Português");
    fr.put("language.es", "Español");
    fr.put("language.fr", "Français");
    fr.put("navigation.title", "Navigation");
    fr.put("navigation.dashboard", "Tableau de bord");
    fr.put("navigation.backends", "Backends");
    fr.put("navigation.services", "Services");
    fr.put("navigation.upstreams", "Upstreams");
    fr.put("navigation.routes", "Routes");
    fr.put("navigation.health", "Vérifications de santé");
    fr.put("navigation.plugins", "Plugins");
    fr.put("navigation.monitoring", "Surveillance");
    fr.put("navigation.settings", "Paramètres");
    fr.put("common.add", "Ajouter");
    fr.put("common.edit", "Modifier");
    fr.put("common.delete", "Supprimer");
    fr.put("common.save", "Enregistrer");
    fr.put("common.cancel", "Annuler");
    fr.put("common.search", "Rechercher");
    fr.put("common.refresh", "Actualiser");
    fr.put("common.export", "Exporter");
    fr.put("common.import", "Importer");
    fr.put("common.loading", "Chargement...");
    fr.put("common.no.data", "Aucune donnée disponible");
    fr.put("status.healthy", "En bonne santé");
    fr.put("status.unhealthy", "Pas en bonne santé");
    fr.put("status.enabled", "Activé");
    fr.put("status.disabled", "Désactivé");

    translations.put("en", en);
    translations.put("pt", pt);
    translations.put("es", es);
    translations.put("fr", fr);
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
}