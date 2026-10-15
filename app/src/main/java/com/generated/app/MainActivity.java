package com.generated.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private DatabaseHelper mDb;
    private LinearLayout mNotesContainer;
    private LinearLayout mCategoryTabsContainer;
    private LinearLayout mEmptyLayout;
    private TextView mNotesCountView;
    private TextView mDualDateView;
    private EditText mEtSearch;

    private String mCurrentCategory = "הכל";
    private String mCurrentSearch = "";

    private static final String[] CATEGORIES = {
            "הכל", "כללי", "עבודה", "אישי", "קניות", "רעיונות"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDb = DatabaseHelper.getInstance(this);

        mNotesContainer = findViewById(R.id.notes_container);
        mCategoryTabsContainer = findViewById(R.id.category_tabs_container);
        mEmptyLayout = findViewById(R.id.layout_empty);
        mNotesCountView = findViewById(R.id.tv_notes_count);
        mDualDateView = findViewById(R.id.tv_dual_date);
        mEtSearch = findViewById(R.id.et_search);

        ImageButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                startActivity(intent);
            }
        });

        setupHeaderDate();
        setupCategoryTabs();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void setupHeaderDate() {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("he", "IL"));
        String gregorianDate = sdf.format(now.getTime());

        HebrewDateHelper.HebrewDate hDate = HebrewDateHelper.getHebrewDate(now);
        String hebrewDate = hDate.format();

        mDualDateView.setText(gregorianDate + " • " + hebrewDate);
    }

    private void setupCategoryTabs() {
        mCategoryTabsContainer.removeAllViews();
        for (final String cat : CATEGORIES) {
            final TextView chip = new TextView(this);
            chip.setText(cat);
            chip.setTextSize(13);
            chip.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(6, 0, 6, 0);
            chip.setLayoutParams(params);
            chip.setPadding(32, 14, 32, 14);

            updateChipStyle(chip, cat.equals(mCurrentCategory));

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentCategory = cat;
                    setupCategoryTabs();
                    loadNotes();
                }
            });

            mCategoryTabsContainer.addView(chip);
        }
    }

    private void updateChipStyle(TextView chip, boolean isSelected) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(32);
        if (isSelected) {
            gd.setColor(0xFF2E7D32);
            chip.setTextColor(Color.WHITE);
        } else {
            gd.setColor(0xFFF1F3F4);
            gd.setStroke(2, 0xFFE0E0E0);
            chip.setTextColor(0xFF424242);
        }
        chip.setBackground(gd);
    }

    private void setupSearch() {
        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCurrentSearch = s.toString();
                loadNotes();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadNotes() {
        mNotesContainer.removeAllViews();
        List<Note> notes = mDb.getAllNotes(mCurrentCategory, mCurrentSearch);

        mNotesCountView.setText(notes.size() + " פתקים");

        if (notes.isEmpty()) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            return;
        } else {
            mEmptyLayout.setVisibility(View.GONE);
        }

        for (final Note note : notes) {
            View noteCard = createNoteCard(note);
            mNotesContainer.addView(noteCard);
        }
    }

    private View createNoteCard(final Note note) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setPadding(28, 24, 28, 24);

        // Card background styling
        GradientDrawable gd = new GradientDrawable();
        int baseColor = note.color != 0 ? note.color : 0xFFFFFFFF;
        gd.setColor(baseColor);
        gd.setCornerRadius(20);
        gd.setStroke(note.isPinned ? 4 : 1, note.isPinned ? 0xFF2E7D32 : 0xFFE0E0E0);
        card.setBackground(gd);
        card.setElevation(3f);

        // Top Row: Pinned Icon + Category Badge + Reminder Badge
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (note.isPinned) {
            ImageView pinIcon = new ImageView(this);
            pinIcon.setImageResource(R.drawable.ic_pin);
            LinearLayout.LayoutParams pinParams = new LinearLayout.LayoutParams(28, 28);
            pinParams.setMargins(0, 0, 8, 0);
            pinIcon.setLayoutParams(pinParams);
            topRow.addView(pinIcon);
        }

        // Category Tag
        TextView catTag = new TextView(this);
        catTag.setText(note.category);
        catTag.setTextSize(11);
        catTag.setTextColor(0xFF388E3C);
        catTag.setPadding(16, 6, 16, 6);
        GradientDrawable catBg = new GradientDrawable();
        catBg.setColor(0x224CAF50);
        catBg.setCornerRadius(12);
        catTag.setBackground(catBg);
        topRow.addView(catTag);

        // Push space
        View space = new View(this);
        LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(0, 1, 1f);
        space.setLayoutParams(spaceParams);
        topRow.addView(space);

        // Reminder indicator if set
        if (note.reminderTime > 0) {
            LinearLayout reminderBadge = new LinearLayout(this);
            reminderBadge.setOrientation(LinearLayout.HORIZONTAL);
            reminderBadge.setGravity(Gravity.CENTER_VERTICAL);
            reminderBadge.setPadding(12, 4, 12, 4);
            GradientDrawable remBg = new GradientDrawable();
            remBg.setColor(0xFFFFF3E0);
            remBg.setStroke(1, 0xFFFFB74D);
            remBg.setCornerRadius(12);
            reminderBadge.setBackground(remBg);

            ImageView remIcon = new ImageView(this);
            remIcon.setImageResource(R.drawable.ic_bell);
            reminderBadge.addView(remIcon, new LinearLayout.LayoutParams(24, 24));

            TextView remText = new TextView(this);
            SimpleDateFormat remFormat = new SimpleDateFormat("d/M HH:mm", Locale.getDefault());
            remText.setText(" " + remFormat.format(new Date(note.reminderTime)));
            remText.setTextSize(11);
            remText.setTextColor(0xFFE65100);
            reminderBadge.addView(remText);

            topRow.addView(reminderBadge);
        }

        card.addView(topRow);

        // Title
        if (note.title != null && !note.title.trim().isEmpty()) {
            TextView tvTitle = new TextView(this);
            tvTitle.setText(note.title);
            tvTitle.setTextSize(17);
            tvTitle.setTextColor(0xFF212121);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ((LinearLayout.LayoutParams) tvTitle.getLayoutParams()).setMargins(0, 12, 0, 4);
            card.addView(tvTitle);
        }

        // Preview Content
        if (note.content != null && !note.content.trim().isEmpty()) {
            TextView tvContent = new TextView(this);
            String preview = note.content;
            if (note.isChecklist) {
                preview = formatChecklistPreview(note.content);
            }
            tvContent.setText(preview);
            tvContent.setMaxLines(3);
            tvContent.setTextSize(14);
            tvContent.setTextColor(0xFF555555);
            tvContent.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ((LinearLayout.LayoutParams) tvContent.getLayoutParams()).setMargins(0, 4, 0, 10);
            card.addView(tvContent);
        }

        // Bottom Row: Hebrew date stamp
        TextView tvDate = new TextView(this);
        HebrewDateHelper.HebrewDate hDate = HebrewDateHelper.getHebrewDate(note.updatedAt);
        tvDate.setText(hDate.formatShort());
        tvDate.setTextSize(11);
        tvDate.setTextColor(0xFF888888);
        card.addView(tvDate);

        // Click to edit
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                intent.putExtra(NoteEditActivity.EXTRA_NOTE_ID, note.id);
                startActivity(intent);
            }
        });

        return card;
    }

    private String formatChecklistPreview(String checklistData) {
        StringBuilder sb = new StringBuilder();
        String[] lines = checklistData.split("\n");
        int count = 0;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            boolean checked = line.startsWith("[x]") || line.startsWith("[X]");
            String itemText = line.replaceAll("^\\[[ xX]\\]", "").trim();
            sb.append(checked ? "☑ " : "☐ ").append(itemText).append("\n");
            count++;
            if (count >= 3) break;
        }
        return sb.toString().trim();
    }
}
