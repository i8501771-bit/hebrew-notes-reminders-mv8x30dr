package com.generated.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NoteEditActivity extends Activity {

    public static final String EXTRA_NOTE_ID = "extra_note_id";

    private DatabaseHelper mDb;
    private Note mNote;

    private LinearLayout mRootLayout;
    private EditText mEtTitle;
    private EditText mEtContent;
    private TextView mTvNoteDualDate;
    private TextView mTvReminderText;
    private TextView mTvClearReminder;
    private LinearLayout mLayoutReminderChip;
    private ImageButton mBtnPin;
    private ImageButton mBtnDelete;
    private TextView mTabText;
    private TextView mTabChecklist;
    private LinearLayout mChecklistSection;
    private LinearLayout mChecklistItemsContainer;
    private EditText mEtNewChecklistItem;
    private LinearLayout mEditCategoryContainer;
    private LinearLayout mColorPaletteContainer;

    private boolean mIsChecklist = false;
    private final List<ChecklistItem> mChecklistItems = new ArrayList<>();

    private static class ChecklistItem {
        String text;
        boolean checked;

        ChecklistItem(String text, boolean checked) {
            this.text = text;
            this.checked = checked;
        }
    }

    private static final String[] CATEGORIES = {"כללי", "עבודה", "אישי", "קניות", "רעיונות"};

    private static final int[] NOTE_COLORS = {
            0xFFFFFFFF, // לבן
            0xFFFFF9C4, // צהוב רך
            0xFFE8F5E9, // ירוק בהיר
            0xFFE3F2FD, // כחול בהיר
            0xFFFCE4EC, // ורוד פסטל
            0xFFF3E5F5  // סגול לבנדר
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        mDb = DatabaseHelper.getInstance(this);

        initViews();

        long noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);
        if (noteId > 0) {
            mNote = mDb.getNote(noteId);
        }

        if (mNote == null) {
            mNote = new Note();
            mBtnDelete.setVisibility(View.GONE);
        }

        bindNoteToUI();
        setupEventListeners();
    }

    private void initViews() {
        mRootLayout = findViewById(R.id.edit_root_layout);
        mEtTitle = findViewById(R.id.et_title);
        mEtContent = findViewById(R.id.et_content);
        mTvNoteDualDate = findViewById(R.id.tv_note_dual_date);
        mTvReminderText = findViewById(R.id.tv_reminder_text);
        mTvClearReminder = findViewById(R.id.tv_clear_reminder);
        mLayoutReminderChip = findViewById(R.id.layout_reminder_chip);
        mBtnPin = findViewById(R.id.btn_pin);
        mBtnDelete = findViewById(R.id.btn_delete);
        mTabText = findViewById(R.id.tab_text);
        mTabChecklist = findViewById(R.id.tab_checklist);
        mChecklistSection = findViewById(R.id.checklist_section);
        mChecklistItemsContainer = findViewById(R.id.checklist_items_container);
        mEtNewChecklistItem = findViewById(R.id.et_new_checklist_item);
        mEditCategoryContainer = findViewById(R.id.edit_category_container);
        mColorPaletteContainer = findViewById(R.id.color_palette_container);
    }

    private void bindNoteToUI() {
        mEtTitle.setText(mNote.title);
        mIsChecklist = mNote.isChecklist;

        if (mIsChecklist) {
            parseChecklistFromContent(mNote.content);
            renderChecklist();
            showChecklistView(true);
        } else {
            mEtContent.setText(mNote.content);
            showChecklistView(false);
        }

        // Date Display
        long timestamp = mNote.updatedAt > 0 ? mNote.updatedAt : System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("he", "IL"));
        HebrewDateHelper.HebrewDate hDate = HebrewDateHelper.getHebrewDate(cal);
        mTvNoteDualDate.setText(sdf.format(cal.getTime()) + " | " + hDate.format());

        updatePinButton();
        updateReminderUI();
        renderCategorySelector();
        renderColorPalette();
        applyBackgroundColor(mNote.color);
    }

    private void setupEventListeners() {
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndFinish();
            }
        });

        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndFinish();
            }
        });

        mBtnPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNote.isPinned = !mNote.isPinned;
                updatePinButton();
                Toast.makeText(NoteEditActivity.this,
                        mNote.isPinned ? "הפתק הוצמד" : "ההצמדה בוטלה", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        mLayoutReminderChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReminderPicker();
            }
        });

        mTvClearReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelReminder();
            }
        });

        mTabText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsChecklist) {
                    // Convert checklist items to plain text
                    mEtContent.setText(generateChecklistContent());
                    mIsChecklist = false;
                    showChecklistView(false);
                }
            }
        });

        mTabChecklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsChecklist) {
                    parseChecklistFromContent(mEtContent.getText().toString());
                    renderChecklist();
                    mIsChecklist = true;
                    showChecklistView(true);
                }
            }
        });

        findViewById(R.id.btn_add_checklist_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChecklistItem();
            }
        });

        mEtNewChecklistItem.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    addChecklistItem();
                    return true;
                }
                return false;
            }
        });
    }

    private void showChecklistView(boolean isChecklist) {
        if (isChecklist) {
            mTabText.setBackgroundResource(0);
            mTabText.setTextColor(0xFF555555);
            mTabChecklist.setBackgroundResource(R.drawable.bg_chip_selected);
            mTabChecklist.setTextColor(Color.WHITE);
            mEtContent.setVisibility(View.GONE);
            mChecklistSection.setVisibility(View.VISIBLE);
        } else {
            mTabText.setBackgroundResource(R.drawable.bg_chip_selected);
            mTabText.setTextColor(Color.WHITE);
            mTabChecklist.setBackgroundResource(0);
            mTabChecklist.setTextColor(0xFF555555);
            mEtContent.setVisibility(View.VISIBLE);
            mChecklistSection.setVisibility(View.GONE);
        }
    }

    private void addChecklistItem() {
        String text = mEtNewChecklistItem.getText().toString().trim();
        if (!text.isEmpty()) {
            mChecklistItems.add(new ChecklistItem(text, false));
            mEtNewChecklistItem.setText("");
            renderChecklist();
        }
    }

    private void parseChecklistFromContent(String text) {
        mChecklistItems.clear();
        if (text == null || text.trim().isEmpty()) return;
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            boolean checked = line.startsWith("[x]") || line.startsWith("[X]");
            String itemText = line.replaceAll("^\\[[ xX]\\]", "").trim();
            mChecklistItems.add(new ChecklistItem(itemText, checked));
        }
    }

    private String generateChecklistContent() {
        StringBuilder sb = new StringBuilder();
        for (ChecklistItem item : mChecklistItems) {
            sb.append(item.checked ? "[x] " : "[ ] ").append(item.text).append("\n");
        }
        return sb.toString();
    }

    private void renderChecklist() {
        mChecklistItemsContainer.removeAllViews();
        for (int i = 0; i < mChecklistItems.size(); i++) {
            final int index = i;
            final ChecklistItem item = mChecklistItems.get(i);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(4, 6, 4, 6);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setChecked(item.checked);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.checked = isChecked;
                }
            });
            row.addView(checkBox);

            final EditText etItem = new EditText(this);
            etItem.setText(item.text);
            etItem.setTextSize(15);
            etItem.setTextColor(item.checked ? 0xFF888888 : 0xFF212121);
            etItem.setBackground(null);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            etItem.setLayoutParams(itemParams);
            etItem.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        item.text = etItem.getText().toString().trim();
                    }
                }
            });
            row.addView(etItem);

            // Delete item button
            TextView btnRemove = new TextView(this);
            btnRemove.setText("✕");
            btnRemove.setTextSize(16);
            btnRemove.setTextColor(0xFFB0BEC5);
            btnRemove.setPadding(16, 8, 16, 8);
            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChecklistItems.remove(index);
                    renderChecklist();
                }
            });
            row.addView(btnRemove);

            mChecklistItemsContainer.addView(row);
        }
    }

    private void updatePinButton() {
        if (mNote.isPinned) {
            mBtnPin.setColorFilter(0xFF2E7D32);
        } else {
            mBtnPin.setColorFilter(0xFF9E9E9E);
        }
    }

    private void updateReminderUI() {
        if (mNote.reminderTime > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(mNote.reminderTime);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d/M/yyyy HH:mm", new Locale("he", "IL"));
            HebrewDateHelper.HebrewDate hDate = HebrewDateHelper.getHebrewDate(cal);

            mTvReminderText.setText(getString(R.string.reminder_set_for) + " " + sdf.format(cal.getTime())
                    + " (" + hDate.formatShort() + ")");
            mTvClearReminder.setVisibility(View.VISIBLE);
        } else {
            mTvReminderText.setText(R.string.set_reminder);
            mTvClearReminder.setVisibility(View.GONE);
        }
    }

    private void showReminderPicker() {
        final Calendar current = Calendar.getInstance();
        if (mNote.reminderTime > System.currentTimeMillis()) {
            current.setTimeInMillis(mNote.reminderTime);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(NoteEditActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        Calendar chosen = Calendar.getInstance();
                                        chosen.set(year, month, dayOfMonth, hourOfDay, minute, 0);

                                        if (chosen.getTimeInMillis() <= System.currentTimeMillis()) {
                                            Toast.makeText(NoteEditActivity.this,
                                                    "אנא בחרו מועד עתידי לתזכורת", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        mNote.reminderTime = chosen.getTimeInMillis();
                                        updateReminderUI();
                                    }
                                },
                                current.get(Calendar.HOUR_OF_DAY),
                                current.get(Calendar.MINUTE),
                                true);
                        timePickerDialog.show();
                    }
                },
                current.get(Calendar.YEAR),
                current.get(Calendar.MONTH),
                current.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void cancelReminder() {
        mNote.reminderTime = 0;
        updateReminderUI();
        if (mNote.id > 0) {
            cancelAlarm(mNote.id);
        }
        Toast.makeText(this, "התזכורת בוטלה", Toast.LENGTH_SHORT).show();
    }

    private void renderCategorySelector() {
        mEditCategoryContainer.removeAllViews();
        for (final String cat : CATEGORIES) {
            final TextView chip = new TextView(this);
            chip.setText(cat);
            chip.setTextSize(13);
            chip.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(6, 0, 6, 0);
            chip.setLayoutParams(params);
            chip.setPadding(28, 12, 28, 12);

            boolean isSelected = cat.equals(mNote.category);
            GradientDrawable gd = new GradientDrawable();
            gd.setCornerRadius(30);
            if (isSelected) {
                gd.setColor(0xFF2E7D32);
                chip.setTextColor(Color.WHITE);
            } else {
                gd.setColor(0xFFF1F3F4);
                chip.setTextColor(0xFF424242);
            }
            chip.setBackground(gd);

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNote.category = cat;
                    renderCategorySelector();
                }
            });

            mEditCategoryContainer.addView(chip);
        }
    }

    private void renderColorPalette() {
        mColorPaletteContainer.removeAllViews();
        for (final int color : NOTE_COLORS) {
            final View colorCircle = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(70, 70);
            params.setMargins(8, 0, 8, 0);
            colorCircle.setLayoutParams(params);

            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(color);
            if (color == mNote.color) {
                gd.setStroke(6, 0xFF2E7D32);
            } else {
                gd.setStroke(2, 0xFFBDBDBD);
            }
            colorCircle.setBackground(gd);

            colorCircle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNote.color = color;
                    applyBackgroundColor(color);
                    renderColorPalette();
                }
            });

            mColorPaletteContainer.addView(colorCircle);
        }
    }

    private void applyBackgroundColor(int color) {
        mRootLayout.setBackgroundColor(color != 0 ? color : Color.WHITE);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_note)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mNote.id > 0) {
                            cancelAlarm(mNote.id);
                            mDb.deleteNote(mNote.id);
                        }
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveAndFinish() {
        String title = mEtTitle.getText().toString().trim();
        String content;

        if (mIsChecklist) {
            content = generateChecklistContent().trim();
        } else {
            content = mEtContent.getText().toString().trim();
        }

        // If title and content are both empty, do not save an empty stub
        if (title.isEmpty() && content.isEmpty()) {
            finish();
            return;
        }

        mNote.title = title;
        mNote.content = content;
        mNote.isChecklist = mIsChecklist;
        mNote.updatedAt = System.currentTimeMillis();

        long noteId = mDb.saveNote(mNote);

        if (mNote.reminderTime > System.currentTimeMillis()) {
            scheduleAlarm(noteId, mNote.title, mNote.content, mNote.reminderTime);
        } else if (mNote.reminderTime == 0 && noteId > 0) {
            cancelAlarm(noteId);
        }

        Toast.makeText(this, R.string.note_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void scheduleAlarm(long noteId, String title, String content, long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_NOTE_ID, noteId);
        intent.putExtra(ReminderReceiver.EXTRA_NOTE_TITLE, title);
        intent.putExtra(ReminderReceiver.EXTRA_NOTE_CONTENT, content);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, (int) noteId, intent, flags);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void cancelAlarm(long noteId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, (int) noteId, intent, flags);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onBackPressed() {
        saveAndFinish();
    }
}
