#!/bin/bash

# Simple script to chain utility scripts for jar processing
# Usage: ./chain_scripts.sh input.jar "class1 class2 ..." output.jar new.package.name
#        ./chain_scripts.sh input.jar all output.jar new.package.name

set -e

INPUT_JAR="$1"
CLASSES="$2"
OUTPUT_JAR="$3"
NEW_PACKAGE="$4"

echo "Processing: $INPUT_JAR"
echo "Classes: $CLASSES"
echo "Output: $OUTPUT_JAR"
echo "New Package: $NEW_PACKAGE"

mkdir -p input
cd input
jar -xf "../$INPUT_JAR"
cd ..

if [[ "$CLASSES" != "all" ]]; then
    echo "Extracting dependencies..."
    ./list_deps.sh "$INPUT_JAR" $CLASSES | ./extract_classes.sh ./input ./extracted

    echo "Repackaging..."
    ./repackage_asm.sh ./extracted ./output "$NEW_PACKAGE"
else
    echo "Repackaging..."
    ./repackage_asm.sh ./input ./output "$NEW_PACKAGE"
fi

echo "Creating output jar..."
OUTPUT_DIR="output"
cd "$OUTPUT_DIR"
jar -cf "../$OUTPUT_JAR" .
cd ..

echo "Cleaning up..."

rm -rf ./input ./output ./extracted 

echo "Done! Created $OUTPUT_JAR"
