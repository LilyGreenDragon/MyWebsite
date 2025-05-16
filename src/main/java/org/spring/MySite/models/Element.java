package org.spring.MySite.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Element {
    private final String id;
    private final String name;
    private final Type type;

    public static enum Type {SOUP, DISH, GARNISH, SAUCE}
}
