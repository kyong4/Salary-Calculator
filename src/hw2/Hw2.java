package hw2;

import java.util.Scanner;
import java.io.File;

/**
 * @author Kevin Yong
 * @since 6/2/2020
 * @description hw2
 */
public class Hw2 {

    public static void main(String[] args) throws Exception {

        String[] empNo = new String[100];
        String[] first = new String[100];//first name
        String[] last = new String[100];//last name
        String[] address = new String[100];
        String[] cityEtc = new String[100]; //city, state, zipcode

        double[] rateBefore9 = new double[100];// hourly rate before 9 PM
        double[] rate9ToMid = new double[100];// hourly rate from 9 PM to midnight
        double[] rateAfterMid = new double[100];// hourly rate after midnight

        int[] days = new int[100];
        int[] startTime = new int[100];
        int[] endTime = new int[100];

        double pay[] = new double[100]; //amount to pay the babysitter

        int count = fillArray(empNo, first, last, address, cityEtc, rateBefore9, rate9ToMid, rateAfterMid, days, pay);

        readStartEnd(rateBefore9, rate9ToMid, rateAfterMid, days, pay);
//passing rates to readStartEnd so that it can be passed to computeFee method within readStartEnd

        sortAlph(last, pay, count);
        print(last, pay, count);
    }

    //fills arrays with input files
    public static int fillArray(String[] empNo, String[] first, String[] last, String[] address, String[] cityEtc,
            double[] rateBefore9, double[] rate9ToMid, double[] rateAfterMid, int[] days, double pay[]) throws Exception {

        int count = 0; //counts amount of items in input

        Scanner sc = new Scanner(new File("personnel.txt"));
        Scanner sc2 = new Scanner(new File("payroll.txt"));

        for (int i = 0; sc.hasNext(); i++) {
            count++;
            empNo[i] = sc.nextLine();

            String full = sc.nextLine(); // full name, will be split to first and last name
            String[] split = full.split(",");

            last[i] = split[0];
            first[i] = split[1];

            address[i] = sc.nextLine();
            cityEtc[i] = sc.nextLine();

            rateBefore9[i] = sc.nextDouble();
            rate9ToMid[i] = sc.nextDouble();
            rateAfterMid[i] = sc.nextDouble();

            String holdLine = sc.nextLine();// after double, need to get rid of trailing new line
            String holdLine2 = sc.nextLine();// there is a blank line inbetween each section of personnel info.
            pay[i] = 0; //set all initial pay value to 0

        } // end first for loop

        int index = 0; // For counting correct index for day[index]. Cant use
        //the "i" from the for loop since i increases even if it was empty or ":" was found
        for (int i = 0; sc2.hasNext(); i++) { //just need to fill days[]

            String holdEmpNo = sc2.nextLine(); //sole purpose is to hold the line containing empid id

            if (holdEmpNo.isEmpty() && sc2.hasNext()) {
                holdEmpNo = sc2.nextLine();

            }

            if (holdEmpNo.contains(":")) { //if it contains ":", then it is a timeshift, not empNo
                if (sc2.hasNext()) { //without this code, after reading the last shift, it will give no line found error
                    holdEmpNo = sc2.nextLine();
                }
            } else {

                days[index] = sc2.nextInt();
                //System.out.println(index + "DAYS:!!" + days[index]); used for debugging
                sc2.nextLine(); //get rid of newline
                String holdLine = sc2.nextLine();//this line should contain the first time shift
                index++;

            }
        }//end second for loop 

        return count;
    }

    //reads payroll, checks the start and end time, then sends to computeFee
    public static void readStartEnd(double[] rateBefore9,
            double[] rate9ToMid, double[] rateAfterMid, int[] days, double[] pay) throws Exception {

        Scanner sc = new Scanner(new File("payroll.txt"));
        int index = 0;
        for (int i = 0; sc.hasNext(); i++) {

            String check = sc.nextLine();//check if variable "check" is a time or a empNo
            if (check.isEmpty()) {
                check = sc.nextLine();
                index++;
                // System.out.println("was empty" + check);
            }

            if (check.contains(":")) {
                String[] startAndEnd = check.split("\\s+"); //check contains line 9:00 and 11:30. Splitting it.
                //Inconsistent white spaces in input. \\s+ matches one or more white spaces
                String start = startAndEnd[0];
                String end = startAndEnd[1];
                computeFee(start, end, rateBefore9, rate9ToMid, rateAfterMid, days, pay, index);

            } else {
                sc.nextLine(); //consume newline

                String start = sc.next();
                String end = sc.next();
                computeFee(start, end, rateBefore9, rate9ToMid, rateAfterMid, days, pay, index);
                //System.out.println("computed" + start + "and" + end + "\n"); used for debugging
                sc.nextLine(); //consume newline

            }

        }//end for loop

    }

    //computes fees given start and end time and rates, i is index 
    public static void computeFee(String start, String end, double[] rateBefore9,
            double[] rate9ToMid, double[] rateAfterMid, int[] days, double[] pay, int index) {

        String[] split = start.split(":"); // given time, for ex, 8:00, split to 8 and 00

        double startHr = Double.parseDouble(split[0]); //convert string to double
        double startMin = Double.parseDouble(split[1]);

        String[] split2 = end.split(":");
        double endHr = Double.parseDouble(split2[0]);
        double endMin = Double.parseDouble(split2[1]);

        startHr += startMin / 60;// converted to hours, example 1:30 is 1.5
        endHr += endMin / 60;

        //first case
        if (startHr >= 6 && startHr <= 9) { //rate from 6 PM to 9:00 PM.

            if (endHr >= 6 && endHr <= 9) {

                pay[index] += (endHr - startHr) * rateBefore9[index];
            }

            if (endHr > 9) {

                double timeWorkedBefore9 = 9 - startHr;
                double timeWorked9ToEndHr = endHr - 9;

                pay[index] += timeWorkedBefore9 * rateBefore9[index]
                        + timeWorked9ToEndHr * rate9ToMid[index];

            }

            if (endHr < 6) {

                double timeWorkedBefore9 = 9 - startHr;
                double timeWorked9ToMid = 12 - 9; //3 hours

                double timeWorkedAfterMid = endHr;

                pay[index] += (timeWorkedBefore9 * rateBefore9[index]
                        + timeWorked9ToMid * rate9ToMid[index]
                        + timeWorkedAfterMid * rateAfterMid[index]);

            }
            // System.out.println("Index" + index); used for debugging
            //System.out.println("PAY IS " + pay[index]);

        }//end first case

        //second case
        if (startHr > 9) { // 9 PM to midnight

            if (endHr > 9) {

                double timeWorkedBefore9 = 9 - startHr;
                double timeWorked9ToEndHr = endHr - 9;

                pay[index] += timeWorkedBefore9 * rateBefore9[index]
                        + timeWorked9ToEndHr * rate9ToMid[index];

            }

            if (endHr < 6) {

                double timeWorkedBefore9 = 9 - startHr;
                double timeWorked9ToMid = 12 - 9; //3 hours

                double timeWorkedAfterMid = endHr;

                pay[index] += (timeWorkedBefore9 * rateBefore9[index]
                        + timeWorked9ToMid * rate9ToMid[index]
                        + timeWorkedAfterMid * rateAfterMid[index]);
            }
        }//end second case

        //third case
        if (startHr < 6) { //midnight to 6 AM

            if (endHr < 6) {

                double timeWorkedAfterMid = endHr - startHr;

                pay[index] += timeWorkedAfterMid * rateAfterMid[index];
            }
        }// end case 3

    }//end computeFee

    //sorts alphabetically
    public static void sortAlph(String[] last, double[] pay, int count) {

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                if (last[i].compareTo(last[j]) < 0) { // if positive then already in alp. order
                    String temp;
                    temp = last[i];
                    last[i] = last[j];
                    last[j] = temp;

                    double temp2;
                    temp2 = pay[i];
                    pay[i] = pay[j];
                    pay[j] = temp2;
                }

            }// end for j
        }//end for i
    }

//given last name and pay array, print them
    public static void print(String[] last, double[] pay, int count) {

        for (int i = 0; i < count; i++) {
            System.out.printf("Last Name:%6s \tPaid: $%.2f\n", last[i], pay[i]);

        }
    }
}
