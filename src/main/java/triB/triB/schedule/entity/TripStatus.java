package triB.triB.schedule.entity;

public enum TripStatus {
    ACCEPTED("승인됨"),
    READY("준비중");
    
    private final String description;
    
    TripStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
