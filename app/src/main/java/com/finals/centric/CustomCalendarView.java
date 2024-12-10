package com.finals.centric;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomCalendarView extends LinearLayout {
    // Basic calendar fields
    private Calendar selectedDate;
    private Calendar currentMonth;
    private Calendar today = Calendar.getInstance();
    private String[] weekdays = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    // UI Components
    private TextView monthYearText;
    private GridView calendarGrid;
    private GridView weekdayGrid;
    private CalendarAdapter adapter;

    // Booking related fields
    private List<Calendar> bookedDates = new ArrayList<>();
    private Map<String, String[]> bookingDateRanges = new HashMap<>();
    private int selectedDays = 1;


    // Listeners
    private OnDateSelectedListener dateSelectedListener;
    private OnBookedDateClickListener bookedDateClickListener;

    // Interfaces
    public interface OnDateSelectedListener {
        void onDateSelected(Calendar date);
    }

    public interface OnBookedDateClickListener {
        void onBookedDateClick(String checkIn, String checkOut);
    }

    // Constructors
    public CustomCalendarView(Context context) {
        super(context);
        init();
    }

    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Setter methods
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateSelectedListener = listener;
    }

    public void setOnBookedDateClickListener(OnBookedDateClickListener listener) {
        this.bookedDateClickListener = listener;
    }

    public void setBookedDates(List<Calendar> dates) {
        this.bookedDates = dates;
        adapter.notifyDataSetChanged();
    }

    public void setBookedDatesWithRanges(Map<String, String[]> dateRanges) {
        this.bookingDateRanges = dateRanges;
        adapter.notifyDataSetChanged();
    }

    public void setSelectedDays(int days) {
        this.selectedDays = days;
        adapter.notifyDataSetChanged();
    }

    private int getSelectedDays() {
        return selectedDays;
    }

    // Helper methods
    private String[] getBookingDatesForDate(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String dateStr = sdf.format(date.getTime());
        return bookingDateRanges.get(dateStr);
    }

    private void updateNavigationButtons() {
        boolean isCurrentMonth = currentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && currentMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH);
        findViewById(R.id.previousMonth).setVisibility(isCurrentMonth ? View.INVISIBLE : View.VISIBLE);
    }

    private void init() {
        selectedDate = Calendar.getInstance();
        currentMonth = Calendar.getInstance();
        selectedDays = 1;

        LayoutInflater.from(getContext()).inflate(R.layout.custom_calendar_view, this, true);

        monthYearText = findViewById(R.id.monthYearText);
        calendarGrid = findViewById(R.id.calendarGrid);
        weekdayGrid = findViewById(R.id.weekdayGrid);

        ImageButton prevButton = findViewById(R.id.previousMonth);
        ImageButton nextButton = findViewById(R.id.nextMonth);

        prevButton.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextButton.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        WeekdayAdapter weekdayAdapter = new WeekdayAdapter();
        weekdayGrid.setAdapter(weekdayAdapter);

        adapter = new CalendarAdapter();
        calendarGrid.setAdapter(adapter);

        updateCalendar();
    }

    private void updateCalendar() {
        List<Calendar> dates = getDatesForMonth();
        adapter.updateDates(dates);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthYearText.setText(dateFormat.format(currentMonth.getTime()));
        updateNavigationButtons();
    }

    private List<Calendar> getDatesForMonth() {
        List<Calendar> dates = new ArrayList<>();
        Calendar calendar = (Calendar) currentMonth.clone();

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        calendar.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek);

        while (dates.size() < 42) {
            dates.add((Calendar) calendar.clone());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }

    private class WeekdayAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return weekdays.length;
        }

        @Override
        public String getItem(int position) {
            return weekdays[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = new TextView(getContext());
                textView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setGravity(Gravity.CENTER);
                textView.setPadding(8, 8, 8, 8);
                textView.setTextColor(Color.GRAY);
                textView.setTextSize(12);
            } else {
                textView = (TextView) convertView;
            }

            textView.setText(weekdays[position]);
            return textView;
        }
    }

    private class CalendarAdapter extends BaseAdapter {
        private List<Calendar> dates = new ArrayList<>();

        public void updateDates(List<Calendar> newDates) {
            dates = newDates;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return dates.size();
        }

        @Override
        public Calendar getItem(int position) {
            return dates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FrameLayout frameLayout;
            if (convertView == null) {
                frameLayout = new FrameLayout(getContext());
                int cellSize = getResources().getDimensionPixelSize(R.dimen.calendar_cell_size);
                frameLayout.setLayoutParams(new GridView.LayoutParams(cellSize, cellSize));

                TextView dayView = new TextView(getContext());
                FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                dayView.setLayoutParams(textParams);
                dayView.setGravity(Gravity.CENTER);
                dayView.setTag("dateText");

                View dotView = new View(getContext());
                int dotSize = getResources().getDimensionPixelSize(R.dimen.dot_size);
                FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dotSize, dotSize);
                dotParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                dotParams.topMargin = 4;
                dotView.setLayoutParams(dotParams);
                dotView.setBackgroundResource(R.drawable.red_dot);
                dotView.setTag("dot");

                frameLayout.addView(dayView);
                frameLayout.addView(dotView);

                // Add blue dot view
                View blueDotView = new View(getContext());
                FrameLayout.LayoutParams blueDotParams = new FrameLayout.LayoutParams(dotSize, dotSize);
                blueDotParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                blueDotParams.bottomMargin = 4;
                blueDotView.setLayoutParams(blueDotParams);
                blueDotView.setBackgroundResource(R.drawable.blue_dot);
                blueDotView.setTag("blueDot");

                frameLayout.addView(blueDotView);
            } else {
                frameLayout = (FrameLayout) convertView;
            }

            TextView dayView = frameLayout.findViewWithTag("dateText");
            View dotView = frameLayout.findViewWithTag("dot");
            Calendar date = dates.get(position);

            dayView.setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));

            if (date.before(today)) {
                dayView.setTextColor(Color.LTGRAY);
                dayView.setEnabled(false);
                dotView.setVisibility(View.GONE);
            } else {
                boolean isBooked = isDateBooked(date);
                dotView.setVisibility(isBooked ? View.VISIBLE : View.GONE);

                if (date.get(Calendar.MONTH) != currentMonth.get(Calendar.MONTH)) {
                    dayView.setTextColor(Color.LTGRAY);
                } else {
                    dayView.setTextColor(Color.BLACK);
                }

                frameLayout.setOnClickListener(v -> {
                    if (date.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)) {
                        if (isDateBooked(date) && bookedDateClickListener != null) {
                            // For booked dates - only show booking info
                            String[] bookingDates = getBookingDatesForDate(date);
                            if (bookingDates != null) {
                                bookedDateClickListener.onBookedDateClick(bookingDates[0], bookingDates[1]);
                            }
                            selectedDate = date; // For highlighting only
                        } else {
                            // For available dates - update selection and form
                            selectedDate = date;
                            if (dateSelectedListener != null) {
                                dateSelectedListener.onDateSelected(date);
                            }
                        }
                        notifyDataSetChanged();
                    }
                });
            }

            if (isSameDate(date, selectedDate)) {
                if (isDateBooked(date)) {
                    dayView.setBackgroundResource(R.drawable.booked_date_background);
                } else {
                    dayView.setBackgroundResource(R.drawable.selected_date_background);
                }
                dayView.setTextColor(Color.WHITE);
            } else {
                dayView.setBackgroundResource(android.R.color.transparent);
            }

            // In getView method
            View blueDotView = frameLayout.findViewWithTag("blueDot");

            // Show blue dot based on selected date range, regardless of booking status
            if (selectedDate != null) {
                Calendar endDate = (Calendar) selectedDate.clone();
                endDate.add(Calendar.DAY_OF_MONTH, selectedDays);

                if (date.compareTo(selectedDate) >= 1 && date.compareTo(endDate) < 1) {
                    blueDotView.setVisibility(View.VISIBLE);
                } else {
                    blueDotView.setVisibility(View.GONE);
                }
            } else {
                blueDotView.setVisibility(View.GONE);
            }



            return frameLayout;
        }

        private boolean isDateBooked(Calendar date) {
            for (Calendar bookedDate : bookedDates) {
                if (isSameDate(date, bookedDate)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isSameDate(Calendar date1, Calendar date2) {
            return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                    date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                    date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH);
        }
    }

    public boolean hasBookingOverlap(Calendar startDate, int days) {
        // Strip time from start date
        Calendar start = (Calendar) startDate.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);

        // Calculate end date and strip time
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, days);

        for (Calendar bookedDate : bookedDates) {
            // Strip time from booked date for comparison
            Calendar bookedCal = (Calendar) bookedDate.clone();
            bookedCal.set(Calendar.HOUR_OF_DAY, 0);
            bookedCal.set(Calendar.MINUTE, 0);

            if (bookedCal.compareTo(start) >= 1 && bookedCal.compareTo(end) < 1) {
                return true;
            }
        }
        return false;
    }

}
