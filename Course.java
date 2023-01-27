import java.util.ArrayList;
import java.util.List;

public class Course {
    public String courseId;
    public Integer totalCapacity;
    public Integer numOfStudents;
    public ArrayList<Student> enrolledStudents = new ArrayList<>();

    public Course(String courseId, int capacity){
        this.totalCapacity = capacity;
        this.courseId = courseId;
        this.numOfStudents = 0;
    }

    public boolean enrollStudent_RAND(Student student, int givenToken){
        if(this.enrolledStudents.contains(student)){
            return false;
        }
        if(this.numOfStudents < totalCapacity){
            this.numOfStudents++;
            enrolledStudents.add(student);
            return true;
        }
        return false;
    }

    public boolean enrollStudent_TATEC(Student student, int givenToken){
        if(this.numOfStudents < totalCapacity){
            this.numOfStudents++;
            enrolledStudents.add(student);
            return true;
        }
        return false;
    }

    public String getCourseNameAndEnrolledStudents(){
        final String[] courseInfo = {this.courseId};
        this.enrolledStudents.stream().forEach(student -> {
            courseInfo[0] = courseInfo[0].concat("," + student.studentId);
        });
        return courseInfo[0];
    }
}
