package org.cpen321.discovr.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.cpen321.discovr.R;
import org.cpen321.discovr.SQLiteDBHandler;
import org.cpen321.discovr.model.Course;
import org.cpen321.discovr.utility.AlertUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;
import static org.cpen321.discovr.R.dimen.button_margin;
import static org.cpen321.discovr.R.id.left;
import static org.cpen321.discovr.parser.CalendarFileParser.loadUserCourses;

/**
 * Created by zhangyueyue on 2016-11-08.
 */

public class CoursesFragment extends Fragment {
    public CoursesFragment() {
        // Required empty public constructor
    }

    //for course time only
    //i got this worked on my phone... but i have no idea why- -
    private static String timeFormatter(long startTime) {
        String time = String.valueOf(startTime);
        String[] timeArray = time.split("");
        if (timeArray.length == 6) {
            return timeArray[0] + timeArray[1] + ":" + timeArray[2] + timeArray[3];
        } else {
            return timeArray[0] + timeArray[1] + timeArray[2] + ":" + timeArray[3] + timeArray[4];
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Get DBHandler for this activity
        SQLiteDBHandler dbh = new SQLiteDBHandler(this.getActivity());
        try {
            List<Course> flag = dbh.getAllCourses();
            if (flag.isEmpty()) {
                //load from local ical files
                List<Course> rawCourses = loadUserCourses();
                //Deal with the course duplicates here
                List<Course> myCourses = removeDuplicates(rawCourses);
                dbh.addCourses(myCourses);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Inflate the layout for this fragment
        final FrameLayout fm = (FrameLayout) inflater.inflate(R.layout.fragment_courses, container, false);
        ScrollView sv = (ScrollView) fm.getChildAt(0);

        //Get linearlayout and layoutParams for new button
        LinearLayout ll = (LinearLayout) sv.getChildAt(0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        List<Course> courseList = null;
        try {
            //get course object from local database
            courseList = dbh.getAllCourses();

            //Add new button for each course in DB
            for (Course course : courseList) {
                //formats button to be the same as the format we want in the fragment
                final Button button = createCourseButton(course);
                //Add this button to the layout
                ll.addView(button, lp);

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            //check if there is any class in 10 mins
            if (AlertUtil.courseAlert(courseList) != null) {
                //get the course if there is any
                Course currCourse = AlertUtil.courseAlert(courseList);
                //show the alert message
                new AlertDialog.Builder(this.getActivity())
                        //set alert title with the course name
                        .setTitle(currCourse.getCategory() + " " + currCourse.getNumber() + " " + currCourse.getSection() + " will start in 10 mins")
                        //set the message with the location
                        .setMessage(currCourse.getBuilding() + " " + currCourse.getRoom())
                        //show the alert
                        .show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return fm;
    }

    private Button createCourseButton(Course course) {
        //Set button properties - gravity, allCaps, padding, backgroundColor, textColor, text
        Button bt = new Button(this.getActivity());

        //button.setId(course.get);
        bt.setGravity(left);
        bt.setAllCaps(false);
        bt.setPadding(getResources().getDimensionPixelSize(button_margin), getResources().getDimensionPixelSize(button_margin), getResources().getDimensionPixelSize(button_margin), getResources().getDimensionPixelSize(button_margin));
        bt.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_press_colors));
        bt.setTextColor(ContextCompat.getColor(getContext(), R.color.primaryTextColor));
        String startTime = timeFormatter(course.getStartTime());
        String endTime = timeFormatter(course.getEndTime());
        SpannableString buttonText = new SpannableString(course.getCategory() + " " + course.getNumber() + " " + course.getSection() + "\n" + course.getBuilding() + "\n" + (startTime + " - " + endTime) + " " + course.getRoom());

        //should i add startDate and endDate as well?
        //when it reaches the endDate ,then the course button automatically disappear
        int index = buttonText.toString().indexOf("\n");
        buttonText.setSpan(new RelativeSizeSpan(2), 0, index, SPAN_INCLUSIVE_INCLUSIVE);
        buttonText.setSpan(new RelativeSizeSpan((float) 1.5), index, buttonText.length(), SPAN_INCLUSIVE_INCLUSIVE);
        bt.setText(buttonText);
        return bt;
    }

    //remove duplicates method
    private List<Course> removeDuplicates(List<Course> rawCourses) {

        List<Course> result = new ArrayList<Course>();
        //List<String> myStrings = new ArrayList<String>();

        for (int i = 0; i < rawCourses.size(); i++) {
            String rTitle = rawCourses.get(i).getCategory() + rawCourses.get(i).getNumber() + rawCourses.get(i).getSection();
            label:
            if (result.isEmpty()) {
                result.add(rawCourses.get(i));
                //myStrings.add(rTitle);
            } else {
                for (int j = 0; j < result.size(); j++) {
                    String nTitle = result.get(j).getCategory() + result.get(j).getNumber() + result.get(j).getSection();
                    if (rTitle.equals(nTitle)) {
                        //String dow = scheduleResult.get(j).getDayOfWeek();
                        result.get(j).setDayOfWeek(result.get(j).getDayOfWeek() + "/" + rawCourses.get(i).getDayOfWeek());
                        break label;
                    }
                }
                result.add(rawCourses.get(i));
            }
        }
        return result;
    }
}