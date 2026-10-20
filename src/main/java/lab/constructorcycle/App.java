package lab.constructorcycle;

import java.util.HashMap;
import java.util.Map;
import lab.Demo;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

// The cycle: OrderService needs PaymentService to charge; PaymentService needs OrderService to mark the order paid.
// Both ask for the other in their constructors.
@SpringBootApplication
public class App {}

@Service
class OrderService implements Demo {
    private final PaymentService payment;
    private final Map<Integer, String> status = new HashMap<>();

    OrderService(PaymentService payment) {
        System.out.println("LAB: creating OrderService");
        this.payment = payment;
    }

    public String placeOrder(int id) {
        status.put(id, "NEW");
        payment.charge(id);
        return "order " + id + ": " + status.get(id);
    }

    void markPaid(int id) { status.put(id, "PAID"); }
}

@Service
class PaymentService {
    private final OrderService orders;

    PaymentService(OrderService orders) {
        System.out.println("LAB: creating PaymentService");
        this.orders = orders;
    }

    void charge(int id) {
        // a real app would charge the card here
        orders.markPaid(id);
    }
}
