package com.gitranking.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProgrammingLanguage {
    JAVA("java"),
    PYTHON("python"),
    JAVASCRIPT("javascript"),
    TYPESCRIPT("typescript"),
    GO("go"),
    RUST("rust"),
    C("c"),
    CPP("c++"),
    CSHARP("c#"),
    RUBY("ruby"),
    PHP("php"),
    SWIFT("swift"),
    KOTLIN("kotlin"),
    SCALA("scala"),
    SHELL("shell");

    private final String value;

    ProgrammingLanguage(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
