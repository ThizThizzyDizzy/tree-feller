package com.thizthizzydizzy.treefeller.architect;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

/**
 * ARCHITECT: The master governor of the TreeFeller ecosystem.
 * Enforcing structural integrity and retrofitting missing components.
 * 
 * Written unapologetically by AI
 */
public class Architect {

    private static final List<ProjectSite> pendingRetrofits = new ArrayList<>();

    public static void main(String[] args) throws BlueprintViolationException {
        File siteRoot = new File("..");

        System.out.println("==========================================================");
        System.out.println("   ARCHITECT: COMMENCING GLOBAL SITE INSPECTION          ");
        System.out.println("   TIMESTAMP: " + new java.util.Date());
        System.out.println("==========================================================");

        System.out.println("[LOG] Scanning terrain for nbproject footprints...");
        List<File> projectFolders = Blueprint.findAllProjects(siteRoot);
        System.out.println("[LOG] Located " + projectFolders.size() + " active sectors.");

        // Build dependency lattice
        System.out.println("[LOG] Mapping dependency lattice...");
        DependencyGraph graph = Blueprint.buildDependencyGraph(projectFolders);
        Set<String> sharedDeps = graph.getSharedDependencies();
        
        if (!sharedDeps.isEmpty()) {
            System.out.println("[SCAN] Shared foundations detected:");
            for (String dep : sharedDeps) {
                int count = graph.getDependentCount(dep);
                System.out.println("  [⚠] '" + dep + "' (" + count + " references)");
            }
        }

        // PHASE 0: Blueprint Validation - Builder must contain all shared dependencies
        validateBuilderBlueprint(projectFolders, sharedDeps);
        System.out.println("[✔] Blueprint validated. Builder contains all shared dependencies.");

        // PHASE 1: Foundation Assessment
        List<ProjectSite> allSites = new ArrayList<>();
        int foundationIssues = 0;
        int structuralIssues = 0;

        for (File folder : projectFolders) {
            ProjectSite site = Inspector.inspectSite(folder, siteRoot, sharedDeps);
            allSites.add(site);
            if (site.needsVersionFix) foundationIssues++;
            if (!site.missingDeps.isEmpty()) structuralIssues++;
        }

        // PHASE 2: Foundation Reinforcement (Must happen first!)
        if (foundationIssues > 0) {
            System.out.println("\n[LOG] Foundation assessment complete. " + foundationIssues + " sites require stabilization.");
            System.out.println("=== COMMENCING FOUNDATION PHASE ===");
            for (ProjectSite site : allSites) {
                if (site.needsVersionFix) {
                    Foreman.stabilizeFoundation(site);
                }
            }
        }

        // PHASE 3: Structural Retrofitting
        if (structuralIssues > 0) {
            System.out.println("\n[LOG] Structural assessment complete. " + structuralIssues + " sites require retrofitting.");
            System.out.println("=== COMMENCING RETROFIT PHASE ===");
            for (ProjectSite site : allSites) {
                if (!site.missingDeps.isEmpty()) {
                    Foreman.retrofit(site);
                }
            }
        }

        if (foundationIssues == 0 && structuralIssues == 0) {
            System.out.println("\n[LOG] All sectors are to spec. No intervention required.");
        }

        System.out.println("\n==========================================================");
        System.out.println("   SITE INSPECTION CONCLUDED. ALL SYSTEMS NOMINAL.       ");
        System.out.println("==========================================================");
    }
    
    private static void validateBuilderBlueprint(List<File> projectFolders, Set<String> sharedDeps) throws BlueprintViolationException {
        File builderDir = null;
        for (File folder : projectFolders) {
            if (folder.getName().equals("Builder")) {
                builderDir = folder;
                break;
            }
        }
        
        if (builderDir == null) {
            throw new BlueprintViolationException("Builder project not found in site!");
        }
        
        try {
            File projectXml = new File(builderDir, "nbproject/project.xml");
            String xmlContent = Files.readString(projectXml.toPath());
            
            Set<String> builderDeps = new HashSet<>();
            Matcher depMatcher = Pattern.compile("<foreign-project>([^<]+)</foreign-project>").matcher(xmlContent);
            while (depMatcher.find()) {
                builderDeps.add(depMatcher.group(1).trim());
            }
            
            // Check for missing shared dependencies
            Set<String> missingShared = new HashSet<>(sharedDeps);
            missingShared.removeAll(builderDeps);
            
            if (!missingShared.isEmpty()) {
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("ARCHITECT: Blueprint violation detected!\n");
                errorMsg.append("The following shared dependencies MUST be declared in Builder's project.xml:\n");
                for (String dep : missingShared) {
                    errorMsg.append("  - ").append(dep).append("\n");
                }
                errorMsg.append("HALTING CONSTRUCTION.");
                throw new BlueprintViolationException(errorMsg.toString());
            }
            
        } catch (IOException e) {
            throw new BlueprintViolationException("Failed to read Builder project.xml: " + e.getMessage());
        }
    }

    public static void queueRetrofit(ProjectSite site) {
        pendingRetrofits.add(site);
    }
}

class ProjectSite {
    File dir;
    String pathName;
    Set<String> missingDeps;
    boolean needsVersionFix;
    String currentSourceVer;
    String currentTargetVer;
    String projectName;
    Set<String> declaredDeps;
    ProjectSite(File d, String p, Set<String> m) { 
        dir = d; pathName = p; missingDeps = m; 
        needsVersionFix = false;
        currentSourceVer = null;
        currentTargetVer = null;
        projectName = d.getName();
        declaredDeps = new HashSet<>();
    }
}

class DependencyGraph {
    private Map<String, Set<String>> projectToDeps = new HashMap<>();
    private Map<String, Set<String>> depToProjects = new HashMap<>();

    public void addDependency(String project, String dependency) {
        projectToDeps.computeIfAbsent(project, k -> new HashSet<>()).add(dependency);
        depToProjects.computeIfAbsent(dependency, k -> new HashSet<>()).add(project);
    }

    public Set<String> getSharedDependencies() {
        Set<String> shared = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : depToProjects.entrySet()) {
            if (entry.getValue().size() > 1) {
                shared.add(entry.getKey());
            }
        }
        return shared;
    }

    public Set<String> getDependents(String dependency) {
        return depToProjects.getOrDefault(dependency, Collections.emptySet());
    }

    public int getDependentCount(String dependency) {
        return depToProjects.getOrDefault(dependency, Collections.emptySet()).size();
    }
}

class BlueprintViolationException extends Exception {
    public BlueprintViolationException(String message) {
        super(message);
    }
}

class Blueprint {
    public static List<File> findAllProjects(File root) {
        List<File> folders = new ArrayList<>();
        File[] files = root.listFiles();
        if (files == null) return folders;

        for (File f : files) {
            if (f.isDirectory() && !f.getName().startsWith(".") && !f.getName().equals("Architect")) {
                if (new File(f, "nbproject/project.xml").exists()) {
                    folders.add(f);
                }
                folders.addAll(findAllProjects(f));
            }
        }
        return folders;
    }

    public static DependencyGraph buildDependencyGraph(List<File> projectFolders) {
        DependencyGraph graph = new DependencyGraph();
        
        for (File projectDir : projectFolders) {
            try {
                File projectXml = new File(projectDir, "nbproject/project.xml");
                if (!projectXml.exists()) continue;
                
                String projectName = projectDir.getName();
                String xmlContent = Files.readString(projectXml.toPath());
                
                Matcher depMatcher = Pattern.compile("<foreign-project>([^<]+)</foreign-project>").matcher(xmlContent);
                while (depMatcher.find()) {
                    String dependency = depMatcher.group(1).trim();
                    graph.addDependency(projectName, dependency);
                }
            } catch (Exception e) {
                System.err.println("  [!] Error parsing project " + projectDir.getName() + ": " + e.getMessage());
            }
        }
        
        return graph;
    }
}

class Inspector {
    private static final String EXPECTED_JAVA_VERSION = "1.8";

    public static ProjectSite inspectSite(File projectDir, File siteRoot, Set<String> sharedDeps) {
        String pathName = siteRoot.toURI().relativize(projectDir.toURI()).getPath();
        System.out.println("[SCAN] Entering Sector: " + pathName);

        ProjectSite site = new ProjectSite(projectDir, pathName, new HashSet<>());

        try {
            // PHASE 1: Foundation Inspection (Java Versions)
            inspectFoundation(projectDir, site);

            // PHASE 2: Structural Inspection (Dependencies)
            inspectStructure(projectDir, site, sharedDeps);

        } catch (Exception e) {
            System.err.println("  [!] Error during scan of " + pathName + ": " + e.getMessage());
        }

        return site;
    }
    
    private static void inspectFoundation(File projectDir, ProjectSite site) {
        try {
            File propsFile = new File(projectDir, "nbproject/project.properties");
            if (!propsFile.exists()) return;
            
            String content = Files.readString(propsFile.toPath());
            
            // Extract current versions
            site.currentSourceVer = extractProperty(content, "javac.source");
            site.currentTargetVer = extractProperty(content, "javac.target");
            
            // Validate against blueprint specs
            boolean sourceMismatch = site.currentSourceVer != null && !site.currentSourceVer.equals(EXPECTED_JAVA_VERSION);
            boolean targetMismatch = site.currentTargetVer != null && !site.currentTargetVer.equals(EXPECTED_JAVA_VERSION);
            
            if (sourceMismatch || targetMismatch) {
                site.needsVersionFix = true;
                System.err.println("  [⚠] FOUNDATION MISMATCH: Java version inconsistency detected");
                if (sourceMismatch) {
                    System.err.println("      └─ javac.source: " + site.currentSourceVer + " (expected " + EXPECTED_JAVA_VERSION + ")");
                }
                if (targetMismatch) {
                    System.err.println("      └─ javac.target: " + site.currentTargetVer + " (expected " + EXPECTED_JAVA_VERSION + ")");
                }
            } else if (site.currentSourceVer != null) {
                System.out.println("  [✔] Foundation stable: Java " + site.currentSourceVer);
            }
        } catch (Exception e) {
            System.err.println("  [!] Foundation scan error: " + e.getMessage());
        }
    }
    
    private static void inspectStructure(File projectDir, ProjectSite site, Set<String> sharedDeps) {
        try {
            File projectXml = new File(projectDir, "nbproject/project.xml");
            if (!projectXml.exists()) return;

            String xmlContent = Files.readString(projectXml.toPath());

            Set<String> dependencies = new HashSet<>();
            Matcher depMatcher = Pattern.compile("<foreign-project>([^<]+)</foreign-project>").matcher(xmlContent);
            while (depMatcher.find()) dependencies.add(depMatcher.group(1).trim());

            if (dependencies.isEmpty()) {
                System.out.println("  [i] Site is independent. No reinforcement needed.");
                return;
            }

            // Store declared dependencies for reference
            site.declaredDeps.addAll(dependencies);

            File buildXml = new File(projectDir, "build.xml");
            String buildContent = buildXml.exists() ? Files.readString(buildXml.toPath()) : "";

            for (String dep : dependencies) {
                // Skip shared dependencies for non-Builder projects - they will be merged by Builder only
                if (sharedDeps.contains(dep) && !site.projectName.equals("Builder")) {
                    System.out.println("  [i] Component '" + dep + "' is shared foundation - merged at Builder level.");
                    continue;
                }

                if (buildContent.contains(dep) && buildContent.contains("zipgroupfileset")) {
                    System.out.println("  [✔] Component '" + dep + "' securely bolted.");
                } else {
                    System.err.println("  [✘] STRUCTURAL WEAKNESS: '" + dep + "' is not merged.");
                    site.missingDeps.add(dep);
                }
            }
        } catch (Exception e) {
            System.err.println("  [!] Structural scan error: " + e.getMessage());
        }
    }
    
    private static String extractProperty(String content, String key) {
        Matcher matcher = Pattern.compile("^" + key + "=(.+)$", Pattern.MULTILINE).matcher(content);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
}

class Foreman {
    private static final String EXPECTED_JAVA_VERSION = "1.8";

    public static void stabilizeFoundation(ProjectSite site) {
        System.out.println("[WORK] Stabilizing Foundation: " + site.pathName);
        File propsFile = new File(site.dir, "nbproject/project.properties");

        try {
            String content = Files.readString(propsFile.toPath(), StandardCharsets.UTF_8);

            // Check and update javac.source
            if (site.currentSourceVer != null && !site.currentSourceVer.equals(EXPECTED_JAVA_VERSION)) {
                System.out.println("  [🔧] Realigning javac.source: " + site.currentSourceVer + " → " + EXPECTED_JAVA_VERSION);
                content = Pattern.compile("^javac.source=.+$", Pattern.MULTILINE)
                    .matcher(content).replaceAll("javac.source=" + EXPECTED_JAVA_VERSION);
            }

            // Check and update javac.target
            if (site.currentTargetVer != null && !site.currentTargetVer.equals(EXPECTED_JAVA_VERSION)) {
                System.out.println("  [🔧] Realigning javac.target: " + site.currentTargetVer + " → " + EXPECTED_JAVA_VERSION);
                content = Pattern.compile("^javac.target=.+$", Pattern.MULTILINE)
                    .matcher(content).replaceAll("javac.target=" + EXPECTED_JAVA_VERSION);
            }

            Files.writeString(propsFile.toPath(), content, StandardCharsets.UTF_8);
            System.out.println("  [OK] Foundation stabilized to Java " + EXPECTED_JAVA_VERSION + " specifications.");

        } catch (Exception e) {
            System.err.println("  [!] Foundation stabilization failed on site " + site.pathName + ": " + e.getMessage());
        }
    }

    public static void retrofit(ProjectSite site) {
        System.out.println("[WORK] Retrofitting Site: " + site.pathName);
        File buildXml = new File(site.dir, "build.xml");

        try {
            String content = Files.readString(buildXml.toPath(), StandardCharsets.UTF_8);

            // Build the zipgroupfileset entries for missing dependencies
            StringBuilder mergeLines = new StringBuilder();
            for (String dep : site.missingDeps) {
                System.out.println("  [+] Injecting merge logic for project: " + dep);
                mergeLines.append("            <zipgroupfileset dir=\"${project.").append(dep).append("}/dist\" includes=\"*.jar\"/>\n");
            }

            // Logic: Find the -post-jar target. If it doesn't exist, we'll need to create it.
            if (content.contains("name=\"-post-jar\"")) {
                // Insert lines before the manifest zipfileset within the jar task
                int postJarStart = content.indexOf("name=\"-post-jar\"");
                int targetEnd = content.indexOf("</target>", postJarStart);
                
                // Look for the manifest zipfileset line within this target
                String section = content.substring(postJarStart, targetEnd != -1 ? targetEnd : content.length());
                int manifestIdx = section.indexOf("META-INF/MANIFEST.MF");
                
                if (manifestIdx != -1) {
                    // Calculate absolute position in content
                    int insertPos = postJarStart + section.lastIndexOf("<zipfileset", manifestIdx);
                    String before = content.substring(0, insertPos);
                    String after = content.substring(insertPos);
                    content = before + mergeLines.toString() + after;
                } else if (targetEnd != -1) {
                    // Fallback: insert before </target>
                    String before = content.substring(0, targetEnd);
                    String after = content.substring(targetEnd);
                    content = before + mergeLines.toString() + after;
                }
            } else {
                // Create the complete target before the final </project> tag
                String newTarget = "\n    <target name=\"-post-jar\">\n" +
                    "        <echo message=\"Architect: Merging dependencies into ${dist.jar}...\"/>\n" +
                    "        <property name=\"dist.jar.temp\" value=\"${dist.jar}.tmp\"/>\n" +
                    "        \n" +
                    "        <move file=\"${dist.jar}\" tofile=\"${dist.jar.temp}\"/>\n" +
                    "        \n" +
                    "        <jar destfile=\"${dist.jar}\" update=\"false\" manifestencoding=\"UTF-8\">\n" +
                    "            <zipfileset src=\"${dist.jar.temp}\"/>\n" +
                    "            \n" +
                    mergeLines.toString() +
                    "            \n" +
                    "            <zipfileset src=\"${dist.jar.temp}\" includes=\"META-INF/MANIFEST.MF\"/>\n" +
                    "        </jar>\n" +
                    "        \n" +
                    "        <delete file=\"${dist.jar.temp}\"/>\n" +
                    "    </target>\n";
                content = content.replace("</project>", newTarget + "</project>");
            }

            Files.writeString(buildXml.toPath(), content, StandardCharsets.UTF_8);
            System.out.println("  [OK] Site reinforced.");

        } catch (Exception e) {
            System.err.println("  [!] Foreman Error on site " + site.pathName + ": " + e.getMessage());
        }
    }
}