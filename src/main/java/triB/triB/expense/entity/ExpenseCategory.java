package triB.triB.expense.entity;

public enum ExpenseCategory {
    FOOD("식비"),
    ACCOMMODATION("숙박비"), 
    TRANSPORTATION("교통비"),
    TOURISM("관광/입장료"),
    ACTIVITY("액티비티/체험"),
    SHOPPING("쇼핑"),
    COMMUNICATION("통신"),
    DAILY_NECESSITIES("생필품"),
    ETC("기타");
    
    private final String description;
    
    ExpenseCategory(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
