package lab.settercycle;

import java.util.HashMap;
import java.util.Map;
import lab.Demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

// The same cycle, with setter injection.
@SpringBootApplication
public class App {}

@Service
class OrderService implements Demo {
    private PaymentService payment;
    private final Map<Integer, String> status = new HashMap<>();

    OrderService() { System.out.println("LAB: creating OrderService"); }

    @Autowired
    void setPayment(PaymentService payment) { this.payment = payment; }

    public String placeOrder(int id) {
        status.put(id, "NEW");
        payment.charge(id);
        return "order " + id + ": " + status.get(id);
    }

    void markPaid(int id) { status.put(id, "PAID"); }
}

@Service
class PaymentService {
    private OrderService orders;

    PaymentService() { System.out.println("LAB: creating PaymentService"); }

    @Autowired
    void setOrders(OrderService orders) { this.orders = orders; }

    void charge(int id) { orders.markPaid(id); }
}
