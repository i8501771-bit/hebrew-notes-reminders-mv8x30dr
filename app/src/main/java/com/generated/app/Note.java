package com.generated.app;

public class Note {
    public long id;
    public String title;
    public String content;
    public String category; // כללי, עבודה, אישי, קניות, רעיונות
    public int color; // Hex color int
    public boolean isPinned;
    public boolean isChecklist;
    public long reminderTime; // 0 if no reminder
    public long updatedAt;

    public Note() {
        this.title = "";
        this.content = "";
        this.category = "כללי";
        this.color = 0xFFFFFFFF;
        this.isPinned = false;
        this.isChecklist = false;
        this.reminderTime = 0;
        this.updatedAt = System.currentTimeMillis();
    }
}
