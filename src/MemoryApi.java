import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Vector;

// The Java class will be hosted at the URI path "/helloworld"

@Path("/MemoryService")
public class MemoryApi {

    @GET
    @Path("/GetGame/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetGame(@PathParam("userId") int userId) {
        GameRepository g = new GameRepository();
        NewGameResult ngr = g.GetNewGame(userId);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz")
                .setPrettyPrinting()
                .create();
        return Response.ok(gson.toJson(ngr)).build();
    }

    @GET
    @Path("/GetActivePlayer/{gameId}")
    @Produces(MediaType.TEXT_PLAIN)
    public int GetActivePlayer(@PathParam("gameId") int gameId) {
        GameRepository g = new GameRepository();
        return g.GetActivePlayer(gameId);
    }

    @GET
    @Path("/MakeMove/{userId}/{gameId}/{x1}/{y1}/{x2}/{y2}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response MakeMove(@PathParam("userId") int userId,
                        @PathParam("gameId") int gameId,
                        @PathParam("x1") int x1,
                        @PathParam("y1") int y1,
                        @PathParam("x2") int x2,
                        @PathParam("y2") int y2) {
        GameRepository g = new GameRepository();
        Move move = g.MakeMove(userId,gameId,x1,y1,x2,y2);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz")
                .setPrettyPrinting()
                .create();
        return Response.ok(gson.toJson(move)).build();
    }

    @GET
    @Path("/GetNotShownMoves/{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetNotShownMoves(@PathParam("gameId") int gameId) {
        GameRepository g = new GameRepository();
        Vector<Move> moveList = g.GetNotShownMoves(gameId);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz")
                .setPrettyPrinting()
                .create();
        return Response.ok(gson.toJson(moveList)).build();
    }

    @GET
    @Path("/GetGameScore/{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetGameScore(@PathParam("gameId") int gameId) {
        GameRepository g = new GameRepository();
        Score score = g.GetGameScore(gameId);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz")
                .setPrettyPrinting()
                .create();
        return Response.ok(gson.toJson(score)).build();
    }
}