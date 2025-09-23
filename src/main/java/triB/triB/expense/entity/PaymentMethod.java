package triB.triB.expense.entity;

public enum PaymentMethod {
    TOGETHER("함께결제"),
    SEPARATE("각자결제");
    
    private final String description;
    
    PaymentMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
