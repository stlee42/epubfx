package de.machmireinebook.epubeditor.spellcheck;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.languagetool.JLanguageTool;
import org.languagetool.ResultCache;
import org.languagetool.UserConfig;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.CategoryIds;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import de.machmireinebook.epubeditor.preferences.PreferencesManager;

/**
 * @author Michail Jungierek
 */
public class SpellcheckManager {
    private static final Logger logger = Logger.getLogger(SpellcheckManager.class);

    private JLanguageTool langTool;
    private static final ResultCache CACHE = new ResultCache(10000, 1, TimeUnit.HOURS);;

    @Inject
    private PreferencesManager preferencesManager;

    @PostConstruct
    public void init() {
        UserConfig config = new UserConfig(Collections.emptyList(), Collections.emptyMap(), 5);
        langTool = new JLanguageTool(preferencesManager.getLanguageSpellSelection().getLanguage(), null, CACHE, config);
        if (preferencesManager.isOnlyDictionaryBasedSpellCheck()) {
            for (Rule rule : langTool.getAllRules()) {
                if (!rule.isDictionaryBasedSpellingRule()) {
                    langTool.disableRule(rule.getId());
                }
            }
        }
        langTool.disableCategory(CategoryIds.TYPOGRAPHY);
        //langTool.disableCategory(CategoryIds.CONFUSED_WORDS);
        langTool.disableCategory(CategoryIds.REDUNDANCY);
        langTool.disableCategory(CategoryIds.STYLE);
        langTool.disableCategory(CategoryIds.GENDER_NEUTRALITY);
        langTool.disableCategory(CategoryIds.SEMANTICS);
        langTool.disableCategory(CategoryIds.COLLOQUIALISMS);
        langTool.disableCategory(CategoryIds.WIKIPEDIA);
        langTool.disableCategory(CategoryIds.BARBARISM);
        langTool.disableCategory(CategoryIds.MISC);
        logger.info("langTool created");
    }

    public List<RuleMatch> check(String text) throws IOException {
        return langTool.check(text);
    }

    public List<RuleMatch> check(AnnotatedText text) throws IOException {
        //using paragraph rules gives to much false positives if paragraph is not formatted in one single code area line
        return langTool.check(text, true, JLanguageTool.ParagraphHandling.ONLYNONPARA);

    }

    public void shutdown() {
        //langTool.shutdown();
    }
}
