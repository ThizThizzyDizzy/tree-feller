#!/bin/bash

# Check if at least a JAR and one class are provided
if [ "$#" -lt 2 ]; then
    echo "Usage: $0 <jar-file> <class1> [class2] [class3] ..."
    exit 1
fi

JAR_FILE=$1
# Shift the arguments so $@ only contains the class names
shift 
CLASSES=("$@")

# Create a temporary file to collect results
TMP_RESULTS=$(mktemp)

for CLASS in "${CLASSES[@]}"; do
    # 1. FIND THE EXACT CLASS
    # Convert package dots to slashes for searching the JAR's file structure
    SEARCH_PATTERN="${CLASS//./\/}"
    
    # Read JAR contents, find the class file, convert slashes back to dots, and drop '.class'
    EXACT_CLASSES=$(jar tf "$JAR_FILE" | grep -i "${SEARCH_PATTERN}.*\.class$" | sed 's/\//./g' | sed 's/\.class$//')
    
    if [ -n "$EXACT_CLASSES" ]; then
        # Append the exact class(es) to our temp results so they appear in the final output
        echo "$EXACT_CLASSES" >> "$TMP_RESULTS"
    fi

    # 2. FIND DEPENDENCIES
    # Run jdeps and append to our temp file
    jdeps -verbose:class -recursive "$JAR_FILE" | \
        grep "^.*$CLASS.* ->" | \
        grep -v "java.base" | \
        awk '{print $3}' >> "$TMP_RESULTS"
done

# Output the final unique, sorted list (ignoring empty lines)
sort -u "$TMP_RESULTS" | grep -v '^[[:space:]]*$'

# Clean up
rm "$TMP_RESULTS"
