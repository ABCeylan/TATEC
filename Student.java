import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Student {
    public String studentId;
    public ArrayList<Course> enrolledCourses = new ArrayList<>();
    public ArrayList<Integer> preferenceList = new ArrayList<>();
    public Integer numOfCoursesWanted;
    public Double unhappiness = 0.;

    public Student(String studentId){
        this.studentId = studentId;
    }

    public Student(String studentId, int totalToken, ArrayList<Integer> preferenceList){
        this.studentId = studentId;
        this.preferenceList = preferenceList;
    }

    public void enrollToCourse_RAND(ArrayList<Course> courseList, Course course){
        int givenTokenToCourse = getGivenTokenToCourse(courseList, course);
        boolean enrollStatus = course.enrollStudent_RAND(this, givenTokenToCourse);
        if (enrollStatus == true){
            enrolledCourses.add(course);
        }
    }

    public void enrollToCourse_TATEC(ArrayList<Course> courseList, Course course){
        int givenTokenToCourse = getGivenTokenToCourse(courseList, course);

        if(givenTokenToCourse == 0) return;
        boolean enrollStatus = course.enrollStudent_TATEC(this, givenTokenToCourse);
        if (enrollStatus == true){
            enrolledCourses.add(course);
        }

    }

    public int getGivenTokenToCourse(ArrayList<Course> courseList, Course course){
        return IntStream.range(0, courseList.size()).map(courseIndex -> {
           if(courseList.get(courseIndex).courseId == course.courseId){
               return preferenceList.get(courseIndex);
           }
           return 0;
        }).sum();
    }

    public boolean checkIfStudentEnrolledToCourse(Course course){
        return enrolledCourses.stream().filter(enrolledCourse -> enrolledCourse.courseId == course.courseId).collect(Collectors.toList()).size() > 0;
    }

    public void calculateUnhappiness(ArrayList<Course> courseList, double h){
        final Double[] totalUnhappiness = {0.};
        final boolean[] assignedAtLeastOneLesson = {false};
        IntStream.range(0, this.preferenceList.size()).forEach(pCourseIndex -> {
            Double unhappiness = 0.;
            Course pCourse = courseList.get(pCourseIndex);
            Integer givenToken = this.preferenceList.get(pCourseIndex);
            boolean isStudentEnrolledToCourse = this.checkIfStudentEnrolledToCourse(pCourse);

            if(isStudentEnrolledToCourse) assignedAtLeastOneLesson[0] = true;

            if(givenToken != 0 && !isStudentEnrolledToCourse){
                unhappiness = (-100/h) * Math.log(1 - givenToken/100.);
                if(unhappiness > 100.) unhappiness = 100.;
            }
            totalUnhappiness[0] += unhappiness;


        });

        if(!assignedAtLeastOneLesson[0]) totalUnhappiness[0] *= totalUnhappiness[0];

        this.unhappiness = totalUnhappiness[0];
    }
}
