package io.axoniq.demo.bikerental.rental;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
             .registerType(org.h2.server.TcpServer.class,
                           MemberCategory.PUBLIC_CLASSES,
                           MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                           MemberCategory.INVOKE_DECLARED_METHODS,
                           MemberCategory.INTROSPECT_DECLARED_METHODS,
                           MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS);
    }
}
