package com.example.front_android.PETICIONES_API;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.front_android.MainActivity;
import com.example.front_android.Modelos.Rol;
import com.example.front_android.Modelos.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import android.widget.Toast;


public class PeticionesUsuarios {

    /**
     * Método para parsear un objeto JSON a un objeto Usuario.
     */
    private static Usuario parseUsuario(JSONObject usuarioObject) throws JSONException {
        Usuario usuario = new Usuario();

        usuario.setId(usuarioObject.getInt("id"));
        usuario.setNombre(usuarioObject.getString("nombre"));
        usuario.setCorreoElectronico(usuarioObject.getString("correo"));
        usuario.setContrasena(usuarioObject.optString("contraseña", null)); // Manejo opcional de la contraseña

        // Parsear el rol del usuario
        JSONObject rolObject = usuarioObject.getJSONObject("rol");
        Rol rol = new Rol();
        rol.setId(rolObject.getInt("id"));
        rol.setNombre(rolObject.getString("nombre"));
        usuario.setRol(rol);

        return usuario;
    }

    /**
     * Método asíncrono para obtener un usuario desde la API.
     */
    public static class ObtenerUsuarios extends AsyncTask<Void, Void, Usuario> {

        @Override
        protected Usuario doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuilder jsonResult = new StringBuilder();

            try {
                //URL de los usuarios
                URL url = new URL("http://10.10.13.251:8080/usuarios");
                urlConnection = (HttpURLConnection) url.openConnection();

                // Verificar el código de respuesta
                int code = urlConnection.getResponseCode();

                if (code != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Respuesta inválida del servidor: " + code);
                }

                // Leer la respuesta del servidor
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResult.append(line).append("\n");
                }

            } catch (IOException e) {
                Log.e("Error", "Error de conexión: " + e.getMessage());
                e.printStackTrace();
                return null;
            } finally {

                // Cerrar recursos
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("Error", "Error al cerrar el reader: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            // Parsear la respuesta JSON a un objeto Usuario
            try {
                JSONObject usuarioObject = new JSONObject(jsonResult.toString());
                return parseUsuario(usuarioObject);
            } catch (JSONException e) {
                Log.e("Error", "Error al parsear el JSON: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Usuario usuario) {
            if (usuario != null) {
                Log.d("Usuario", usuario.toString());
            } else {
                Log.e("Usuario", "No se pudo obtener el usuario.");
            }

        }
    }

    /**
     * Método asíncrono para obtner solo un usuario
     */
    public static class ObtenerUnUsuario extends AsyncTask<Integer, Void, Usuario> {

        @Override
        protected Usuario doInBackground(Integer... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuilder jsonResult = new StringBuilder();

            try {
                int idUsuario = params[0];
                // URL de un usuario
                URL url = new URL("http://10.10.13.251:8080/usuarios/" + idUsuario);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Accept", "application/json");

                // Verifica el código de respuesta del servidor
                int code = urlConnection.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Respuesta inválida del servidor: " + code);
                }

                // Lee la respuesta del servidor
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResult.append(line).append("\n");
                }

            } catch (IOException e) {
                Log.e("Error", "Error de conexión: " + e.getMessage());
                e.printStackTrace();
                return null;
            } finally {
                // Cerrar recursos
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("Error", "Error al cerrar el reader: " + e.getMessage());
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            // Parsear la respuesta JSON a un objeto Usuario
            try {
                JSONObject usuarioObject = new JSONObject(jsonResult.toString());
                return parseUsuario(usuarioObject);
            } catch (JSONException e) {
                Log.e("Error", "Error al parsear el JSON: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Método asíncrono para loguear un usuario
     */
    public static class LoguearUsuario extends AsyncTask<String, Void, String> {
        private Context context;

        public LoguearUsuario(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuilder jsonResult = new StringBuilder();

            try {
                // Url del servidor para iniciar sesión
                URL url = new URL("http://10.10.13.251:8080/loginAndroid");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);

                String nombre = strings[0];
                String contraseña = strings[1];

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("nombre", nombre);
                jsonParam.put("contraseña", contraseña);

                // Enviar datos al servidor
                try (DataOutputStream writer = new DataOutputStream(urlConnection.getOutputStream())) {
                    writer.write(jsonParam.toString().getBytes(StandardCharsets.UTF_8));
                    writer.flush();
                }

                // Obtener respuesta del servidor
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResult.append(line);
                    }

                } else {
                    Log.e("LoguearUsuario", "Error: Código de respuesta " + responseCode);
                    return null;
                }

            } catch (Exception e) {
                Log.e("LoguearUsuario", "Error en la conexión: " + e.getMessage(), e);
                return null;
            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return jsonResult.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {

                    // Parsear la respuesta del servidor
                    JSONObject responseJson = new JSONObject(result);

                    String rol = responseJson.optString("rol");

                    // Redirige al MainActivity
                    if (rol != null && !rol.isEmpty()) {
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);

                    }

                    // Guarda el ID del usuario en el SharedPreferences
                    String id = responseJson.optString("id");
                    SharedPreferences preferences = context.getSharedPreferences("app_localDatos", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.putString("idUsuario", id);
                    editor.putBoolean("session", true);
                    editor.apply();


                } catch (JSONException e) {
                    Log.e("LoguearUsuario", "Error al parsear la respuesta JSON: " + e.getMessage());
                    Toast.makeText(context, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context,"El usuario no existe o la contraseña es incorrecta", Toast.LENGTH_SHORT).show();
            }
        }


    }

    /**
     * Método asíncrono para registrar un usuario
     */
    public static class RegistrarUsuario extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuilder jsonResult = new StringBuilder();

            try {
                // Url del servidor para registrar un usuario
                URL url = new URL("http://10.10.13.251:8080/registroAndroid");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);

                String nombre = strings[0];
                String contrasena = strings[1];
                String correo = strings[2];


                JSONObject jsonParam = new JSONObject();
                jsonParam.put("nombre", nombre);
                jsonParam.put("contraseña", contrasena);
                jsonParam.put("correo", correo);

                // Enviar datos al servidor
                try (DataOutputStream writer = new DataOutputStream(urlConnection.getOutputStream())) {
                    writer.write(jsonParam.toString().getBytes(StandardCharsets.UTF_8));
                    writer.flush();
                }

                // Leer respuesta del servidor
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResult.append(line);
                    }
                } else {
                    Log.e("RegistrarUsuario", "Error: Código de respuesta " + responseCode);
                    return null;
                }
            } catch (Exception e) {
                Log.e("RegistrarUsuario", "Error en la conexión: " + e.getMessage(), e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return jsonResult.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d("RegistrarUsuario", "Respuesta del servidor: " + result);
            } else {
                Log.e("RegistrarUsuario", "No se recibió respuesta del servidor.");
            }
        }
    }

}
