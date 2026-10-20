#!/usr/bin/env bash
# Builds the jar in Docker and starts every scenario the way a user would (java -jar), saving each full console
# output to evidence/logs/<scenario>[--allow].log plus its exit code. These logs are the video's evidence.
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p evidence/logs
MSYS_NO_PATHCONV=1 docker run --rm -v "$(pwd -W 2>/dev/null || pwd):/lab" -v spring-bean-cycle-m2:/root/.m2 -w /lab \
  maven:3.9-eclipse-temurin-21 bash -c '
set -u
mvn -q -B -DskipTests package
java -version 2>&1 | head -1 > evidence/logs/java-version.txt
run() {  # run <log-name> <args...>
  local name=$1; shift
  java -jar target/spring-bean-cycle-lab-1.0.jar "$@" > evidence/logs/$name.log 2>&1
  echo "exit code: $?" >> evidence/logs/$name.log
}
for s in constructor-cycle field-cycle setter-cycle three-bean-cycle mixed-field-first mixed-constructor-first fix-extract fix-events fix-lazy fix-provider; do run $s $s; done
for s in constructor-cycle field-cycle setter-cycle mixed-field-first mixed-constructor-first; do run $s--allow $s --spring.main.allow-circular-references=true; done
'
# The 2.6 change: the same code on the last 2.5 and 2.6 releases (JDK 17; pom-2.5.xml / pom-2.6.xml).
for v in 2.5 2.6; do
  MSYS_NO_PATHCONV=1 docker run --rm -v "$(pwd -W 2>/dev/null || pwd):/lab" -v spring-bean-cycle-m2:/root/.m2 -w /lab \
    maven:3.9-eclipse-temurin-17 bash -c "
set -u
mvn -q -B -DskipTests -f pom-$v.xml package
for s in constructor-cycle field-cycle; do
  java -jar target-$v/spring-bean-cycle-lab-1.0.jar \$s > evidence/logs/boot-$v-\$s.log 2>&1
  echo \"exit code: \$?\" >> evidence/logs/boot-$v-\$s.log
done
"
done
for f in evidence/logs/*.log; do printf '%-34s %s\n' "$(basename "$f")" "$(tail -1 "$f")"; done
