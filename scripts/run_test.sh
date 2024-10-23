#!/bin/bash

# Variables
ROOT_DIR=$(dirname $(dirname $(realpath $0)))
GRADLE_TASKS=("test" "integrationTest")

cd "$ROOT_DIR" || { echo "Failed to navigate to project root."; exit 1; }

for task in "${GRADLE_TASKS[@]}"; do
  echo "Running ./gradlew clean $task..."
  ./gradlew clean $task

  if [ $? -ne 0 ]; then
    echo "The task $task failed."
    exit 1
  fi
done

echo "All tasks completed successfully."
