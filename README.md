# Spring Boot "beans ... form a cycle": a lab

**Lab snapshot: 19 Oct 2026.** Companion lab for The Stack Underflow's video on this error (video coming 30 Oct). Spring Boot **4.1.1** on Java 21, plus the same code on **2.5.15** and **2.6.15** (Java 17)
to show the 2.6 change. Everything runs in Docker (Maven images); nothing is installed on the host.

One tiny shop app: `OrderService` needs `PaymentService` to charge, and `PaymentService` needs `OrderService` to mark
the order paid. Each scenario is its own `@SpringBootApplication` in its own package, started with
`java -jar lab.jar <scenario>`. On success it places order 42 and prints `order 42: PAID`.

| Scenario | What it shows | 4.1.1 result |
|---|---|---|
| `constructor-cycle` | the cycle with constructor injection | fails; neither constructor runs |
| `constructor-cycle` + flag | `--spring.main.allow-circular-references=true` | still fails ("Despite circular references being allowed...") |
| `field-cycle`, `setter-cycle` | the cycle with `@Autowired` fields / setters | fail by default; start with the flag |
| `three-bean-cycle` | Order → Payment → Invoice → Order | fails; the box lists all three |
| `mixed-field-first` | one field + one constructor; Spring builds the field bean first | fails by default; starts with the flag |
| `mixed-constructor-first` | the same, constructor bean built first | fails even with the flag |
| `fix-extract` | the shared status moved into `OrderStatusStore` | starts, no flag |
| `fix-events` | Payment publishes `PaymentSucceeded`, Order listens | starts |
| `fix-provider` | `ObjectProvider<PaymentService>` | starts |
| `fix-lazy` | `@Lazy` on the constructor parameter | starts; a CGLIB proxy is injected |

```bash
bash tests/run-tests.sh      # JUnit (12 tests, Boot 4.1.1) + the evidence checks (12 tests)
bash run-scenarios.sh        # rebuilds and reruns every scenario, rewriting evidence/logs/
```

Tests were written first and run red (`tests/out/red-phase.log`: 10 failing against a stub), then green
(`tests/out/green-phase.log`). `tests/test_evidence.py` checks the recorded logs. One more check confirms that every
terminal line shown in the video was copied from one of these logs; it needs the video's script, which is not in this
repo, so it is skipped here (11 run, 1 skipped).

`video/frame0.py` draws the Short's first frame from the real report in `evidence/logs/constructor-cycle.log`.

## License

Apache-2.0, see `LICENSE`.
