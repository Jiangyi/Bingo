/*
* Copyright (C) 2013 Andrew Jiang && Samuel Zhou
*/
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Bingo {

    public static void main(String[] args) {
        // Declare board arrays and game variables
        int[][][] playerBoard = new int[2][5][5];
        int[][][] cpuBoard = new int[2][5][5];
        boolean[] callBoard = new boolean[75 + 1];
        int points = 0;
        int choice = 0;
        boolean victory = false;
        Scanner kb = new Scanner(System.in);

        // Initialize arrays - Read board files
        try {
            playerBoard = readBoard("PLAYER.txt");
            cpuBoard = readBoard("CPU.txt");
        } catch (IOException e) {
            System.out.println("Error closing file.");
            System.exit(0);
        }

        callBoard[0] = true; // Bonus space is 0, and is automatically called

        // Main loop
        do {
            // Make a valid Bingo call and construct a string in the form X#
            int call = makeValidCall(callBoard);
            String callout = "";
            if (call <= 15) {
                callout = "B" + call;
            } else if (call <= 30) {
                callout = "I" + call;
            } else if (call <= 45) {
                callout = "N" + call;
            } else if (call <= 60) {
                callout = "G" + call;
            } else {
                callout = "O" + call;
            }
            
            // Output board information once per turn
            System.out.println("Player Board:");
            printBoard(playerBoard);
            System.out.println("\nCPU Board:");
            printBoard(cpuBoard);
            System.out.println("\nCall Board:");
            printCallBoard(callBoard);
            System.out.println("\nBINGO CALL IS: " + callout);
            System.out.println("\nCurrent Points Available: " + points);
            System.out.println("1. Daub Board \n2. Call Bingo! \n3. Use Bonus (1000 points) \n4. Next Call\n");
            
            // Get current time for use with measuring reaction time
            long startTime = System.currentTimeMillis();
            
            do {
                // Get input from user and make sure it's a valid int
                if (kb.hasNextInt()) {
                    choice = kb.nextInt();
                }
                
                // Choice 1 : Daub Board
                if (choice == 1) {
                    if (daubBoard(playerBoard, call, false)) {
                        // Determine points given by reaction time, if the daub is successful
                        long endTime = System.currentTimeMillis();
                        long reactionTime = endTime - startTime;
                        int pointsGained = reactionTime <= 3000 ? 400 : reactionTime <= 4000 ? 200 : 0;
                        points += pointsGained;
                        System.out.println("You daubed " + callout + " on your board(s), " + pointsGained + " points earned.");
                    } else {
                        // reduce points by 500 on an invalid daub
                        points -= 500;
                        System.out.println("You do not have " + callout + " on your board(s), 500 point penalty.");
                    }
                // Choice 2 : Call Bingo
                } else if (choice == 2) {
                    // Checks if the player has BINGO, outputs appropriate result
                    if (bingoChecker(playerBoard)) {
                        System.out.println("BINGO! Congratulations, you have won!");
                        System.out.println("\nPlayer Board:");
                        printBoard(playerBoard);
                    } else {
                        System.out.println("Bad Bingo call, you have lost! (You might be an idiot)");
                    }
                    // Game ends after BINGO is called, regardless of validity
                    victory = true;
                    break;
                // Choice 3 : Use Bonus
                } else if (choice == 3) {
                    // Ensures the user has points to spend for a bonus daub
                    if (points >= 1000) {
                        useBonus(playerBoard);
                        points -= 1000;
                    } else {
                        System.out.println("You do not have enough points for a bonus daub.");
                    }
                // Invalid choice
                } else if (choice != 4) {
                    System.out.println("Invalid entry!");
                }
            } while (choice != 4); // When choice 4 is called (next call), exit the loop

            // CPU turn: Daub and output
            if (daubBoard(cpuBoard, call, false)) {
                System.out.println("The computer has daubed " + callout + ".\n");
            } else {
                System.out.println("The computer did not have " + callout + ".\n");
            }

            // Check if CPU has BINGO
            if (bingoChecker(cpuBoard)) {
                System.out.println("\nCPU Board:");
                printBoard(cpuBoard);
                System.out.println("BINGO! The computer has won!");
                victory = true;
            }
        } while (!victory); // End the game when a victor is found
    }

    public static boolean bingoChecker(int[][][] board) {
        // Local Variable Declarations
        int horizontalCounter = 0;
        int verticalCounter = 0;
        int leftDiagonalCounter = 0;
        int rightDiagonalCounter = 0;
        
        // For loop: checks rows, columns, and diagonals for 5 valid daubs
        for (int depth = 0; depth < board.length; depth++) {
            for (int col = 0; col < board[0].length; col++) {
                for (int row = 0; row < board[0][0].length; row++) {
                    // Valid daubs are assigned a value of 0 or -1
                    // If a space is valid, it is added to the victory counter
                    if (board[depth][col][row] <= 0) {
                        horizontalCounter += 1;
                    } else if (board[depth][row][col] <= 0) {
                        verticalCounter += 1;
                    } else if (board[depth][row][row] <= 0) {
                        leftDiagonalCounter += 1;
                    } else if (board[depth][row][board[0][0].length - (row + 1)] <= 0) {
                        rightDiagonalCounter += 1;
                    }
                }
                // If any row / column / diagonal has 5 valid daubs, victory is achieved
                if (horizontalCounter == 5 || verticalCounter == 5 || leftDiagonalCounter == 5 || rightDiagonalCounter == 5) {
                    return true;
                } else {
                    // If not, reset the counters
                    horizontalCounter = verticalCounter = leftDiagonalCounter = rightDiagonalCounter = 0;
                }
            } 
        }
        // If no bingos are found, return false
        return false;
    }

    public static void printBoard(int[][][] board) {
        // Initialization of local string array
        String[][] value = new String[][] {
            {"B","I","N","G","O"},
            {"B","I","N","G","O"}
        };

        // Print out the header values: B I N G O
        for (int i = 0; i < 2; i++) {
            System.out.format("%1$3s %2$3s %3$3s %4$3s %5$3s     ", value[i][0], value[i][1], value[i][2], value[i][3], value[i][4]);
        }

        // Start a new line for the values
        System.out.print("\n");

        // Reads each row of board 1 and 2, and prints it out
        for (int row = 0; row < 5; row++) {
            for (int i = 0; i < 2; i++) {
                for (int col = 0; col < 5; col++) {
                    // If the value is greater than 0, it is undaubed, and convert into a string
                    if (board[i][row][col] > 0) {
                        value[i][col] = String.valueOf(board[i][row][col]);
                    } else if (board[i][row][col] == 0) {
                        // Value of 0 is assigned to bonus / free spaces, represented by F
                        value[i][col] = "F";
                    } else {
                        // Value of -1 is assigned to daubed spaces, represented by *
                        value[i][col] = "*";
                    }
                }
                // Print out the formatted row
                System.out.format("%1$3s %2$3s %3$3s %4$3s %5$3s     ", value[i][0], value[i][1], value[i][2], value[i][3], value[i][4]);
            }
            // New line
            System.out.print("\n");
        }
    }

    public static void printCallBoard(boolean[] callBoard) {
        // Initialize local variables for B I N G O header
        String[] header = {" B ", " I ", " N ", " G ", " O "};
        int headerIndex = 0;
        
        // Print out 'B' to start
        System.out.print(header[headerIndex]);

        // Cycle through the boolean array of calls
        for (int i = 1; i < 76; i++) {
            // If a value has not been called, print the value; otherwise print *
            if (!callBoard[i]) {
                System.out.format("%1$3s ", i);
            } else {
                System.out.format("%1$3s ", "*");
            }

            // Skip to a new row after every 15 values
            if (i % 15 == 0) {
                System.out.print("\n");
                // print the next row name (I, N, G, O)
                if (headerIndex < 4) {
                    headerIndex++;
                    System.out.print(header[headerIndex]);
                }
            }
        }
    }

    public static int[][][] readBoard(String filename) throws IOException {
        // Declare local variables
        int[][][] board = new int[2][5][5];
        BufferedReader inputStream = null;
        String line = null;
        boolean exceptionThrown = false;

        try {
            // Create input stream
            inputStream = new BufferedReader(new FileReader(filename));

            // For loop to read through both boards
            for (int i = 0; i < 2; i++) {
                // Read through the 5 rows of each board
                for (int j = 0; j < 5; j++) {
                    // Read the row, and prepare for token separation
                    line = inputStream.readLine();
                    StringTokenizer token = new StringTokenizer(line);
                    for (int k = 0; k < 5; k++) {
                        // Read through tokens, converting all values to integers for use in the array
                        String nextToken = token.nextToken();
                        if (nextToken.equalsIgnoreCase("F")) {
                            // Free spaces (F) are assigned a value of 0
                            board[i][j][k] = 0;
                        } else {
                            board[i][j][k] = Integer.parseInt(nextToken);
                        }
                    }
                }
            }

        } catch(FileNotFoundException e) {
            // CPU.txt or PLAYER.txt was not found
            System.out.println("File not found: " + e.getMessage() + "\nEnsure that it is in the same directory.");
            exceptionThrown = true;
        } catch (IOException e) {
            // Error accessing contents of the file
            System.out.println("File Access Error: " + e.getMessage());
            exceptionThrown = true;
        } catch (NoSuchElementException e) {
            // Thrown when tokenizer does not have a valid nextToken (occurs when file is formatted incorrectly)
            System.out.println("Invalid file format. \nEnsure the file has 10 rows of 5 digits.");
            exceptionThrown = true;
        } catch (NumberFormatException e) {
            // Thrown when a non-numerical character that is not 'F' is in the file
            System.out.println("Invalid character in file. \nThe only non-numerical value should be \'F\'.");
            exceptionThrown = true;
        } finally {
            // Close input stream if it exists
            if (inputStream != null) {
                inputStream.close();
            }
            // If an exception was thrown, exit the program
            if (exceptionThrown) {
                System.exit(0);
            }
        }
        return board;
    }

    public static int makeValidCall(boolean[] callBoard) {
        // Declare local variables
        Random rdm = new Random();
        int value;

        // Generate a random value until one that has not been called is created
        do {
            value = rdm.nextInt(74) + 1;
        } while (callBoard[value] == true);

        // Flag the value as called, and return it
        callBoard[value] = true;
        return value;
    }

    public static boolean daubBoard(int[][][] board, int call, boolean isBonus) {
        // Local column variable to minimize searching
        int col;
        // Find the column the number resides in, eliminating unnecessary searches
        if (call <= 15) {
            col = 0;
        } else if (call <= 30) {
            col = 1;
        } else if (call <= 45) {
            col = 2;
        } else if (call <= 60) {
            col = 3;
        } else {
            col = 4;
        }

        // For-loop to search through the column specified
        for (int depth = 0; depth < board.length; depth++) {
            for (int row = 0; row < board[0].length; row++) {
                if (board[depth][row][col] == call) {
                    // If a match is found, assign it 0 or -1 based on whether or not it was a bonus daub
                    board[depth][row][col] = isBonus ? 0 : -1;
                    return true;
                }
            }
        }
        // If no matches are found, the daub is unsuccessful
        return false;
    }

    public static void useBonus(int[][][] board) {
        // Declare local variables
        Random rdm = new Random();
        int row, col, depth, call;

        // Loop to generate a space on one of the two boards that is not daubed (positive, non-zero value)
        do {
            row = rdm.nextInt(5);
            col = rdm.nextInt(5);
            depth = rdm.nextInt(2);
        } while (board[depth][row][col] <= 0);

        // Get the value of the undaubed space, and pass it into daubBoard, with the bonus flag set as true
        call = board[depth][row][col];
        daubBoard(board, call, true);
    }
}