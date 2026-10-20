package lab.fixprovider;

import java.util.HashMap;
import java.util.Map;
import lab.Demo;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

// Fix 4: OrderService asks for an ObjectProvider, and fetches PaymentService only when it places an order.
@SpringBootApplication
public class App {}

@Service
class OrderService implements Demo {
    private final ObjectProvider<PaymentService> payment;
    private final Map<Integer, String> status = new HashMap<>();

    OrderService(ObjectProvider<PaymentService> payment) { this.payment = payment; }

    public String placeOrder(int id) {
        status.put(id, "NEW");
        payment.getObject().charge(id);
        return "order " + id + ": " + status.get(id);
    }

    void markPaid(int id) { status.put(id, "PAID"); }
}

@Service
class PaymentService {
    private final OrderService orders;

    PaymentService(OrderService orders) { this.orders = orders; }

    void charge(int id) { orders.markPaid(id); }
}
