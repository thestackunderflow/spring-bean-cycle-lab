package lab.mixedconstructorfirst;

import java.util.HashMap;
import java.util.Map;
import lab.Demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

// The same mixed loop, the other way round: OrderService takes PaymentService through its CONSTRUCTOR, PaymentService
// takes OrderService through a FIELD. Spring creates OrderService first here, so there is no half-built OrderService
// to hand out.
@SpringBootApplication
public class App {}

@Service
class OrderService implements Demo {
    private final PaymentService payment;
    private final Map<Integer, String> status = new HashMap<>();

    OrderService(PaymentService payment) {
        System.out.println("LAB: creating OrderService (constructor)");
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
    @Autowired private OrderService orders;

    PaymentService() { System.out.println("LAB: creating PaymentService (field)"); }

    void charge(int id) { orders.markPaid(id); }
}
