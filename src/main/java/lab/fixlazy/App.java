package lab.fixlazy;

import java.util.HashMap;
import java.util.Map;
import lab.Demo;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

// Fix 3: @Lazy on one constructor parameter. Spring hands OrderService a stand-in (a proxy) for PaymentService,
// and finds the real one on the first call. The two classes still need each other.
@SpringBootApplication
public class App {}

@Service
class OrderService implements Demo {
    private final PaymentService payment;
    private final Map<Integer, String> status = new HashMap<>();

    OrderService(@Lazy PaymentService payment) {
        System.out.println("LAB: creating OrderService, handed a " + payment.getClass().getSimpleName());
        this.payment = payment;
    }

    public String placeOrder(int id) {
        status.put(id, "NEW");
        payment.charge(id);
        return "order " + id + ": " + status.get(id);
    }

    public String paymentClass() { return payment.getClass().getName(); }

    void markPaid(int id) { status.put(id, "PAID"); }
}

@Service
class PaymentService {
    private final OrderService orders;

    PaymentService(OrderService orders) {
        System.out.println("LAB: creating PaymentService");
        this.orders = orders;
    }

    void charge(int id) { orders.markPaid(id); }
}
