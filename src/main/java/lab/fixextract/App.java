package lab.fixextract;

import java.util.HashMap;
import java.util.Map;
import lab.Demo;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

// Fix 1: the thing both services needed (the order's status) moves into a third bean. Arrows now point one way.
@SpringBootApplication
public class App {}

@Service
class OrderStatusStore {
    private final Map<Integer, String> status = new HashMap<>();

    void set(int id, String s) { status.put(id, s); }
    String get(int id) { return status.get(id); }
}

@Service
class OrderService implements Demo {
    private final PaymentService payment;
    private final OrderStatusStore store;

    OrderService(PaymentService payment, OrderStatusStore store) {
        this.payment = payment;
        this.store = store;
    }

    public String placeOrder(int id) {
        store.set(id, "NEW");
        payment.charge(id);
        return "order " + id + ": " + store.get(id);
    }
}

@Service
class PaymentService {
    private final OrderStatusStore store;

    PaymentService(OrderStatusStore store) { this.store = store; }

    void charge(int id) { store.set(id, "PAID"); }
}
