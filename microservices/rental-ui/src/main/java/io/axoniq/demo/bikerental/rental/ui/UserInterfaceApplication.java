package io.axoniq.demo.bikerental.rental.ui;

import com.thoughtworks.xstream.XStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserInterfaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserInterfaceApplication.class, args);
	}

	@Autowired
	public void configureXStreamSecurity(XStream xStream) {
		xStream.allowTypesByWildcard(new String[]{"io.axoniq.demo.bikerental.coreapi.**"});
	}

}
