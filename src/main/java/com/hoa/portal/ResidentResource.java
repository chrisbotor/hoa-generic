package com.hoa.portal;

import jakarta.annotation.security.RolesAllowed; // Required for Security
import jakarta.ws.rs.POST;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/portal")
public class ResidentResource {

    @ConfigProperty(name = "HASURA_URL") 
    String hasuraUrl;

    @ConfigProperty(name = "HASURA_ADMIN_SECRET")
    String hasuraSecret;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getDashboard() {
        try {
            // Fetching from Hasura on .80
            String query = "{\"query\": \"query { residents(order_by: {house_number: asc}) { house_number street_name owner_name monthly_dues_status } }\"}";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hasuraUrl))
                    .header("Content-Type", "application/json")
                    .header("x-hasura-admin-secret", hasuraSecret)
                    .POST(HttpRequest.BodyPublishers.ofString(query))
                    .build();
            
            String jsonResponse = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            StringBuilder rowsHtml = new StringBuilder();
            Pattern p = Pattern.compile("\\{\"house_number\":\"(.*?)\",\"street_name\":\"(.*?)\",\"owner_name\":\"(.*?)\",\"monthly_dues_status\":\"(.*?)\"\\}");
            Matcher m = p.matcher(jsonResponse);
            
            while (m.find()) {
                String lot = m.group(1);
                String street = m.group(2);
                String name = m.group(3);
                String status = m.group(4);
                
                rowsHtml.append(String.format("""
                    <tr>
                        <td><strong>%s</strong></td>
                        <td>%s</td>
                        <td>%s</td>
                        <td><span class="badge %s">%s</span></td>
                        <td>
                            <form action="/portal/delete" method="POST" style="display:inline;">
                                <input type="hidden" name="lot" value="%s">
                                <button type="submit" class="btn btn-outline-danger btn-sm" onclick="return confirm('Delete this resident?')">Delete</button>
                            </form>
                        </td>
                    </tr>
                    """, lot, street, name, status.equalsIgnoreCase("Updated") ? "bg-success" : "bg-warning text-dark", status, lot));
            }

            return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>HOA Portal</title>
                    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                    <style>
                        body { background-color: #f8f9fa; font-family: sans-serif; }
                        .navbar { background: linear-gradient(90deg, #0d6efd 0%, #0a58ca 100%); }
                        .card { border-radius: 15px; border: none; }
                    </style>
                </head>
                <body>
                    <nav class="navbar navbar-dark mb-4 p-3 shadow">
                        <div class="container d-flex justify-content-between">
                            <span class="navbar-brand h1 mb-0">🏘️ HOA Portal</span>
                            <button class="btn btn-light rounded-pill px-4 fw-bold" data-bs-toggle="modal" data-bs-target="#regModal">+ Add Resident</button>
                        </div>
                    </nav>
                    <div class="container">
                        <div class="card p-4 shadow-sm">
                            <input type="text" id="searchInput" class="form-control mb-4 py-2" placeholder="🔍 Search by name, lot, or street...">
                            <table class="table table-hover align-middle" id="residentTable">
                                <thead class="table-light">
                                    <tr><th>Lot/Block</th><th>Street</th><th>Homeowner</th><th>Status</th><th>Action</th></tr>
                                </thead>
                                <tbody>""" + rowsHtml.toString() + """
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="modal fade" id="regModal" tabindex="-1">
                      <div class="modal-dialog modal-dialog-centered">
                        <form action="/portal/register" method="POST" class="modal-content shadow-lg border-0" style="border-radius:20px;">
                          <div class="modal-header border-0"><h5 class="modal-title fw-bold">Add New Resident</h5></div>
                          <div class="modal-body p-4">
                            <div class="mb-3"><label class="fw-bold small text-muted">LOT/BLK</label><input name="lot" class="form-control" required></div>
                            <div class="mb-3"><label class="fw-bold small text-muted">STREET</label><input name="street" class="form-control" required></div>
                            <div class="mb-3"><label class="fw-bold small text-muted">OWNER NAME</label><input name="name" class="form-control" required></div>
                            <button type="submit" class="btn btn-primary w-100 py-2 fw-bold">Save to Database</button>
                          </div>
                        </form>
                      </div>
                    </div>

                    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                    <script>
                        document.getElementById('searchInput').addEventListener('keyup', function() {
                            const filter = this.value.toLowerCase();
                            const rows = document.querySelectorAll("#residentTable tbody tr");
                            rows.forEach(row => {
                                row.style.display = row.innerText.toLowerCase().includes(filter) ? "" : "none";
                            });
                        });
                    </script>
                </body>
                </html>
                """;
        } catch (Exception e) { return "Error connecting to Hasura: " + e.getMessage(); }
    }

    @POST
    @Path("/register")
    @RolesAllowed("admin") // <--- Security triggered here
    public Response register(@FormParam("lot") String lot, @FormParam("name") String name, @FormParam("street") String street) {
        try {
            String mutation = String.format("{\"query\": \"mutation { insert_residents_one(object: {house_number: \\\"%s\\\", owner_name: \\\"%s\\\", street_name: \\\"%s\\\", monthly_dues_status: \\\"Pending\\\"}) { id } }\"}", lot, name, street);
            sendHasuraRequest(mutation);
            return Response.seeOther(URI.create("/portal")).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }

    @POST
    @Path("/delete")
    @RolesAllowed("admin") // <--- Security triggered here
    public Response delete(@FormParam("lot") String lot) {
        try {
            String mutation = String.format("{\"query\": \"mutation { delete_residents(where: {house_number: {_eq: \\\"%s\\\"}}) { affected_rows } }\"}", lot);
            sendHasuraRequest(mutation);
            return Response.seeOther(URI.create("/portal")).build();
        } catch (Exception e) { return Response.serverError().build(); }
    }

    private void sendHasuraRequest(String query) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(hasuraUrl))
                .header("Content-Type", "application/json")
                .header("x-hasura-admin-secret", hasuraSecret)
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
