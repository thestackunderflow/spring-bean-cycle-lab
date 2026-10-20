package lab.fieldcycle;

import java.util.HashMap;
import java.util.Map;
import lab.Demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

// The same cycle, with field injection: Spring creates each object first, then fills the fields.
@SpringBootApplication
public class App {}

@Service
class OrderService implements Demo {
    @Autowired private PaymentService payment;
    private final Map<Integer, String> status = new HashMap<>();

    OrderService() { System.out.println("LAB: creating OrderService"); }

    public String placeOrder(int id) {
        status.put(id, "NEW");
        payment.charge(id);
        return "order " + id + ": " + status.get(id);
    }

    void markPaid(int id) { status.put(id, "PAID"); }
}

@Service
class PaymentService {
    @Autowired private OrderService orders;

    PaymentService() { System.out.println("LAB: creating PaymentService"); }

    void charge(int id) { orders.markPaid(id); }
}
