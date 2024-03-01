package com.innocuous.innoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.innocuous.dependencyinjection.servicedata.IStoppable;
import okio.Path;

import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class InnoConfigBase implements IStoppable
{
    public abstract String GetConfigPath();

    public void Init()
    {
        String path = Path.get("") + "/" + GetConfigPath();
        if (!Files.exists(Paths.get(path)))
        {
            for (Field field : getClass().getFields())
            {
                try {
                    System.out.println(field.get(this));
                }
                catch (Exception ex) { throw new RuntimeException(ex); }
            }

            try
            {
                SaveFile(this.getClass().getDeclaredConstructor().newInstance()); //Save a new file
            }
            catch (Exception ex) { throw new RuntimeException(ex); }
            return;
        }

        //Load the file to a new object, grab fields and populate current class
        Object dataObject = LoadFile();
        Field[] dataFields = getClass().getFields();
        try
        {
            for (Field field : dataFields) {
                field.set(this, field.get(dataObject));
            }
        }
        catch (Exception ex) { throw new RuntimeException(ex); }
    }

    public void Shutdown()
    {
        SaveFile(this);
    }

    public void SaveFile(Object reference)
    {
        //Save file
        String path = Path.get("") + "/" + GetConfigPath();
        try
        {
            if (!Files.exists(Paths.get(path)))
            {
                ArrayList<String> dirs = new ArrayList<>(Arrays.stream(path.split("/")).toList());
                dirs.remove(dirs.size()-1);
                String pathDir = String.join("/", dirs);
                Files.createDirectories(Paths.get(pathDir));
                Files.createFile(Paths.get(path));
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

            String jsonString = objectMapper.writeValueAsString(reference);

            FileWriter writer = new FileWriter(path);
            writer.write(jsonString);
            writer.close();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public Object LoadFile()
    {
        String path = Path.get("") + "/" + GetConfigPath();
        try
        {
            byte[] jsonData = Files.readAllBytes(Paths.get(path));
            ObjectMapper objectMapper = new ObjectMapper();
            Object dataObject = objectMapper.readValue(jsonData, getClass());

            return dataObject;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
