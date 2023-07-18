package io.axoniq.demo.bikerental.payment;

import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import io.axoniq.demo.bikerental.coreapi.payment.ConfirmPaymentCommand;
import io.axoniq.demo.bikerental.coreapi.payment.RejectPaymentCommand;
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
