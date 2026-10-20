package lab;

/** What every scenario's OrderService can do, so CycleLab can call it the same way. */
public interface Demo {
    String placeOrder(int id);

    /** The class of the PaymentService object OrderService actually holds (shows @Lazy's stand-in). */
    default String paymentClass() { return ""; }
}
