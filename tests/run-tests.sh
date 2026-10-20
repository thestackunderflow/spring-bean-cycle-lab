#!/usr/bin/env bash
# Runs the JUnit tests in Docker (Maven + JDK 21). Maven's downloads are cached in a named volume.
# Usage: bash tests/run-tests.sh [log-file]
set -euo pipefail
cd "$(dirname "$0")/.."
LOG="${1:-tests/out/test-run.log}"
mkdir -p "$(dirname "$LOG")"
MSYS_NO_PATHCONV=1 docker run --rm -v "$(pwd -W 2>/dev/null || pwd):/lab" -v spring-bean-cycle-m2:/root/.m2 -w /lab \
  maven:3.9-eclipse-temurin-21 mvn -q -B test 2>&1 | tee "$LOG" || true
# -q hides Maven's summary: take the counts from the Surefire report
grep -o 'tests="[0-9]*" errors="[0-9]*" skipped="[0-9]*" failures="[0-9]*"' target/surefire-reports/TEST-lab.CycleLabTest.xml \
  | sed 's/^/JUnit summary: /' | tee -a "$LOG"
python -m unittest discover -s tests -p "test_evidence.py" 2>&1 | tail -3 | tee -a "$LOG"
