package lab;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Starts one scenario: java -jar lab.jar constructor-cycle [--spring.main.allow-circular-references=true]
 * On success it places order 42 and prints the result; on failure Spring prints its report and we exit 1.
 */
public class CycleLab {
    static final Map<String, Class<?>> SCENARIOS = new LinkedHashMap<>();
    static {
        SCENARIOS.put("constructor-cycle", lab.constructorcycle.App.class);
        SCENARIOS.put("field-cycle", lab.fieldcycle.App.class);
        SCENARIOS.put("setter-cycle", lab.settercycle.App.class);
        SCENARIOS.put("three-bean-cycle", lab.threebeancycle.App.class);
        SCENARIOS.put("mixed-field-first", lab.mixedfieldfirst.App.class);
        SCENARIOS.put("mixed-constructor-first", lab.mixedconstructorfirst.App.class);
        SCENARIOS.put("fix-extract", lab.fixextract.App.class);
        SCENARIOS.put("fix-events", lab.fixevents.App.class);
        SCENARIOS.put("fix-lazy", lab.fixlazy.App.class);
        SCENARIOS.put("fix-provider", lab.fixprovider.App.class);
    }

    public static ConfigurableApplicationContext start(String scenario, String... args) {
        Class<?> app = SCENARIOS.get(scenario);
        if (app == null) throw new IllegalArgumentException("unknown scenario " + scenario + "; one of " + SCENARIOS.keySet());
        return new SpringApplication(app).run(args);
    }

    public static String demo(ConfigurableApplicationContext ctx) {
        return ctx.getBean(Demo.class).placeOrder(42);
    }

    public static String injectedClass(ConfigurableApplicationContext ctx) {
        return ctx.getBean(Demo.class).paymentClass();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("scenarios: " + SCENARIOS.keySet());
            return;
        }
        try (ConfigurableApplicationContext ctx = start(args[0], Arrays.copyOfRange(args, 1, args.length))) {
            System.out.println("LAB: started. " + demo(ctx));
            String cls = injectedClass(ctx);
            if (!cls.isEmpty()) System.out.println("LAB: OrderService holds a " + cls);
        } catch (RuntimeException e) {
            System.exit(1);
        }
    }
}
