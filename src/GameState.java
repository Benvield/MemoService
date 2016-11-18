import java.util.Random;

/**
 * Created by chudy on 09.11.2016.
 */
public class GameState {
    public int[][] gameBoard = new int[6][6];
    public GameState(){
        int x = 1;
        for(int i = 0; i < 6; i++){
            for (int j = 0; j < 6; j++){
                gameBoard[i][j] = x;
                if(j%2==1)
                {
                    x++;
                }
            }
        }

        Random rng = new Random();
        for(int i = 0; i < 100; i++)
        {
            int x1 = rng.nextInt(6);
            int y1 = rng.nextInt(6);
            int x2,y2;
            do {
                x2 = rng.nextInt(6);
                y2 = rng.nextInt(6);
            } while(x1 == x2 && y1 == y2);

            int temp = gameBoard[x1][y1];
            gameBoard[x1][y1] = gameBoard[x2][y2];
            gameBoard[x2][y2] = temp;
        }
    }
}
