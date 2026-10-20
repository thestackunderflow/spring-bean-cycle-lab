package lab.threebeancycle;

import java.util.HashMap;
import java.util.Map;
import lab.Demo;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

// A longer loop: OrderService -> PaymentService -> InvoiceService -> OrderService.
@SpringBootApplication
public class App {}

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

    void markPaid(int id) { status.put(id, "PAID"); }
}

@Service
class PaymentService {
    private final InvoiceService invoices;

    PaymentService(InvoiceService invoices) { this.invoices = invoices; }

    void charge(int id) { invoices.issue(id); }
}

@Service
class InvoiceService {
    private final OrderService orders;

    InvoiceService(OrderService orders) { this.orders = orders; }

    void issue(int id) { orders.markPaid(id); }
}
