#!/bin/bash

# Check for required arguments
if [ "$#" -ne 2 ]; then
    echo "Usage: <list_of_classes> | $0 <source_dir> <target_dir>"
    echo "Example: cat deps.txt | $0 ./build/classes ./extracted_deps"
    exit 1
fi

SRC_DIR=$1
TARGET_DIR=$2

echo "Starting extraction..."
echo "Source: $SRC_DIR"
echo "Target: $TARGET_DIR"
echo "---------------------------------------------------------------"

# Count successful copies
COUNT=0

# Read each class name from stdin
while read -r CLASS; do
    # 1. Convert package dots to slashes and append .class
    # Example: com.foo.Bar -> com/foo/Bar.class
    REL_PATH="${CLASS//.//}.class"
    
    SRC_FILE="$SRC_DIR/$REL_PATH"
    DEST_FILE="$TARGET_DIR/$REL_PATH"

    # 2. Check if the class file actually exists in the source
    if [ -f "$SRC_FILE" ]; then
        # 3. Create the target directory structure (-p handles nested folders)
        mkdir -p "$(dirname "$DEST_FILE")"
        
        # 4. Copy the file
        cp "$SRC_FILE" "$DEST_FILE"
        ((COUNT++))
    else
        echo "Skipping: $CLASS (File not found in $SRC_DIR)"
    fi
done

echo "---------------------------------------------------------------"
echo "Done! Extracted $COUNT files to $TARGET_DIR"
