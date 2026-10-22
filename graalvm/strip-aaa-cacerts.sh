#!/usr/bin/env bash
# Writes a copy of the JDK default cacerts without "AAA Certificate Services"
# to $1 (default: target/trust/cacerts). native-image embeds the build JDK's
# trust store, so this keeps distrusted roots out of the binary.
set -euo pipefail

OUT="${1:-target/trust/cacerts}"
JAVA_HOME_DIR="${JAVA_HOME:-$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")}"
SRC="$JAVA_HOME_DIR/lib/security/cacerts"

mkdir -p "$(dirname "$OUT")"
cp "$SRC" "$OUT"
chmod u+w "$OUT"

keytool -list -keystore "$OUT" -storepass changeit 2>/dev/null \
  | grep -i -E '^(comodoaaaca|aaacertificateservices)' \
  | sed 's/, .*//' \
  | while IFS= read -r alias; do
      keytool -delete -alias "$alias" -keystore "$OUT" -storepass changeit 2>/dev/null
      echo "removed $alias"
    done

if keytool -list -keystore "$OUT" -storepass changeit 2>/dev/null | grep -qi -E '^(comodoaaaca|aaacertificateservices)'; then
  echo "AAA Certificate Services still present in $OUT" >&2
  exit 1
fi
