/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package rules;

/**
 * RulesOf6005 represents the collaboration policy of 6.005 as described by the
 * general information on Stellar.
 */
public class RulesOf6005 {
     
    /**
     * Judge whether a given piece of code may be used in an assignment (problem
     * set or team project) or not, according to the 6.005 collaboration policy.
     * 
     * @param writtenByYourself true if the code in question was written by
     *        yourself or, in the case of a team project, your teammates,
     *        otherwise false.
     * @param availableToOthers if not writtenByYourself, whether the
     *        code in question is available to all other students in the class.
     *        Otherwise, ignored.
     * @param writtenAsCourseWork if not writtenByYourself, whether the
     *        code in question was written specifically as part of a solution to
     *        a 6.005 assignment, in the current or past semesters. Otherwise,
     *        ignored.
     * @param citingYourSource if not writtenByYourself, whether you
     *        properly cite your source. Otherwise, ignored.
     * @param implementationRequired whether the assignment specifically asks
     *        you to implement the feature in question.
     * @return Whether or not, based on the information provided in the
     *         arguments, you are likely to be allowed to use the code in
     *         question in your assignment, according to the 6.005 collaboration
     *         policy for the current semester.
     */
    public static boolean mayUseCodeInAssignment(boolean writtenByYourself,
            boolean availableToOthers, boolean writtenAsCourseWork,
            boolean citingYourSource, boolean implementationRequired) {
        
        // If you wrote the code yourself (or your team wrote it), you can always use it
        if (writtenByYourself) {
            return true;
        }
        
        // If the assignment requires you to implement it yourself, you cannot use external code
        if (implementationRequired) {
            return false;
        }
        
        // If the code was written as course work (homework solutions), you cannot use it
        // This prevents using other students' assignment solutions
        if (writtenAsCourseWork) {
            return false;
        }
        
        // For external code (libraries, online resources, etc.):
        // - It must be available to all students (not giving you unfair advantage)
        // - You must cite your source properly
        return availableToOthers && citingYourSource;
    }
    
    /**
     * Main method of the class.
     * 
     * Runs the mayUseCodeInAssignment method.
     * 
     * @param args unused
     */
    public static void main(String[] args) {
        System.out.println("You may certainly use code you wrote yourself: " +
            RulesOf6005.mayUseCodeInAssignment(true, false, true, true, true));
    }
}
