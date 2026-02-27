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

echo "Analyzing dependencies for: ${CLASSES[*]}"
echo "Source: $JAR_FILE"
echo "---------------------------------------------------------------"

# Create a temporary file to collect results
TMP_RESULTS=$(mktemp)

for CLASS in "${CLASSES[@]}"; do
    # Run jdeps and append to our temp file
    jdeps -verbose:class -recursive "$JAR_FILE" | \
        grep "^.*$CLASS.* ->" | \
        grep -v "java.base" | \
        awk '{print $3}' >> "$TMP_RESULTS"
done

# Output the final unique, sorted list
sort -u "$TMP_RESULTS"

# Clean up
rm "$TMP_RESULTS"
