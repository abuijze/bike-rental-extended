package io.axoniq.demo.bikerental.rental.command;

import com.thoughtworks.xstream.XStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RentalCommandApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalCommandApplication.class, args);
	}

	@Autowired
	public void configureXStreamSecurity(XStream xStream) {
		xStream.allowTypesByWildcard(new String[]{"io.axoniq.demo.bikerental.coreapi.**"});
	}

}
