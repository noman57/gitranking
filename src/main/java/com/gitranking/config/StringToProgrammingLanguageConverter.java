package com.gitranking.config;

import com.gitranking.model.ProgrammingLanguage;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StringToProgrammingLanguageConverter implements Converter<String, ProgrammingLanguage> {

    @Override
    public ProgrammingLanguage convert(String source) {
        return Arrays.stream(ProgrammingLanguage.values())
                .filter(l -> l.getValue().equalsIgnoreCase(source))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown language: '%s'. Supported values: %s".formatted(
                                source,
                                Arrays.stream(ProgrammingLanguage.values())
                                        .map(ProgrammingLanguage::getValue)
                                        .toList())));
    }
}
