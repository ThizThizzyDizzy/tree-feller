package com.thizthizzydizzy.treefeller.architect;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Architect{
    private static final String JAVA_VERSION = "1.8";
    public static void main(String[] args) throws Exception{
        System.out.println("Finding projects...");
        List<File> projectFolders = new ArrayList<>();
        projectFolders.addAll(findAllProjects(new File("..")));
        System.out.println("Found "+projectFolders.size()+" projects.");

        for(File projectDir : projectFolders){
            File propertiesFile = new File(projectDir, "nbproject/project.properties");
            if(!propertiesFile.exists())return;

            String properties = Files.readString(propertiesFile.toPath());

            var currentSourceVer = extractProperty(properties, "javac.source");
            var currentTargetVer = extractProperty(properties, "javac.target");

            // Validate against blueprint specs
            boolean sourceMismatch = currentSourceVer!=null&&!currentSourceVer.equals(JAVA_VERSION);
            boolean targetMismatch = currentTargetVer!=null&&!currentTargetVer.equals(JAVA_VERSION);
            if(sourceMismatch||targetMismatch){
                System.out.println(projectDir.getAbsolutePath()+" - Not Fine");

                if(sourceMismatch){
                    System.out.println("Changing javac.source from "+currentSourceVer+" to "+JAVA_VERSION);
                    properties = Pattern.compile("^javac.source=.+$", Pattern.MULTILINE)
                        .matcher(properties).replaceAll("javac.source="+JAVA_VERSION);
                }
                if(targetMismatch){
                    System.out.println("Changing javac.target from "+currentTargetVer+" to "+JAVA_VERSION);
                    properties = Pattern.compile("^javac.target=.+$", Pattern.MULTILINE)
                        .matcher(properties).replaceAll("javac.target="+JAVA_VERSION);
                }

                Files.writeString(propertiesFile.toPath(), properties, StandardCharsets.UTF_8);
                System.out.println(projectDir.getAbsolutePath()+" - Now Fine");
            }else if(currentSourceVer!=null){
                System.out.println(projectDir.getAbsolutePath()+" - Fine");
            }
        }
    }
    private static String extractProperty(String content, String key){
        Matcher matcher = Pattern.compile("^"+key+"=(.+)$", Pattern.MULTILINE).matcher(content);
        return matcher.find()?matcher.group(1).trim():null;
    }
    private static List<File> findAllProjects(File file){
        List<File> projects = new ArrayList<>();
        for(File f : file.listFiles()){
            if(f.isDirectory()&&!f.getName().startsWith(".")&&!f.getName().equals("Architect")){
                if(new File(f, "nbproject/project.xml").exists()){
                    projects.add(f);
                }
                projects.addAll(findAllProjects(f));
            }
        }
        return projects;
    }
}
