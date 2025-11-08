#!/bin/bash

# Generate localhost certificates for development
# This script requires mkcert to be installed globally or will download it

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CERTS_DIR="$PROJECT_ROOT/certs"

echo "Generating localhost certificates..."

# Find mkcert in PATH or download it
MKCERT=$(which mkcert || echo "")

if [ -z "$MKCERT" ]; then
    echo "mkcert not found in PATH. Downloading..."
    TEMP_DIR=$(mktemp -d)
    curl -L https://github.com/FiloSottile/mkcert/releases/download/v1.4.4/mkcert-v1.4.4-windows-amd64.exe -o "$TEMP_DIR/mkcert.exe"
    MKCERT="$TEMP_DIR/mkcert.exe"
    "$MKCERT" -install
fi

# Create certs directory
mkdir -p "$CERTS_DIR"

# Generate certificates
cd "$CERTS_DIR"
"$MKCERT" -key-file localhost-key.pem -cert-file localhost.pem localhost 127.0.0.1 ::1

echo "✅ Certificates generated in $CERTS_DIR"
