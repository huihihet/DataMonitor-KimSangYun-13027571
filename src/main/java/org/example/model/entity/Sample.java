package org.example.model.entity;

public class Sample {
    private final Long sampleId;
    private final String name;
    private final int avgProductionTime;
    private final double yield;
    private final int stock;

    public Sample(Long sampleId, String name, int avgProductionTime, double yield, int stock) {
        if (sampleId == null) throw new IllegalArgumentException("시료 ID는 null일 수 없습니다.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("시료 이름은 공백일 수 없습니다.");
        if (avgProductionTime < 1) throw new IllegalArgumentException("평균 생산 시간은 1 이상이어야 합니다.");
        if (yield <= 0.0 || yield > 1.0) throw new IllegalArgumentException("수율은 0.0 초과 1.0 이하이어야 합니다.");
        if (stock < 0) throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다.");
        this.sampleId = sampleId;
        this.name = name;
        this.avgProductionTime = avgProductionTime;
        this.yield = yield;
        this.stock = stock;
    }

    public Long getSampleId() { return sampleId; }
    public String getName() { return name; }
    public int getAvgProductionTime() { return avgProductionTime; }
    public double getYield() { return yield; }
    public int getStock() { return stock; }
}
