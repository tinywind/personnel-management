package org.tinywind.pm;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.tinywind.pm.model.Person;
import org.tinywind.pm.model.PersonRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class PersonContractTest {
    private static ConfigurableApplicationContext application;
    private static String base;

    @BeforeAll
    public static void startIsolatedApplication() {
        application = SpringApplication.run(Launcher.class, "--server.port=0", "--server.address=127.0.0.1",
                "--spring.datasource.url=jdbc:h2:mem:personnel-contract;DB_CLOSE_DELAY=-1", "--spring.jpa.hibernate.ddl-auto=create-drop");
        base = "http://127.0.0.1:" + application.getEnvironment().getProperty("local.server.port");
    }

    @AfterAll
    public static void stopIsolatedApplication() {
        if (application != null) application.close();
    }

    @Test
    public void repositoryPreservesNameAndImmutableCreationTime() {
        PersonRepository repository = application.getBean(PersonRepository.class);
        EntityManager manager = application.getBean(EntityManagerFactory.class).createEntityManager();
        try {
            Person person = new Person();
            person.setName("O'Reilly");
            Date created = new Date(1700000000000L);
            person.setCreatedAt(created);
            Person saved = repository.saveAndFlush(person);
            Long id = saved.getId();
            assertNotNull(id);
            Person loaded = manager.find(Person.class, id);
            assertEquals("O'Reilly", loaded.getName());
            assertEquals(created.getTime(), loaded.getCreatedAt().getTime());
            saved.setName("Updated");
            saved.setCreatedAt(new Date(1800000000000L));
            repository.saveAndFlush(saved);
            manager.clear();
            loaded = manager.find(Person.class, id);
            assertEquals("Updated", loaded.getName());
            assertEquals(created.getTime(), loaded.getCreatedAt().getTime());
            repository.delete(saved);
            repository.flush();
            manager.clear();
            assertNull(manager.find(Person.class, id));
        } finally {
            manager.close();
        }
    }

    @Test
    public void helloRendersTheThymeleafTemplate() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(base + "/hello").openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        try {
            assertEquals(200, connection.getResponseCode());
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (InputStream input = connection.getInputStream()) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = input.read(buffer)) != -1) bytes.write(buffer, 0, length);
            }
            String html = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            assertTrue(html.contains("Hello!"));
            assertTrue(html.contains("tinywind"));
            assertFalse(html.contains("th:text"));
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void developmentConsoleIsNotExposed() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(base + "/h2-console/").openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        try {
            assertEquals(404, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }
}
