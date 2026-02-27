#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <input_dir> <output_dir> <new_prefix>"
    exit 1
fi

INPUT_DIR=$(realpath "$1")
OUTPUT_DIR=$(realpath "$2")
NEW_PREFIX="$3" # e.g., com.thizthizzydizzy.treefeller.lib
NEW_PREFIX_PATH="${NEW_PREFIX//.//}"

# 1. Setup ASM dependencies
ASM_VERSION="9.6"
ASM_JAR="asm-$ASM_VERSION.jar"
ASM_COMMONS_JAR="asm-commons-$ASM_VERSION.jar"

if [ ! -f "$ASM_JAR" ]; then
    echo "Downloading ASM..."
    curl -sL "https://repo1.maven.org/maven2/org/ow2/asm/asm/$ASM_VERSION/$ASM_JAR" -o "$ASM_JAR"
    curl -sL "https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/$ASM_VERSION/$ASM_COMMONS_JAR" -o "$ASM_COMMONS_JAR"
fi

# 2. Create the Java Remapper Tool
cat <<EOF > SimpleRemapper.java
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import java.io.*;
import java.nio.file.*;

public class SimpleRemapper {
    public static void main(String[] args) throws Exception {
        String inputDir = args[0];
        String outputDir = args[1];
        String prefix = args[2].replace('.', '/');

        Remapper remapper = new Remapper() {
            @Override
            public String map(String internalName) {
                // Only remap if it's NOT already remapped and NOT a java/ system class
                if (!internalName.startsWith(prefix) && !internalName.startsWith("java/")) {
                    return prefix + "/" + internalName;
                }
                return internalName;
            }
        };

        Files.walk(Paths.get(inputDir)).filter(p -> p.toString().endsWith(".class")).forEach(path -> {
            try {
                byte[] bytes = Files.readAllBytes(path);
                ClassReader cr = new ClassReader(bytes);
                ClassWriter cw = new ClassWriter(0);
                ClassVisitor cv = new ClassRemapper(cw, remapper);
                cr.accept(cv, 0);

                String relPath = Paths.get(inputDir).relativize(path).toString();
                Path outPath = Paths.get(outputDir, prefix, relPath);
                Files.createDirectories(outPath.getParent());
                Files.write(outPath, cw.toByteArray());
            } catch (IOException e) { e.printStackTrace(); }
        });
    }
}
EOF

# 3. Compile and Run
echo "Compiling ASM remapper..."
javac -cp "$ASM_JAR:$ASM_COMMONS_JAR" SimpleRemapper.java

echo "Executing repackage..."
java -cp ".:$ASM_JAR:$ASM_COMMONS_JAR" SimpleRemapper "$INPUT_DIR" "$OUTPUT_DIR" "$NEW_PREFIX"

# 4. Cleanup
rm SimpleRemapper.class SimpleRemapper.java

echo "---------------------------------------------------------------"
echo "Check $OUTPUT_DIR/$NEW_PREFIX_PATH/ for your files!"
