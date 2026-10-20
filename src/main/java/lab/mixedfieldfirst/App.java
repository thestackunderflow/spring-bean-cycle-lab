package lab.mixedfieldfirst;

import java.util.HashMap;
import java.util.Map;
import lab.Demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

// A mixed loop: OrderService takes PaymentService through a FIELD, PaymentService takes OrderService through its
// CONSTRUCTOR. Spring creates OrderService first here (the log shows the order).
@SpringBootApplication
public class App {}

@Service
class OrderService implements Demo {
    @Autowired private PaymentService payment;
    private final Map<Integer, String> status = new HashMap<>();

    OrderService() { System.out.println("LAB: creating OrderService (field)"); }

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
        System.out.println("LAB: creating PaymentService (constructor)");
        this.orders = orders;
    }

    void charge(int id) { orders.markPaid(id); }
}
