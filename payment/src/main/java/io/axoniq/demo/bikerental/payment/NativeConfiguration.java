package io.axoniq.demo.bikerental.payment;

import org.h2.server.TcpServer;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@ImportRuntimeHints(NativeConfiguration.CustomRuntimeHints.class)
@Configuration
public class NativeConfiguration {

    public static class CustomRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Allows running h2 in Server Mode in Native
            hints.reflection()
                 .registerType(TcpServer.class,
                               MemberCategory.PUBLIC_CLASSES,
                               MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                               MemberCategory.INVOKE_DECLARED_METHODS);
        }
    }
}
