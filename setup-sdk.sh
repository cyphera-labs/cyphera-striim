#!/bin/bash
# Extract the Striim Open Processor SDK from a running Striim container.
# Run this once before building the processors.
#
# Usage: bash setup-sdk.sh [container_name]

set -e
CONTAINER=${1:-cyphera-striim-striim-1}
mkdir -p lib

echo "Extracting StriimOpenProcessor-SDK.jar from $CONTAINER..."
docker cp "$CONTAINER:/opt/striim/StriimSDK/StriimOpenProcessor-SDK.jar" lib/
echo "Done. SDK saved to lib/StriimOpenProcessor-SDK.jar"
echo ""
echo "You can now build with: docker build -t cyphera-striim ."
