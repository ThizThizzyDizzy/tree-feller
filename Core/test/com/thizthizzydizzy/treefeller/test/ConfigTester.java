package com.thizthizzydizzy.treefeller.test;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.ConfigReader;
import com.thizthizzydizzy.treefeller.core.config.ConfigWriter;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import java.io.IOException;
import java.nio.file.Path;
public class ConfigTester {
    public static void main(String[] args) throws IOException{
        TreeFellerCore.connector = new TestConnector();
        Path configFile = Path.of("test/config.conf");
        System.out.println("Reading Config...");
        TreeFellerConfiguration config = ConfigReader.read(configFile);
        System.out.println("Writing Config...");
        String s1 = ConfigWriter.writeToString(config);
        String s2 = ConfigWriter.writeToString(ConfigReader.readFromString(s1));
        System.out.println("Rewriting Config...");
        if(!s1.equals(s2))throw new AssertionError("Config did not survive write/read/write!");
        System.out.println(s2);
    }
}
