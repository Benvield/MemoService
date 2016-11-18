import java.sql.*;
import java.util.List;
import java.util.Vector;

public class GameRepository {
    Connection con;

    public NewGameResult GetNewGame(int userId) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/memorydb", "SA", "");
            ResultSet result = con.createStatement().executeQuery("select * from games where player2Id is NULL");

            if (!result.isBeforeFirst()) {
                //no waiting game
                con.setAutoCommit(false);
                PreparedStatement pstGames = con.prepareStatement("insert into games (Player1Id,Player2Id,ActivePlayer,Player1Score,Player2Score) values(?,NULL,1,0,0)", Statement.RETURN_GENERATED_KEYS);
                pstGames.setInt(1, userId);
                pstGames.executeUpdate();
                ResultSet generatedKeys = pstGames.getGeneratedKeys();
                generatedKeys.next();
                int gameId = generatedKeys.getInt(1);

                PreparedStatement pstMap = con.prepareStatement("insert into gameMap (GameId) values(?)", Statement.RETURN_GENERATED_KEYS);
                pstMap.setInt(1, gameId);
                pstMap.executeUpdate();
                generatedKeys = pstMap.getGeneratedKeys();
                generatedKeys.next();
                int mapId = generatedKeys.getInt(1);

                GameState gameState = new GameState();
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 6; j++) {
                        PreparedStatement pstMapTile = con.prepareStatement("insert into mapTiles (MapId,x,y,value) values(?,?,?,?)");
                        pstMapTile.setInt(1, mapId);
                        pstMapTile.setInt(2, i);
                        pstMapTile.setInt(3, j);
                        pstMapTile.setInt(4, gameState.gameBoard[i][j]);
                        pstMapTile.executeUpdate();
                    }
                }
                con.commit();
                NewGameResult res = new NewGameResult();
                res.GameId = gameId;
                res.PlayerNo = 1;
                return res;
            } else {
                result.next();
                int gameId = Integer.parseInt(result.getString(1));
                PreparedStatement pst = con.prepareStatement("update games set Player2Id=? where GameId=?");
                pst.setInt(1, userId);
                pst.setInt(2, gameId);
                pst.executeUpdate();
                NewGameResult res = new NewGameResult();
                res.GameId = gameId;
                res.PlayerNo = 2;
                return res;
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace(System.err);
            }
        }
        NewGameResult res = new NewGameResult();
        res.GameId = -1;
        res.PlayerNo = -1;
        return res;
    }

    public int GetActivePlayer(int gameId) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/memorydb", "SA", "");
            PreparedStatement pst = con.prepareStatement("select ActivePlayer from games where GameId=?");
            pst.setInt(1, gameId);
            ResultSet result = pst.executeQuery();
            if (!result.isBeforeFirst()) {
                return -1;
            }
            result.next();
            return result.getInt(1);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public Move MakeMove(int userId, int gameId, int x1, int y1,int x2, int y2){
        Move move = new Move();
        move.x1 = x1;
        move.y1 = y1;
        move.x2 = x2;
        move.y2 = y2;
        move.value1 = -1;
        move.value2 = -1;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/memorydb", "SA", "");
            PreparedStatement pst = con.prepareStatement("select value from maptiles m " +
                    "join gamemap gm on m.mapid=gm.mapid " +
                    "where gameid=? and x=? and y=?");
            pst.setInt(1, gameId);
            pst.setInt(2, x1);
            pst.setInt(3, y1);
            ResultSet result = pst.executeQuery();
            if (!result.isBeforeFirst()) {
                return move;
            }
            result.next();
            move.value1 = result.getInt(1);

            pst.clearParameters();
            pst.setInt(1, gameId);
            pst.setInt(2, x2);
            pst.setInt(3, y2);
            result = pst.executeQuery();
            if (!result.isBeforeFirst()) {
                return move;
            }
            result.next();
            move.value2 = result.getInt(1);

            con.setAutoCommit(false);

            pst = con.prepareStatement("select ActivePlayer from games where GameId=?");
            pst.setInt(1, gameId);
            result = pst.executeQuery();
            result.next();
            int currentPlayer = result.getInt(1);

            if(move.value1 != move.value2){
                pst = con.prepareStatement("update games set ActivePlayer=? where GameId=?");
                if(currentPlayer==1) {
                    pst.setInt(1, 2);
                } else {
                    pst.setInt(1, 1);
                }
                pst.setInt(2, gameId);
                pst.executeUpdate();
            } else {
                pst = con.prepareStatement("select Player1Score,Player2Score from games where GameId=?");
                pst.setInt(1, gameId);
                result = pst.executeQuery();
                result.next();
                int score1 = result.getInt(1);
                int score2 = result.getInt(1);

                if(currentPlayer==1) {
                    pst = con.prepareStatement("update games set Player1Score=? where GameId=?");
                    pst.setInt(1, score1+1);
                } else {
                    pst = con.prepareStatement("update games set Player2Score=? where GameId=?");
                    pst.setInt(1, score2+1);
                }
                pst.setInt(2, gameId);
                pst.executeUpdate();

                if(score1 + score2 + 1 == 18)
                {
                    pst = con.prepareStatement("update games set ActivePlayer=0 where GameId=?");
                    pst.setInt(1, gameId);
                    pst.executeUpdate();
                }
            }

            pst = con.prepareStatement("insert into moves (GameId,PlayerId,x1,y1,value1,x2,y2,value2,shown) values(?,?,?,?,?,?,?,?,?)");
            pst.setInt(1, gameId);
            pst.setInt(2, userId);
            pst.setInt(3,move.x1);
            pst.setInt(4,move.y1);
            pst.setInt(5,move.value1);
            pst.setInt(6,move.x2);
            pst.setInt(7,move.y2);
            pst.setInt(8,move.value2);
            pst.setBoolean(9,false);
            pst.executeUpdate();
            con.commit();

            return move;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return move;
    }

    public Vector<Move> GetNotShownMoves(int gameId)
    {
        Vector<Move> moveList = new Vector<Move>();

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/memorydb", "SA", "");
            PreparedStatement pst = con.prepareStatement("select x1,y1,value1,x2,y2,value2 from moves where gameid=? and shown=false");
            pst.setInt(1, gameId);
            ResultSet result = pst.executeQuery();

            while (result.next())
            {
                Move newMove = new Move();
                newMove.x1 = result.getInt(1);
                newMove.y1 = result.getInt(2);
                newMove.value1 = result.getInt(3);
                newMove.x2 = result.getInt(4);
                newMove.y2 = result.getInt(5);
                newMove.value2 = result.getInt(6);

                moveList.add(newMove);
            }

            pst = con.prepareStatement("update moves set shown=true where GameId=? and shown=false");
            pst.setInt(1, gameId);
            pst.executeUpdate();

            return moveList;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return moveList;
    }

    public Score GetGameScore(int gameId) {
        Score score = new Score();
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/memorydb", "SA", "");
            PreparedStatement pst = con.prepareStatement("select player1score,player2score from games where gameid=?");
            pst.setInt(1, gameId);
            ResultSet result = pst.executeQuery();
            if (!result.isBeforeFirst()) {
                return score;
            }
            result.next();
            score.Player1Score = result.getInt(1);
            score.Player2Score = result.getInt(2);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return score;
    }
}


