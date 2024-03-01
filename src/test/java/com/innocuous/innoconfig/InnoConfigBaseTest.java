package com.innocuous.innoconfig;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InnoConfigBaseTest
{
    @Test
    @Order(0)
    void saveFile() throws IOException
    {
        TestConfig config = new TestConfig();
        Path path = Paths.get(okio.Path.get("") + "/" +config.GetConfigPath());
        config.tested = true;
        config.SaveFile(config);
        assertTrue(Files.exists(path));
        assertTrue(Files.readString(path).contains("\"tested\" : true"));
        Files.deleteIfExists(path);
    }

    @Test
    @Order(1)
    void initConfig()
    {
        TestConfig config = new TestConfig();
        Path path = Paths.get(okio.Path.get("") + "/" +config.GetConfigPath());
        config.Init();
        assertNotNull(config.tested);
        assertTrue(Files.exists(path));
    }

    @Test
    @Order(2)
    void loadFile() throws IOException
    {
        TestConfig config = new TestConfig();
        Path path = Paths.get(okio.Path.get("") + "/" +config.GetConfigPath());
        Object data = config.LoadFile();
        assertNotNull(data);
        assertSame(data.getClass(), TestConfig.class);
        assertNotNull(((TestConfig) data).tested);
        Files.deleteIfExists(path);
    }


}