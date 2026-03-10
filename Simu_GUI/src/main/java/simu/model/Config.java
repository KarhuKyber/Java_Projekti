package simu.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private final Properties props = new Properties();

    public Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("casino.properties")) {
            if (input == null) {
                throw new RuntimeException("Config file casino.properties not found");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config file", e);
        }
    }


    public double getDouble(String key) {
        return Double.parseDouble(props.getProperty(key));
    }
}