import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ConversorMonedasMenu {

    private static final String API_KEY = "d4845be4bbd791438c2a0780";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<String> historial = new ArrayList<>();

    // Colores
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String PURPLE = "\u001B[35m";

    public static void main(String[] args) {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Selecciona una opción: ");
            switch (opcion) {
                case 1 -> convertirMoneda();
                case 2 -> mostrarTasas();
                case 3 -> mostrarHistorial();
                case 4 -> salir();
                default -> System.out.println(RED + "Opción no válida." + RESET);
            }
        } while (opcion != 4);
    }

    private static void mostrarMenu() {
        System.out.println(CYAN + "\n===== CONVERSOR DE MONEDAS =====" + RESET);
        System.out.println(YELLOW + "1." + RESET + " Convertir moneda");
        System.out.println(YELLOW + "2." + RESET + " Ver todas las tasas de una moneda");
        System.out.println(YELLOW + "3." + RESET + " Ver historial de conversiones");
        System.out.println(YELLOW + "4." + RESET + " Salir");
    }

    private static void convertirMoneda() {
        boolean continuar = true;
        while (continuar) {
            String monedaBase = leerMonedaValida("Introduce el código de la moneda base (ej. USD): ");
            String monedaDestino = leerMonedaValida("Introduce el código de la moneda destino (ej. CLP): ");
            double cantidad = leerDouble("Introduce la cantidad a convertir: ");

            try {
                JsonObject json = obtenerDatosAPI(monedaBase);
                if (json != null) {
                    JsonObject rates = json.getAsJsonObject("conversion_rates");
                    if (!rates.has(monedaDestino)) {
                        System.out.println(RED + "Moneda destino no válida." + RESET);
                        continue;
                    }
                    double tasa = rates.get(monedaDestino).getAsDouble();
                    double resultado = cantidad * tasa;
                    String mensaje = String.format("%.2f %s = %,.2f %s", cantidad, monedaBase, resultado, monedaDestino);
                    System.out.println(GREEN + mensaje + RESET);
                    historial.add(mensaje);
                }
            } catch (Exception e) {
                System.out.println(RED + "Error al conectar con la API: " + e.getMessage() + RESET);
            }

            System.out.print(PURPLE + "\n¿Deseas hacer otra conversión? (S/N): " + RESET);
            String resp = scanner.nextLine().trim().toUpperCase();
            if (!resp.equals("S") && !resp.equals("SI")) {
                System.out.println(GREEN + "Operación finalizada. Volviendo al menú..." + RESET);
                continuar = false;
            }
        }
    }

    private static void mostrarTasas() {
        String monedaBase = leerMonedaValida("Introduce el código de la moneda base (ej. USD): ");
        try {
            JsonObject json = obtenerDatosAPI(monedaBase);
            if (json != null) {
                JsonObject rates = json.getAsJsonObject("conversion_rates");
                System.out.println(CYAN + "Tasas de " + monedaBase + ":" + RESET);
                rates.entrySet().forEach(e ->
                        System.out.printf("%s%-5s%s : %,.2f%n", YELLOW, e.getKey(), RESET, e.getValue().getAsDouble()));
            }
        } catch (Exception e) {
            System.out.println(RED + "Error al obtener tasas: " + e.getMessage() + RESET);
        }
    }

    private static void mostrarHistorial() {
        if (historial.isEmpty()) {
            System.out.println(RED + "No hay conversiones aún." + RESET);
        } else {
            System.out.println(CYAN + "Historial de conversiones:" + RESET);
            historial.forEach(c -> System.out.println(YELLOW + "- " + RESET + c));
        }
    }

    private static void salir() {
        System.out.println(GREEN + "Gracias por usar el conversor. ¡Hasta luego!" + RESET);
    }

    private static String leerMonedaValida(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String codigo = scanner.nextLine().trim().toUpperCase();
            if (codigo.length() != 3) {
                System.out.println(RED + "Código inválido. Debe tener 3 letras." + RESET);
                continue;
            }
            if (monedaExiste(codigo)) return codigo;
            System.out.println(RED + "Moneda no reconocida. Intenta de nuevo." + RESET);
        }
    }

    private static boolean monedaExiste(String codigo) {
        try {
            JsonObject json = obtenerDatosAPI("USD"); // obtenemos la lista de monedas disponibles
            JsonObject rates = json.getAsJsonObject("conversion_rates");
            return rates.has(codigo);
        } catch (Exception e) {
            return false;
        }
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println(RED + "Ingresa un número válido." + RESET);}
        }
    }

    private static double leerDouble(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try { return Double.parseDouble(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println(RED + "Ingresa un valor numérico válido." + RESET);}
        }
    }

    private static JsonObject obtenerDatosAPI(String monedaBase) throws Exception {
        String urlStr = BASE_URL + monedaBase;
        URL url = new URL(urlStr.trim());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder resp = new StringBuilder();
        String linea;
        while ((linea = in.readLine()) != null) resp.append(linea);
        in.close();

        return new Gson().fromJson(resp.toString(), JsonObject.class);
    }
}
