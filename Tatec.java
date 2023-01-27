import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.Random;

import static java.lang.String.valueOf;

public class Tatec
{
    private static final int CORRECT_TOTAL_TOKEN_PER_STUDENT = 100;
    private static final String OUT_TATEC_UNHAPPY = "unhappyOutTATEC.txt";
    private static final String OUT_TATEC_ADMISSION = "admissionOutTATEC.txt";
    private static final String OUT_RAND_UNHAPPY = "unhappyOutRANDOM.txt";
    private static final String OUT_RAND_ADMISSION = "admissionOutRANDOM.txt";


    public static void main(String args[])
    {
        if(args.length < 4)
        {
            System.err.println("Not enough arguments!");
            return;
        }

        ArrayList<Course> courseList = new ArrayList<>();
        readCourses(args, courseList);

        ArrayList<Student> studentList = new ArrayList<>();
        readStudents(args, studentList);

        ArrayList<ArrayList<Integer>> tokenList = new ArrayList<>();
        readTokens(args, tokenList);

        createPreferenceList(studentList, tokenList);

        double h = 0.;

        try { h = Double.parseDouble(args[3]);}
        catch (NumberFormatException ex)
        {
            System.err.println("4th argument is not a double!");
        }

        final double[] h_constant = {h};

        // CHECK FOR TOTAL GIVEN TOKEN == 100 FOR EACH STUDENT
        final boolean[] error = {false};
        if(checkExceedingToken(error, studentList)){
            return;
        };

        assignStudentsRandomly(studentList, courseList);

        //CALCULATE HAPPINESS OF RANDOMLY ASSIGNED STUDENTS
        studentList.stream().forEach(student -> {
            student.calculateUnhappiness(courseList, h_constant[0]);
        });


        createRandomUnhappinessFile(studentList);

        createRandomAdmissionFile(courseList);

        ///////////// RANDOM ASSIGNMENT FINISHED \\\\\\\\\\\\\

        clearEnrollments(studentList, courseList);

        assignStudents(studentList, courseList);

        //CALCULATE HAPPINESS
        studentList.stream().forEach(student -> {
            student.calculateUnhappiness(courseList, h_constant[0]);
        });

        createTatecUnhappynessFile(studentList);

        createTATECAdmissionFile(courseList);

    }
    public static void readCourses(String args[], ArrayList<Course> courseList){
        String courseFilePath = args[0];
        try {
            Files.lines(Path.of(courseFilePath), StandardCharsets.UTF_8).forEach(courseLine -> {
                String[] courseIdAndCapacity = courseLine.split(",");
                Course course = new Course(courseIdAndCapacity[0], Integer.parseInt(courseIdAndCapacity[1].replaceAll(" ", "")));
                courseList.add(course);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readStudents(String args[], ArrayList<Student> studentList){
        String studentIdFilePath = args[1];
        try {
            Files.lines(Path.of(studentIdFilePath), StandardCharsets.UTF_8).forEach(studentIdLine -> {
                Student student = new Student(studentIdLine);
                studentList.add(student);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readTokens(String args[], ArrayList<ArrayList<Integer>> tokenList){
        String tokenFilePath = args[2];
        try
        {
            Files.lines(Path.of(tokenFilePath), StandardCharsets.UTF_8).forEach(tokenLine -> {
                String[] tokenStringArray = tokenLine.split(",");
                ArrayList<String> tokenStringList = new ArrayList<>(Arrays.asList(tokenStringArray));
                ArrayList<Integer> tokenIntegerList = new ArrayList<>();
                tokenStringList.stream().forEach(list -> {
                    tokenIntegerList.add(Integer.parseInt(list.replaceAll(" ", "")));
                });
                tokenList.add(tokenIntegerList);
            });
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void createPreferenceList(ArrayList<Student> studentList, ArrayList<ArrayList<Integer>> tokenList){
        int studentSize = studentList.toArray().length;

        IntStream.range(0, studentSize).forEach(index -> {
            studentList.get(index).preferenceList = tokenList.get(index);
            studentList.get(index).numOfCoursesWanted = tokenList.get(index).stream().filter(givenToken -> givenToken != 0).collect(Collectors.toList()).size();
        });
    }

    public static boolean checkExceedingToken(final boolean[] error, ArrayList<Student> studentList){
        studentList.stream().forEach(student -> {
            int totalTokensGiven = student.preferenceList.stream().reduce(0, (subtotal, givenToken) -> subtotal + givenToken);
            if (totalTokensGiven != 100) error[0] = true;
        });

        if (error[0] == true){
            System.out.println("Not all students gave their tokens correctly. Exiting...");
            return true;
        }
        else{
            return false;
        }
    }

    public static void assignStudentsRandomly(ArrayList<Student> studentList, ArrayList<Course> courseList){
        studentList.stream().forEach(student -> {
            IntStream.range(0, student.numOfCoursesWanted).forEach(enrollCount -> {
                Integer courseIndex = (new Random()).nextInt(0, courseList.size());
                student.enrollToCourse_RAND(courseList, courseList.get(courseIndex));
            });
        });
    }

    public static void createRandomUnhappinessFile(ArrayList<Student> studentList){
        final Double[] totalUnhappiness = {0.};
        IntStream.range(0, studentList.size()).forEach(studentIndex -> {
            totalUnhappiness[0] += studentList.get(studentIndex).unhappiness;
        });

        final Double[] avgUnhappiness = {0.};
        avgUnhappiness[0] = totalUnhappiness[0] / studentList.size();

        final String[] OUT_RAND_UNHAPPY_TEXT = {valueOf(avgUnhappiness[0])}; // TOPLAM UNHAPPINESS YAZ
        studentList.stream().forEach(student -> {
            OUT_RAND_UNHAPPY_TEXT[0] = OUT_RAND_UNHAPPY_TEXT[0].concat("\n" + student.unhappiness);
        });

        try {
            FileWriter myWriter = new FileWriter(OUT_RAND_UNHAPPY);
            myWriter.write(OUT_RAND_UNHAPPY_TEXT[0]);
            myWriter.close();
        }
        catch (IOException e){
            throw new RuntimeException();
        }
    }

    public static void createRandomAdmissionFile(ArrayList<Course> courseList){
        final String[] OUT_RAND_ADMISSION_TEXT = {""};
        courseList.stream().forEach(course -> {
            OUT_RAND_ADMISSION_TEXT[0] = OUT_RAND_ADMISSION_TEXT[0].concat(course.getCourseNameAndEnrolledStudents()+ "\n");
        });

        try {
            FileWriter myWriter = new FileWriter(OUT_RAND_ADMISSION);
            myWriter.write(OUT_RAND_ADMISSION_TEXT[0]);
            myWriter.close();
        }
        catch (IOException e){
            throw new RuntimeException();
        }
    }

    public static void clearEnrollments(ArrayList<Student> studentList, ArrayList<Course> courseList){
        studentList.stream().forEach(student -> {
            student.enrolledCourses = new ArrayList<>();
        });

        courseList.stream().forEach(course -> {
            course.enrolledStudents = new ArrayList<>();
            course.numOfStudents = 0;
        });
    }

    public static Student getStudentById(ArrayList<Student> studentList, String studentId){
        Student[] student = {null};
        studentList.stream().forEach(currentStudent -> {
            if(currentStudent.studentId == studentId)
                student[0] = currentStudent;
        });
        return student[0];
    }

    public static void assignStudents(ArrayList<Student> studentList, ArrayList<Course> courseList){
        // FOR EVERY COURSE
        IntStream.range(0, courseList.size()).forEach(courseIndex -> {
            Course course = courseList.get(courseIndex);

            // FIND WHO GAVE HOW MANY TOKENS TO COURSE
            ArrayList<Pair<String, Integer>> studentTokenList = new ArrayList<>();
            studentList.stream().forEach(student -> {
                studentTokenList.add(new Pair<>(student.studentId, student.preferenceList.get(courseIndex)));
            });

            // SORT TOKENS
            Stream<Pair<String, Integer>> sortedPairStream = studentTokenList.stream().sorted((Pair<String, Integer> p1, Pair<String, Integer> p2) -> p2.getR().compareTo(p1.getR()));
            ArrayList<Pair<String, Integer> > sortedStudentTokenList = new ArrayList<Pair<String, Integer>>(sortedPairStream.collect(Collectors.toList()));

            // IF MULTIPLE STUDENTS GAVE THE MINIMUM TOKEN FOR COURSE I.E THE LAST ENROLLED STUDENT'S TOKEN NUM, INCREASE CAPACITY
            final Integer[] lastStudentIndex = {0};
            if(course.totalCapacity > studentList.size()){
                lastStudentIndex[0] = studentList.size() - 1;
            }
            else{
                lastStudentIndex[0] = course.totalCapacity - 1;
            }
            Integer minTokenRequired = sortedStudentTokenList.get(lastStudentIndex[0]).getR();

            IntStream.range(lastStudentIndex[0] + 1, studentList.size()).forEach(studentIndex -> {
                String studentId = sortedStudentTokenList.get(studentIndex).getL();
                Student student = getStudentById(studentList, studentId);

                if(student.getGivenTokenToCourse(courseList, course) == minTokenRequired){
                    lastStudentIndex[0] = studentIndex;
                    course.totalCapacity += 1;
                }
            });

            // ENROLL
            IntStream.range(0, lastStudentIndex[0] + 1).forEach(studentIndex -> {
                String studentId = sortedStudentTokenList.get(studentIndex).getL();
                Student student = getStudentById(studentList, studentId);
                student.enrollToCourse_TATEC(courseList, course);
            });
        });
    }

    public static void createTatecUnhappynessFile(ArrayList<Student> studentList){
        final Double[] totalUnhappinessTATEC = {0.};
        IntStream.range(0, studentList.size()).forEach(studentIndex -> {
            totalUnhappinessTATEC[0] += studentList.get(studentIndex).unhappiness;
        });

        final Double[] avgUnhappiness = {0.};
        avgUnhappiness[0] = totalUnhappinessTATEC[0] / studentList.size();

        final String[] OUT_TATEC_UNHAPPY_TEXT = {valueOf(avgUnhappiness[0])}; // TOPLAM UNHAPPINESS YAZ
        studentList.stream().forEach(student -> {
            OUT_TATEC_UNHAPPY_TEXT[0] = OUT_TATEC_UNHAPPY_TEXT[0].concat("\n" + student.unhappiness);
        });

        try {
            FileWriter myWriter = new FileWriter(OUT_TATEC_UNHAPPY);
            myWriter.write(OUT_TATEC_UNHAPPY_TEXT[0]);
            myWriter.close();
        }
        catch (IOException e){
            throw new RuntimeException();
        }
    }

    public static void createTATECAdmissionFile(ArrayList<Course> courseList){
        final String[] OUT_TATEC_ADMISSION_TEXT = {""};
        courseList.stream().forEach(course -> {
            OUT_TATEC_ADMISSION_TEXT[0] = OUT_TATEC_ADMISSION_TEXT[0].concat(course.getCourseNameAndEnrolledStudents()+ "\n");
        });

        try {
            FileWriter myWriter = new FileWriter(OUT_TATEC_ADMISSION);
            myWriter.write(OUT_TATEC_ADMISSION_TEXT[0]);
            myWriter.close();
        }
        catch (IOException e){
            throw new RuntimeException();
        }
    }
}



