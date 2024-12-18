package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;

class AzureDalle {
  private static final HttpClient client = HttpClient.newHttpClient();
  private static final String API_KEY = "5jz0SGM1ecSnsbHFiukqzb9LawygHCthZ7E04rAT8qE2nZU3Vmt7JQQJ99ALACfhMk5XJ3w3AAAAACOGxQvj";
  private static final String ENDPOINT = "https://kosie-m4szfuz3-swedencentral.openai.azure.com/openai/deployments/dall-e-3/images/generations?api-version=2024-02-01";

  private String prompt;

  public AzureDalle(String prompt) {
    this.prompt = prompt;
  }

  private HttpRequest.BodyPublisher makeBody() {
    JSONObject body = new JSONObject()
      .put("prompt", this.prompt)
      .put("n", 1)
      .put("size", "1024x1024"); // Square image
    return HttpRequest.BodyPublishers.ofString(body.toString());
  }


  private HttpRequest makeRequest() {
    return HttpRequest.newBuilder(URI.create(ENDPOINT))
      .header("Content-Type", "application/json")
      .header("api-key", API_KEY)
      .POST(makeBody())
      .build();
  }

  private String extractImageUrl(JSONObject response) {
    JSONArray data = response.getJSONArray("data");
    return data.getJSONObject(0).getString("url");
  }

  private String downloadImage(String imageUrl) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(URI.create(imageUrl)).build();
    String localPath = "puzzle_image.png";
    System.out.println("Image saved to: " + Paths.get(localPath).toAbsolutePath());


    HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(Paths.get(localPath)));

    if (response.statusCode() == 200) {
      System.out.println("Image downloaded successfully: " + localPath);
      return localPath;
    } else {
      throw new RuntimeException("Failed to download image.");
    }
  }

  public String generateImage() {
    HttpRequest request = makeRequest();
    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      System.out.println("Response body: " + response.body()); // Debug

      if (response.statusCode() != 200) {
        throw new RuntimeException("Error: " + response.body());
      }

      String imageUrl = extractImageUrl(new JSONObject(response.body()));
      return downloadImage(imageUrl);

    } catch (Exception e) {
      throw new RuntimeException("Failed to generate image: " + e.getMessage());
    }
  }
}



