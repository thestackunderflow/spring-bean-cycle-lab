package lab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * One test per behaviour the video claims. Each scenario is a separate @SpringBootApplication in its own package,
 * started exactly the way `java -jar lab.jar <scenario>` starts it (CycleLab.start).
 */
@ExtendWith(OutputCaptureExtension.class)
class CycleLabTest {

    static final String HEADLINE = "The dependencies of some of the beans in the application context form a cycle:";
    static final String ALLOW = "--spring.main.allow-circular-references=true";

    // 1. Two services that need each other through their constructors: Spring refuses to start.
    @Test
    void constructorCycleRefusesToStartAndDrawsTheCycle(CapturedOutput out) {
        assertThatThrownBy(() -> CycleLab.start("constructor-cycle"))
            .hasRootCauseInstanceOf(BeanCurrentlyInCreationException.class);
        assertThat(out).contains(HEADLINE, "orderService", "paymentService", "┌─────┐", "└─────┘");
        assertThat(out).contains("spring.main.allow-circular-references");
    }

    // 2. The flag does not rescue a constructor cycle: an object can't be handed to a constructor before it exists.
    @Test
    void allowFlagDoesNotFixAConstructorCycle() {
        assertThatThrownBy(() -> CycleLab.start("constructor-cycle", ALLOW))
            .hasRootCauseInstanceOf(BeanCurrentlyInCreationException.class);
    }

    // 3. Field injection: refused by default since Boot 2.6...
    @Test
    void fieldCycleRefusedByDefault(CapturedOutput out) {
        assertThatThrownBy(() -> CycleLab.start("field-cycle"))
            .hasRootCauseInstanceOf(BeanCurrentlyInCreationException.class);
        assertThat(out).contains(HEADLINE);
    }

    // 4. ...but starts with the flag, because Spring can hand out a half-built ("early") object.
    @Test
    void fieldCycleStartsWithTheFlag() {
        try (ConfigurableApplicationContext ctx = CycleLab.start("field-cycle", ALLOW)) {
            assertThat(CycleLab.demo(ctx)).contains("order 42: PAID");
        }
    }

    // 5. Setter injection behaves like field injection.
    @Test
    void setterCycleRefusedByDefaultStartsWithTheFlag() {
        assertThatThrownBy(() -> CycleLab.start("setter-cycle"))
            .hasRootCauseInstanceOf(BeanCurrentlyInCreationException.class);
        try (ConfigurableApplicationContext ctx = CycleLab.start("setter-cycle", ALLOW)) {
            assertThat(CycleLab.demo(ctx)).contains("order 42: PAID");
        }
    }

    // 6. A longer loop: the diagram lists every bean in the loop.
    @Test
    void threeBeanCycleListsAllThree(CapturedOutput out) {
        assertThatThrownBy(() -> CycleLab.start("three-bean-cycle"))
            .hasRootCauseInstanceOf(BeanCurrentlyInCreationException.class);
        assertThat(out).contains(HEADLINE, "orderService", "paymentService", "invoiceService");
    }

    // 6b. A mixed loop (one field, one constructor) where Spring builds the FIELD bean first: refused by default,
    // but the flag rescues it, because the field bean exists before the constructor bean asks for it.
    @Test
    void mixedLoopFieldBeanFirstStartsWithTheFlag(CapturedOutput out) {
        assertThatThrownBy(() -> CycleLab.start("mixed-field-first"))
            .hasRootCauseInstanceOf(BeanCurrentlyInCreationException.class);
        try (ConfigurableApplicationContext ctx = CycleLab.start("mixed-field-first", ALLOW)) {
            assertThat(CycleLab.demo(ctx)).contains("order 42: PAID");
        }
        assertThat(out.toString().lastIndexOf("LAB: creating OrderService (field)"))
            .isLessThan(out.toString().lastIndexOf("LAB: creating PaymentService (constructor)"));
    }

    // 6c. The same mixed loop, but Spring builds the CONSTRUCTOR bean first: the flag can't help.
    @Test
    void mixedLoopConstructorBeanFirstFailsEvenWithTheFlag() {
        assertThatThrownBy(() -> CycleLab.start("mixed-constructor-first", ALLOW))
            .hasRootCauseInstanceOf(BeanCurrentlyInCreationException.class);
    }

    // 7. Fix 1: move what both needed into a third bean. No cycle, no flag.
    @Test
    void fixExtractThirdBeanStarts() {
        try (ConfigurableApplicationContext ctx = CycleLab.start("fix-extract")) {
            assertThat(CycleLab.demo(ctx)).contains("order 42: PAID");
        }
    }

    // 8. Fix 2: payment announces an event instead of calling back into orders.
    @Test
    void fixEventsStarts() {
        try (ConfigurableApplicationContext ctx = CycleLab.start("fix-events")) {
            assertThat(CycleLab.demo(ctx)).contains("order 42: PAID");
        }
    }

    // 9. Fix 3: @Lazy injects a stand-in (a proxy); the cycle is still in the design.
    @Test
    void fixLazyStartsWithAProxy() {
        try (ConfigurableApplicationContext ctx = CycleLab.start("fix-lazy")) {
            assertThat(CycleLab.demo(ctx)).contains("order 42: PAID");
            assertThat(CycleLab.injectedClass(ctx)).contains("$$");
        }
    }

    // 10. Fix 4: ObjectProvider asks for the bean only when it is needed.
    @Test
    void fixObjectProviderStarts() {
        try (ConfigurableApplicationContext ctx = CycleLab.start("fix-provider")) {
            assertThat(CycleLab.demo(ctx)).contains("order 42: PAID");
        }
    }
}
