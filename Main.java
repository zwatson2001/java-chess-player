import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // "RNBKQBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbkqbnr"
        Board board = new Board("RNBKQBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbkqbnr");
        Game game = new Game(board, true);
        Scanner scanner = new Scanner(System.in);
        boolean keepPlaying = true;
        Agent agent = new Agent(board, false, 3);
        Random r = new Random();
        int numOfErrors = 0;

        String[] snarkyComments = {
            "\nTry again. Maybe put some effort in this time.\n", 
            "\nNice try buster.\n",
            "\nNo bueno.\n"
        };

        System.out.println("\n\n\n");
        System.out.println("Alrighty let's get started with some chess. Type 'quit' if you get too scared >:)");
        System.out.println("Example move: 'p e2 e4'");
        System.out.println(board);

        while (keepPlaying) {
            System.out.println("Input move:");

            if (board.inCheckmate(true)) {
                System.out.println("I almost feel bad. Not really though.");
                System.out.println("* * * * * * *\n* YOU LOSE! *\n* * * * * * *");
            }

            String input = scanner.nextLine();
            if (input.toLowerCase().contains("quit")) {
                keepPlaying = false; 
                scanner.close();
                break;
            }

            if (!game.makePlayerMove(input, scanner)) {
                if (numOfErrors == 0) {
                    int randomNumber = r.nextInt((snarkyComments.length));
                    System.out.println(snarkyComments[randomNumber]);
                } else {
                    System.out.println("\nDude. Focus.\n");
                }

                numOfErrors ++;
            } else {
                System.out.println(board + "\n");

                if (board.getMoveCountDraw() >= 100) {
                    System.out.println("We appear to be at an impasse. It's probably your fault.");
                    System.out.println("* * * * * * * * * * * * *\n* DRAW BY 50 MOVE RULE! *\n* * * * * * * * * * * * *");
                    break;
                }

                if (board.checkThreefoldRepetition()) {
                    System.out.println("We appear to be at an impasse. It's probably your fault.");
                    System.out.println("* * * * * * * * * * * * * * * * *\n* DRAW BY THREEFOLD REPETITION! *\n* * * * * * * * * * * * * * * * *");
                    break;
                }

                System.out.println("Thinking...");

                if (!agent.makeBestMove()) {
                    if (board.inCheck(false)) {
                        System.out.println("Shit.\n");
                        System.out.println("* * * * * * *\n*  YOU WIN! *\n* * * * * * *");
                        break;
                    } else {
                        System.out.println("We appear to be at an impasse. It's probably your fault.");
                        System.out.println("* * * * * * *\n* STALEMATE! *\n* * * * * * *");
                        break;
                    }
                }
    
                System.out.println(board + "\n");

                numOfErrors = 0;
            }

            if (board.getMoveCountDraw() >= 100) {
                System.out.println("We appear to be at an impasse. It's probably your fault.");
                System.out.println("* * * * * * * * * * * * *\n* DRAW BY 50 MOVE RULE! *\n* * * * * * * * * * * * *");
                break;
            }
        }
    }
}
