package xyz.voroniyx.deathtotem.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class JsonConfigManager<T> {

    private final Gson gson;
    private final Class<T> configClass;
    private final File file;
    private T data;

    public JsonConfigManager(Class<T> configClass, String filePath) {
        this.configClass = configClass;
        this.file = new File(filePath);

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    public void save() throws IOException {
        if (data == null) return;

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        }
    }

    public boolean saveSave() {
        try {
            save();
        }catch (Exception ex) {
            return false;
        }
        return true;
    }

    public void load() throws IOException {
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                this.data = gson.fromJson(reader, configClass);
            }
        } else {
            try {
                this.data = configClass.getDeclaredConstructor().newInstance();
                save();
            } catch (Exception e) {
                throw new IOException("Failed to create default config", e);
            }
        }
    }

    public T getData() { return data; }
}