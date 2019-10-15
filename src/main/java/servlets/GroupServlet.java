package servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.bson.Document;
import org.bson.types.ObjectId;
import com.google.gson.Gson;

@WebServlet("/api/*")
public class GroupServlet extends HttpServlet {

    private MongoClient mongoClient;
    private MongoCollection<Document> groups;
    private MongoCollection<Document> users;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        fixHeaders(response);

        Group group = new Gson().fromJson(request.getReader(), Group.class);

        Document newGroup = new Document("name", group.name);
        groups.insertOne(newGroup);
        ObjectId group_id = (ObjectId)newGroup.get("_id");
        group.id = group_id.toString();

        Document newUser =
            new Document("name", group.user.name)
                 .append("email", group.user.email)
                 .append("group_id", group_id)
                 .append("admin", true);
        users.insertOne(newUser);


        PrintWriter out = response.getWriter();
        out.println(new Gson().toJson(group));

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        fixHeaders(response);

        PrintWriter out = response.getWriter();

        URI uri;
        try {
            uri = new URI(request.getRequestURI());
        } catch (URISyntaxException e) {
            return;
        }
        String[] segments = uri.getPath().replaceAll("^/|/$","").split("/");
        if (segments.length > 2 && "groups".equals(segments[1]) && !segments[2].isEmpty()) {

            // GET: /groups/:id

            String group_id = segments[2];
            Document query;
            try {
                query = new Document("_id", new ObjectId(group_id));
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }
            try (MongoCursor<Document> cursor = groups.find(query).iterator()) {
                if (!cursor.hasNext()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Group not found");
                    return;
                }
                out.println(cursor.next());
            }
        } else {
            try (MongoCursor<Document> cursor = groups.find().iterator()) {
                while (cursor.hasNext()) {
                    out.println(cursor.next());
                }
            }
        }
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        fixHeaders(response);
    }

    private void fixHeaders(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");

        response.setContentType("application/json;charset=UTF-8");
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String mongoConnectString = getServletContext().getInitParameter("mongodbConnectString");
        mongoClient = new MongoClient(new MongoClientURI(mongoConnectString));
        MongoDatabase mongoDatabase = mongoClient.getDatabase("adm");

        groups = mongoDatabase.getCollection("group");
        users = mongoDatabase.getCollection("user");
    }

    public void destroy() {
        mongoClient.close();
    }

    // Structures for Gson
    private class Group {
        String id;
        String name;
        User user;
    }

    private class User {
        String name;
        String email;
    }
}

