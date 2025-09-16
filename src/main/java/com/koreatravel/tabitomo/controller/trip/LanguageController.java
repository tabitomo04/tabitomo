package vio.tabitomo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import vio.tabitomo.config.AppProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Controller for handling language switching functionality.
 */
@Controller
public class LanguageController {

    private final AppProperties appProperties;

    @Autowired
    public LanguageController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Changes the application's locale based on the requested language.
     * 
     * @param lang The language code to switch to (e.g., 'ko', 'en', 'ja')
     * @param request The HTTP request
     * @param response The HTTP response
     * @return A redirect to the previous page or home page
     */
    @GetMapping("/change-language")
    public String changeLanguage(@RequestParam("lang") String lang,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        // Only change language if it's in our supported languages list
        if (appProperties.isLanguageSupported(lang)) {
            Locale locale = new Locale(lang);
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            
            if (localeResolver != null) {
                localeResolver.setLocale(request, response, locale);
            }
        }
        
        // Redirect back to the previous page or home if referrer is not available
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}
