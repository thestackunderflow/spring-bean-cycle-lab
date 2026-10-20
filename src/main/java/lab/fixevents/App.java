package lab.fixevents;

import java.util.HashMap;
import java.util.Map;
import lab.Demo;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

// Fix 2: PaymentService no longer calls OrderService. It announces "payment succeeded"; OrderService listens.
@SpringBootApplication
public class App {}

record PaymentSucceeded(int orderId) {}

@Service
class OrderService implements Demo {
    private final PaymentService payment;
    private final Map<Integer, String> status = new HashMap<>();

    OrderService(PaymentService payment) { this.payment = payment; }

    public String placeOrder(int id) {
        status.put(id, "NEW");
        payment.charge(id);
        return "order " + id + ": " + status.get(id);
    }

    @EventListener
    void on(PaymentSucceeded e) { status.put(e.orderId(), "PAID"); }
}

@Service
class PaymentService {
    private final ApplicationEventPublisher events;

    PaymentService(ApplicationEventPublisher events) { this.events = events; }

    void charge(int id) { events.publishEvent(new PaymentSucceeded(id)); }
}
