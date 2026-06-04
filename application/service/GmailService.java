package application.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Service d'envoi d'e-mails via l'API Gmail REST.
 * Aucun JAR externe requis — utilise uniquement java.net.http (Java 11+).
 *
 * ─── Obtenir l'Access Token ───────────────────────────────────────────────
 *  1. Allez sur https://developers.google.com/oauthplayground
 *  2. Cochez : https://www.googleapis.com/auth/gmail.send
 *  3. "Authorize APIs" → connectez votre compte Gmail
 *  4. "Exchange authorization code for tokens"
 *  5. Copiez Access Token → collez dans ACCESS_TOKEN ci-dessous
 * ──────────────────────────────────────────────────────────────────────────
 */
public class GmailService {

    // ✅ Collez votre Access Token OAuth2 ici
    private static final String ACCESS_TOKEN = "your token";

    private static final DateTimeFormatter DATE_FMT  =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HEURE_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    // ─────────────────────────────────────────────────────────────────────
    // ✅ Notification : NOUVEAU rendez-vous
    // ─────────────────────────────────────────────────────────────────────
    public static boolean envoyerNouveauRdvMedecin(
            String medecinEmail,
            String medecinNom,
            String patientNom,
            LocalDate dateRdv,
            LocalTime heureRdv,
            String motif) {

        if (medecinEmail == null || medecinEmail.isBlank()) {
            System.err.println("⚠ Email médecin manquant.");
            return false;
        }

        String sujet = "Nouveau rendez-vous  " + patientNom;
        String corps = buildHtmlNouveauRdv(medecinNom, patientNom, dateRdv, heureRdv, motif);
        return envoyerEmail(medecinEmail, sujet, corps);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ✅ Notification : ANNULATION rendez-vous
    // ─────────────────────────────────────────────────────────────────────
    public static boolean envoyerAnnulationRdvMedecin(
            String medecinEmail,
            String medecinNom,
            String patientNom,
            LocalDate dateRdv,
            LocalTime heureRdv,
            String motif) {

        if (medecinEmail == null || medecinEmail.isBlank()) {
            System.err.println("⚠ Email médecin manquant.");
            return false;
        }

        String sujet = "Annulation de rendez-vous " + patientNom;
        String corps = buildHtmlAnnulation(medecinNom, patientNom, dateRdv, heureRdv, motif);
        return envoyerEmail(medecinEmail, sujet, corps);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Méthode commune d'envoi HTTP
    // ─────────────────────────────────────────────────────────────────────
    private static boolean envoyerEmail(String to, String subject, String htmlBody) {
        try {
            String rawEmail = "To: " + to + "\r\n" +
                              "Subject: " + subject + "\r\n" +
                              "MIME-Version: 1.0\r\n" +
                              "Content-Type: text/html; charset=UTF-8\r\n" +
                              "\r\n" +
                              htmlBody;

            String encoded = Base64.getUrlEncoder()
                    .encodeToString(rawEmail.getBytes(StandardCharsets.UTF_8));

            String jsonBody = "{\"raw\":\"" + encoded + "\"}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                        "https://gmail.googleapis.com/gmail/v1/users/me/messages/send"))
                    .header("Authorization", "Bearer " + ACCESS_TOKEN)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ E-mail envoyé à " + to);
                return true;
            } else {
                System.err.println("❌ Erreur Gmail API : "
                    + response.statusCode() + " → " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Exception envoi e-mail : " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // HTML : Nouveau RDV (vert)
    // ─────────────────────────────────────────────────────────────────────
    private static String buildHtmlNouveauRdv(
            String medecinNom, String patientNom,
            LocalDate dateRdv, LocalTime heureRdv, String motif) {

        String dateStr  = dateRdv  != null ? dateRdv.format(DATE_FMT)   : "–";
        String heureStr = heureRdv != null ? heureRdv.format(HEURE_FMT) : "–";
        String motifStr = (motif != null && !motif.isBlank()) ? motif : "Non précisé";

        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head><meta charset="UTF-8"/>
                  <style>
                    body  { font-family:Arial,sans-serif; background:#f4f6f8;
                            margin:0; padding:20px; color:#2c3e50; }
                    .card { background:#fff; border-radius:8px; max-width:560px;
                            margin:auto; padding:32px;
                            box-shadow:0 2px 8px rgba(0,0,0,.1); }
                    .badge{ display:inline-block; background:#e8f8f5;
                            color:#1e8449; font-weight:bold; padding:4px 12px;
                            border-radius:20px; font-size:13px; }
                    table { width:100%%; border-collapse:collapse; margin-top:20px; }
                    td    { padding:10px 12px; border-bottom:1px solid #ecf0f1;
                            font-size:14px; }
                    td:first-child{ font-weight:bold; color:#7f8c8d; width:38%%; }
                    .footer{ margin-top:28px; font-size:12px; color:#95a5a6;
                             text-align:center; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <p>Bonjour <strong>Dr. %s</strong>,</p>
                    <p>Un patient vient de prendre un rendez-vous avec vous.</p>
                    <span class="badge">📅 Nouveau rendez-vous</span>
                    <table>
                      <tr><td>Patient</td>        <td>%s</td></tr>
                      <tr><td>Date</td>           <td>%s</td></tr>
                      <tr><td>Heure</td>          <td>%s</td></tr>
                      <tr><td>Motif</td>          <td>%s</td></tr>
                      <tr><td>Statut</td>         <td>⏳ En attente de confirmation</td></tr>
                    </table>
                    <p style="margin-top:20px;">
                      Connectez-vous à l'application pour confirmer ou refuser
                      ce rendez-vous.
                    </p>
                    <div class="footer">
                      E-mail automatique — Système de gestion HopitalGest.
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(medecinNom, patientNom, dateStr, heureStr, motifStr);
    }

    // ─────────────────────────────────────────────────────────────────────
    // HTML : Annulation RDV (rouge)
    // ─────────────────────────────────────────────────────────────────────
    private static String buildHtmlAnnulation(
            String medecinNom, String patientNom,
            LocalDate dateRdv, LocalTime heureRdv, String motif) {

        String dateStr  = dateRdv  != null ? dateRdv.format(DATE_FMT)   : "–";
        String heureStr = heureRdv != null ? heureRdv.format(HEURE_FMT) : "–";
        String motifStr = (motif != null && !motif.isBlank()) ? motif : "Non précisé";

        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head><meta charset="UTF-8"/>
                  <style>
                    body  { font-family:Arial,sans-serif; background:#f4f6f8;
                            margin:0; padding:20px; color:#2c3e50; }
                    .card { background:#fff; border-radius:8px; max-width:560px;
                            margin:auto; padding:32px;
                            box-shadow:0 2px 8px rgba(0,0,0,.1); }
                    .badge{ display:inline-block; background:#fdedec;
                            color:#c0392b; font-weight:bold; padding:4px 12px;
                            border-radius:20px; font-size:13px; }
                    table { width:100%%; border-collapse:collapse; margin-top:20px; }
                    td    { padding:10px 12px; border-bottom:1px solid #ecf0f1;
                            font-size:14px; }
                    td:first-child{ font-weight:bold; color:#7f8c8d; width:38%%; }
                    .footer{ margin-top:28px; font-size:12px; color:#95a5a6;
                             text-align:center; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <p>Bonjour <strong>Dr. %s</strong>,</p>
                    <p>Nous vous informons qu'un patient a annulé son rendez-vous.</p>
                    <span class="badge">❌ Rendez-vous annulé</span>
                    <table>
                      <tr><td>Patient</td>        <td>%s</td></tr>
                      <tr><td>Date</td>           <td>%s</td></tr>
                      <tr><td>Heure</td>          <td>%s</td></tr>
                      <tr><td>Motif initial</td>  <td>%s</td></tr>
                    </table>
                    <p style="margin-top:20px;">
                      Ce créneau est désormais disponible. Connectez-vous à
                      l'application pour gérer votre planning.
                    </p>
                    <div class="footer">
                      E-mail automatique — Système de gestion HopitalGest.
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(medecinNom, patientNom, dateStr, heureStr, motifStr);
    }
    public static boolean envoyerConfirmationRdvPatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            LocalDate date,
            LocalTime heure,
            String motif) {

        String sujet = "Rendez-vous confirme  "
            + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String corps =
            "Bonjour " + nomPatient + ",\n\n" +
            "Votre rendez-vous a été confirmé par Dr. " + nomMedecin + ".\n\n" +
            "📅 Date  : " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
            "🕐 Heure : " + heure.format(DateTimeFormatter.ofPattern("HH:mm")) + "\n" +
            "📝 Motif : " + motif + "\n\n" +
            "Merci de vous présenter 10 minutes avant l'heure prévue.\n\n" +
            "Cordialement,\nDr. " + nomMedecin;

        return envoyerEmail(emailPatient, sujet, corps);
    }

    public static boolean envoyerRefusRdvPatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            LocalDate date,
            LocalTime heure,
            String motif) {

        String sujet = "Rendez-vous annule  "
            + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String corps =
            "Bonjour " + nomPatient + ",\n\n" +
            "Votre rendez-vous avec Dr. " + nomMedecin + " a été annulé.\n\n" +
            "📅 Date  : " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
            "🕐 Heure : " + heure.format(DateTimeFormatter.ofPattern("HH:mm")) + "\n" +
            "📝 Motif : " + motif + "\n\n" +
            "Veuillez reprendre rendez-vous à votre convenance.\n\n" +
            "Cordialement,\nDr. " + nomMedecin;

        return envoyerEmail(emailPatient, sujet, corps);
    }
}