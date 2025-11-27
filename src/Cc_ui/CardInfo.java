package Cc_ui;

public class CardInfo {
    private String name;
    private String description;
    private int damage;
    private boolean active; // <-- new

    public CardInfo(String name, String description, int damage) {
        this.name = name;
        this.description = description;
        this.damage = damage;
        this.active = true; // card starts visible
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDamage() { return damage; }
}
